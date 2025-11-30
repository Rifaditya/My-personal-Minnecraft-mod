package net.conczin.mca.entity.ai.relationship;

import net.conczin.mca.registry.CriterionMCA;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface EntityRelationship {
    static Optional<EntityRelationship> of(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            return Optional.ofNullable(PlayerSaveData.get(player));
        }

        if (entity instanceof CompassionateEntity<?> compassionateEntity) {
            return Optional.ofNullable(compassionateEntity.getRelationships());
        }

        return Optional.empty();
    }

    default Gender getGender() {
        return Gender.MALE;
    }

    default FamilyTree getFamilyTree() {
        return FamilyTree.get(getWorld());
    }

    ServerLevel getWorld();

    UUID getUUID();

    @NotNull
    FamilyTreeNode getFamilyEntry();

    default Stream<Entity> getFamily(int parents, int children) {
        return getFamilyEntry()
                .getRelatives(parents, children)
                .map(getWorld()::getEntity)
                .filter(Objects::nonNull)
                .filter(e -> !e.getUUID().equals(getUUID()));
    }

    default Stream<Entity> getParents() {
        return getFamilyEntry().streamParents().map(getWorld()::getEntity).filter(Objects::nonNull);
    }

    /**
     * @deprecated Use {@link #getPartners()} instead.
     */
    @Deprecated
    default Optional<Entity> getPartner() {
        return Optional.ofNullable(getWorld().getEntity(getFamilyEntry().partner()));
    }

    default Stream<Entity> getPartners() {
        return getFamilyEntry().partners().stream()
                .map(uuid -> getWorld().getEntity(uuid))
                .filter(Objects::nonNull);
    }

    // try to load a PlayerSaveData before loading the entity
    // that way, offline players are also considered
    default Stream<EntityRelationship> getRelationshipStream(Stream<UUID> uuids) {
        return uuids.map(uuid -> PlayerSaveData.getIfPresent(getWorld(), uuid)
                .map(p -> (EntityRelationship) p)
                .or(() -> EntityRelationship.of(getWorld().getEntity(uuid))))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    default void onTragedy(DamageSource cause, @Nullable BlockPos burialSite, RelationshipType type, Entity victim) {
        if (type == RelationshipType.STRANGER) {
            return; // effects don't propagate from strangers
        }

        // notify family
        if (type == RelationshipType.SELF) {
            getRelationshipStream(getFamilyEntry().streamParents())
                    .forEach(r -> r.onTragedy(cause, burialSite, RelationshipType.CHILD, victim));

            getRelationshipStream(getFamilyEntry().siblings().stream())
                    .forEach(r -> r.onTragedy(cause, burialSite, RelationshipType.SIBLING, victim));

            getRelationshipStream(getFamilyEntry().partners().stream())
                    .forEach(r -> r.onTragedy(cause, burialSite, RelationshipType.SPOUSE, victim));
        }

        // end the marriage for both the deceased one and the spouse
        if (type == RelationshipType.SPOUSE || type == RelationshipType.SELF) {
            if (getRelationshipState().isMarried()) {
                // If self died, we are dead, so we don't need to do anything to our own state
                // really,
                // but we should probably ensure our partners know we are gone?
                // The current logic calls endRelationShip(WIDOW).
                // With multiple spouses, if one dies, are we a widow? Yes.
                // But do we lose ALL spouses? Probably not.
                // But the original logic was "end the marriage".
                // For now, let's keep it simple: if a spouse dies, we remove THAT spouse.

                if (type == RelationshipType.SPOUSE && victim != null) {
                    // A spouse died. Remove them.
                    getFamilyEntry().removePartner(victim.getUUID());
                    // If we have no more partners, we are a widow (or single? Widow usually implies
                    // dead spouse).
                    // But if we still have partners, we are still married.
                    if (getFamilyEntry().partners().isEmpty()) {
                        // We are now a widow of that specific spouse, but state is global.
                        // Let's set to WIDOW if no partners left.
                        getFamilyEntry().setRelationshipState(RelationshipState.WIDOW);
                    }
                } else if (type == RelationshipType.SELF) {
                    // We died. Our partners should probably know?
                    // The notification logic above (notify family) handles the "onTragedy" call to
                    // partners.
                    // So they will receive a SPOUSE tragedy and execute the block above.
                    // For us (the dead person), we can just clear everything or set to deceased.
                    // The FamilyTreeNode has a 'deceased' flag.
                }
            } else {
                // endRelationShip(RelationshipState.SINGLE);
            }
        }
    }

    default boolean canMarry(Entity spouse) {
        // No Polyandry: Females can only have 1 spouse.
        if (getGender() == Gender.FEMALE && !getFamilyEntry().partners().isEmpty()) {
            // Allow if the spouse is already a partner (e.g. upgrading from Engaged)
            if (getFamilyEntry().partners().contains(spouse.getUUID())) {
                return true;
            }
            return false;
        }
        // Males can have multiple.
        return true;
    }

    default void marry(Entity spouse) {
        if (!canMarry(spouse)) {
            return;
        }

        RelationshipState state = spouse instanceof Player ? RelationshipState.MARRIED_TO_PLAYER
                : RelationshipState.MARRIED_TO_VILLAGER;
        if (spouse instanceof ServerPlayer spouseEntity) {
            CriterionMCA.GENERIC_EVENT.trigger(spouseEntity, "marriage");
        }
        // Use addPartner instead of updatePartner (which clears)
        getFamilyEntry().addPartner(spouse, state);
    }

    default void engage(Entity spouse) {
        if (spouse instanceof ServerPlayer spouseEntity) {
            CriterionMCA.GENERIC_EVENT.trigger(spouseEntity, "engage");
        }
        // Engagement is usually 1-to-1? For now let's assume yes or treat it like
        // marriage.
        // The original code used updatePartner which replaces.
        // Let's stick to updatePartner for engagement to keep it simple/exclusive until
        // marriage?
        // Or should engagement also be poly?
        // Let's assume engagement is a precursor to marriage, so it should follow
        // similar rules.
        // But 'partners' set is for MARRIAGE. Engagement uses the same field?
        // Yes, 'partner' field was used for everything.
        // So if we engage, we add to partners?
        // If we use addPartner, we are "married" in data structure but state is
        // ENGAGED.
        getFamilyEntry().addPartner(spouse, RelationshipState.ENGAGED);
    }

    default void promise(Entity spouse) {
        if (spouse instanceof ServerPlayer spouseEntity) {
            CriterionMCA.GENERIC_EVENT.trigger(spouseEntity, "promise");
        }
        getFamilyEntry().addPartner(spouse, RelationshipState.PROMISED);
    }

    default void endRelationShip(RelationshipState newState) {
        getFamilyEntry().updatePartner(null, newState);
    }

    default RelationshipState getRelationshipState() {
        return getFamilyEntry().getRelationshipState();
    }

    default Optional<UUID> getPartnerUUID() {
        // Return first partner for compat
        UUID spouse = getFamilyEntry().partner();
        if (spouse.equals(Util.NIL_UUID)) {
            return Optional.empty();
        } else {
            return Optional.of(spouse);
        }
    }

    default Optional<Component> getPartnerName() {
        return getFamilyTree().getOrEmpty(getFamilyEntry().partner()).map(FamilyTreeNode::getName)
                .map(Component::literal);
    }

    default boolean isMarried() {
        return !getFamilyEntry().partners().isEmpty() && (getRelationshipState() == RelationshipState.MARRIED_TO_PLAYER
                || getRelationshipState() == RelationshipState.MARRIED_TO_VILLAGER);
    }

    default boolean isEngaged() {
        return getRelationshipState() == RelationshipState.ENGAGED;
    }

    default boolean isPromised() {
        return getRelationshipState() == RelationshipState.PROMISED;
    }

    default boolean isPromisedTo(UUID uuid) {
        return getFamilyEntry().partners().contains(uuid) && isPromised();
    }

    default boolean isMarriedTo(UUID uuid) {
        return getFamilyEntry().partners().contains(uuid) && isMarried();
    }

    default boolean isEngagedWith(UUID uuid) {
        return getFamilyEntry().partners().contains(uuid) && isEngaged();
    }
}
