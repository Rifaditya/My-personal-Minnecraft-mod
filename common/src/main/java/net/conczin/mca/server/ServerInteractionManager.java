package net.conczin.mca.server;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.conczin.mca.Config;
import net.conczin.mca.entity.ai.relationship.EntityRelationship;
import net.conczin.mca.entity.ai.relationship.RelationshipState;
import net.conczin.mca.item.BabyItem;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.OpenDestinyGuiRequest;
import net.conczin.mca.network.s2c.ShowToastRequest;
import net.conczin.mca.server.world.data.PlayerSaveData;
import net.conczin.mca.server.world.data.FamilyTreeNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class ServerInteractionManager {

    private static final ServerInteractionManager INSTANCE = new ServerInteractionManager();

    /**
     * Maps a player's UUID to a list of UUIDs that have proposed to them with /mca
     * propose
     */
    private final Map<UUID, List<UUID>> proposals = new HashMap<>();

    /**
     * List of UUIDs that initiated procreation mapped to the time the request
     * expires.
     */
    private final Object2LongArrayMap<UUID> procreateMap = new Object2LongArrayMap<>();

    /**
     * Maps player UUID to the game time of their last successful procreation.
     * Used for cooldown tracking.
     */
    private final Object2LongArrayMap<UUID> lastProcreationTime = new Object2LongArrayMap<>();

    private ServerInteractionManager() {
    }

    public static ServerInteractionManager getInstance() {
        return INSTANCE;
    }

    public static void launchDestiny(ServerPlayer player) {
        Network.sendToPlayer(new OpenDestinyGuiRequest(player), player);
    }

    public void tick() {
        List<UUID> removals = new ArrayList<>();
        procreateMap.keySet().stream()
                .filter((k) -> procreateMap.getLong(k) < System.currentTimeMillis())
                .forEach(removals::add);
        removals.forEach(procreateMap::removeLong);
    }

    public void onPlayerJoin(ServerPlayer player) {
        PlayerSaveData playerData = PlayerSaveData.get(player);
        if (!playerData.isEntityDataSet()) {
            if (Config.getInstance().launchIntoDestiny) {
                launchDestiny(player);

                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 3600));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 3600));
            } else if (Config.getInstance().allowDestinyCommandOnce) {
                Network.sendToPlayer(new ShowToastRequest(
                        "server.destinyNotSet.title",
                        "server.destinyNotSet.description"), player);
            } else if (Config.getInstance().allowFullPlayerEditor) {
                Network.sendToPlayer(new ShowToastRequest(
                        "server.playerNotCustomized.title",
                        "server.playerNotCustomized.description"), player);
            }
        }

        if (playerData.hasMail()) {
            PlayerSaveData.showMailNotification(player);
        }
    }

    /**
     * Returns true if receiver has a proposal from sender.
     *
     * @param sender   Command sender
     * @param receiver Player whose name was entered by the sender
     * @return boolean
     */
    private boolean hasProposalFrom(ServerPlayer sender, ServerPlayer receiver) {
        return getProposalsFor(receiver).contains(sender.getUUID());
    }

    /**
     * Returns all proposals for the provided player
     *
     * @param player Player whose proposals should be returned.
     * @return List<UUID>
     */
    private List<UUID> getProposalsFor(ServerPlayer player) {
        return proposals.getOrDefault(player.getUUID(), new ArrayList<>());
    }

    /**
     * Removes the provided proposer from the target's list of proposals.
     *
     * @param target   Target player whose proposal list will be modified.
     * @param proposer The proposer to the target player.
     */
    private void removeProposalFor(ServerPlayer target, ServerPlayer proposer) {
        List<UUID> list = getProposalsFor(target);
        list.remove(proposer.getUUID());
        proposals.put(target.getUUID(), list);
    }

    /**
     * Lists all proposals for the given player.
     *
     * @param sender Player whose active proposals will be listed.
     */
    public void listProposals(ServerPlayer sender) {
        List<UUID> proposals = getProposalsFor(sender);

        if (proposals.isEmpty()) {
            infoMessage(sender, Component.translatable("server.noProposals"));
        } else {
            infoMessage(sender, Component.translatable("server.proposals"));
        }

        // Send the name of all online players to the command sender.
        proposals.forEach((uuid -> {
            Player player = sender.level().getPlayerByUUID(uuid);
            if (player != null) {
                infoMessage(sender, Component.literal("- ").append(Component.literal(player.getScoreboardName())));
            }
        }));
    }

    /**
     * Sends a proposal from the sender to the receiver.
     *
     * @param sender   The player sending the proposal.
     * @param receiver The player being proposed to.
     */
    /**
     * Sends a proposal from the sender to the receiver.
     *
     * @param sender   The player sending the proposal.
     * @param receiver The player being proposed to.
     */
    public void sendProposal(ServerPlayer sender, ServerPlayer receiver) {
        // Checks if the admin allows this
        if (!Config.getInstance().allowPlayerMarriage) {
            failMessage(sender, Component.translatable("notify.playerMarriage.disabled"));
            return;
        }

        PlayerSaveData senderData = PlayerSaveData.get(sender);
        PlayerSaveData receiverData = PlayerSaveData.get(receiver);

        // Ensure the sender can marry (gender check / polyandry check)
        if (!senderData.canMarry(receiver)) {
            failMessage(sender, Component.translatable("server.alreadyMarried")); // Or a more specific message like
                                                                                  // "You cannot have more spouses"
            return;
        }

        // Ensure the receiver can marry
        if (!receiverData.canMarry(sender)) {
            failMessage(sender, Component.translatable("server.targetAlreadyMarried", receiver.getScoreboardName()));
            return;
        }

        // Ensure the sender isn't himself.
        if (sender == receiver) {
            failMessage(sender, Component.translatable("server.proposedToYourself"));
            return;
        }

        // Ensure the receiver hasn't already been proposed to by this player.
        if (hasProposalFrom(sender, receiver)) {
            failMessage(sender, Component.translatable("server.sentProposal", receiver.getScoreboardName()));
        } else {
            // Send the proposal messages.
            successMessage(sender, Component.translatable("server.proposalSent", receiver.getScoreboardName()));
            infoMessage(receiver, Component.translatable("server.proposedMarriage", sender.getScoreboardName()));

            // Add the proposal to the receiver's proposal list.
            List<UUID> list = getProposalsFor(receiver);
            list.add(sender.getUUID());
            proposals.put(receiver.getUUID(), list);
        }
    }

    /**
     * Rejects and removes a proposal from the receiver to the sender.
     *
     * @param sender   The person rejecting the proposal.
     * @param receiver The initial proposer.
     */
    public void rejectProposal(ServerPlayer sender, ServerPlayer receiver) {
        // Ensure a proposal existed.
        if (!hasProposalFrom(receiver, sender)) {
            failMessage(sender, Component.translatable("server.noProposal", receiver.getDisplayName()));
        } else {
            // Notify of the proposal failure and remove it.
            successMessage(sender, Component.translatable("server.proposalRejectionSent"));
            failMessage(receiver, Component.translatable("server.proposalRejected", sender.getScoreboardName()));
            removeProposalFor(sender, receiver);
        }
    }

    /**
     * Accepts and removes a proposal from the receiver to the sender.
     *
     * @param sender   The person accepting the proposal.
     * @param receiver The initial proposer.
     */
    public void acceptProposal(ServerPlayer sender, ServerPlayer receiver) {
        // Ensure a proposal is active.
        if (!hasProposalFrom(receiver, sender)) {
            failMessage(sender, Component.translatable("server.noProposal", receiver.getDisplayName()));
        } else {
            PlayerSaveData senderData = PlayerSaveData.get(sender);
            PlayerSaveData receiverData = PlayerSaveData.get(receiver);

            // Double check eligibility (race conditions)
            if (!senderData.canMarry(receiver)) {
                failMessage(sender, Component.translatable("server.alreadyMarried"));
                return;
            }
            if (!receiverData.canMarry(sender)) {
                failMessage(sender, Component.translatable("server.targetAlreadyMarried", receiver.getDisplayName()));
                return;
            }

            // Notify of acceptance.
            successMessage(receiver, Component.translatable("server.proposalAccepted", sender.getDisplayName()));

            // Set both player data as married.
            senderData.marry(receiver);
            receiverData.marry(sender);

            // Send success messages.
            successMessage(sender, Component.translatable("server.married", receiver.getDisplayName()));
            successMessage(receiver, Component.translatable("server.married", sender.getDisplayName()));

            // Remove the proposal.
            removeProposalFor(sender, receiver);
        }
    }

    /**
     * Ends the sender's marriage and notifies their spouse if the spouse is online.
     *
     * @param sender The person ending their marriage.
     */
    public void endMarriage(ServerPlayer sender) {
        // Retrieve all data instances and an instance of the ex-spouse if they are
        // present.
        EntityRelationship.of(sender).ifPresent(senderData -> {
            // Ensure the sender is married
            if (!senderData.isMarried()) {
                failMessage(sender, Component.translatable("server.endMarriageNotMarried"));
                return;
            }

            // Lookup the spouse, if it's a villager, we can't continue
            if (senderData.getRelationshipState() != RelationshipState.MARRIED_TO_PLAYER) {
                failMessage(sender, Component.translatable("server.marriedToVillager"));
                return;
            }

            // Notify the sender of the success and end both marriages.
            senderData.getPartnerName().ifPresent(
                    name -> successMessage(sender, Component.translatable("server.endMarriage", name.getString())));

            // Notify all partners
            senderData.getPartners().forEach(spouse -> {
                if (spouse instanceof Player player) {
                    // Notify the ex if they are online.
                    failMessage(player, Component.translatable("server.marriageEnded", sender.getScoreboardName()));
                    // Remove sender from spouse's partners
                    PlayerSaveData.get((ServerPlayer) player).getFamilyEntry().removePartner(sender.getUUID());
                }
            });

            // Let's iterate UUIDs for correctness.
            Set<UUID> partnerIds = new HashSet<>(senderData.getFamilyEntry().partners());
            for (UUID partnerId : partnerIds) {
                PlayerSaveData.getIfPresent(sender.getServer().overworld(), partnerId).ifPresent(partnerData -> {
                    partnerData.getFamilyEntry().removePartner(sender.getUUID());
                });
            }

            senderData.endRelationShip(RelationshipState.SINGLE); // This clears all partners
        });
    }

    public void endMarriage(ServerPlayer sender, String spouseName) {
        EntityRelationship.of(sender).ifPresent(senderData -> {
            if (!senderData.isMarried()) {
                failMessage(sender, Component.translatable("server.endMarriageNotMarried"));
                return;
            }

            // Find partner by name
            Optional<UUID> targetPartner = senderData.getFamilyEntry().partners().stream()
                    .filter(uuid -> {
                        // Try to resolve name
                        Optional<String> name = senderData.getFamilyTree().getOrEmpty(uuid)
                                .map(FamilyTreeNode::getName);
                        return name.isPresent() && name.get().equalsIgnoreCase(spouseName);
                    })
                    .findFirst();

            if (targetPartner.isPresent()) {
                UUID partnerId = targetPartner.get();

                // Remove from us
                senderData.getFamilyEntry().removePartner(partnerId);
                successMessage(sender, Component.translatable("server.endMarriage", spouseName));

                // Remove from them (if online/loaded)
                PlayerSaveData.getIfPresent(sender.getServer().overworld(), partnerId).ifPresent(partnerData -> {
                    partnerData.getFamilyEntry().removePartner(sender.getUUID());
                    if (partnerData instanceof PlayerSaveData psd && psd.getUUID() != null) {
                        ServerPlayer sp = sender.getServer().getPlayerList().getPlayer(psd.getUUID());
                        if (sp != null) {
                            failMessage(sp, Component.translatable("server.marriageEnded", sender.getScoreboardName()));
                        }
                    }
                });

            } else {
                failMessage(sender, Component.translatable("server.spouseNotPresent")); // Or "Spouse not found"
            }
        });
    }

    public void forceMarry(ServerPlayer source, ServerPlayer target1, ServerPlayer target2) {
        PlayerSaveData data1 = PlayerSaveData.get(target1);
        PlayerSaveData data2 = PlayerSaveData.get(target2);

        if (target1 == target2) {
            failMessage(source, Component.translatable("server.proposedToYourself"));
            return;
        }

        // Check eligibility
        if (!data1.canMarry(target2)) {
            failMessage(source,
                    Component.literal("Target 1 cannot marry Target 2 (Already married/Gender restrictions)"));
            return;
        }
        if (!data2.canMarry(target1)) {
            failMessage(source,
                    Component.literal("Target 2 cannot marry Target 1 (Already married/Gender restrictions)"));
            return;
        }

        // Marry them
        data1.marry(target2);
        data2.marry(target1);

        successMessage(source, Component.literal(
                "Forced marriage between " + target1.getScoreboardName() + " and " + target2.getScoreboardName()));
        successMessage(target1, Component.translatable("server.married", target2.getDisplayName()));
        successMessage(target2, Component.translatable("server.married", target1.getDisplayName()));
    }

    /**
     */
    public void procreate(ServerPlayer sender) {
        // Ensure the sender is married.
        PlayerSaveData senderData = PlayerSaveData.get(sender);
        if (!senderData.isMarried()) {
            failMessage(sender, Component.translatable("server.notMarried"));
            return;
        }

        // Ensure the spouse is a player
        if (senderData.getRelationshipState() != RelationshipState.MARRIED_TO_PLAYER) {
            failMessage(sender, Component.translatable("server.marriedToVillager"));
            return;
        }

        // Check procreation cooldown
        long currentTime = sender.getServer().overworld().getGameTime();
        if (lastProcreationTime.containsKey(sender.getUUID())) {
            long lastProcreation = lastProcreationTime.getLong(sender.getUUID());
            long timeSinceLastProcreation = currentTime - lastProcreation;
            int cooldown = Config.getInstance().playerProcreationCooldown;

            if (cooldown > 0 && timeSinceLastProcreation < cooldown) {
                long ticksRemaining = cooldown - timeSinceLastProcreation;
                int daysRemaining = (int) Math.ceil(ticksRemaining / 24000.0);
                failMessage(sender, Component.translatable("server.procreationCooldown", daysRemaining));
                return;
            }
        }

        // Ensure the spouse is online.
        senderData.getPartner().filter(e -> e instanceof Player).map(Player.class::cast).ifPresentOrElse(spouse -> {
            // If the spouse is online and has previously sent a procreation request that
            // hasn't expired, we can continue.
            // Otherwise, we notify the spouse that they must also enter the command.
            if (!procreateMap.containsKey(spouse.getUUID())) {
                procreateMap.put(sender.getUUID(), System.currentTimeMillis() + 10000);
                infoMessage(spouse, Component.translatable("server.procreationRequest", sender.getScoreboardName()));
            } else {
                // On success, add a randomly generated baby to the original requester.
                successMessage(sender, Component.translatable("server.procreationSuccessful"));
                successMessage(spouse, Component.translatable("server.procreationSuccessful"));

                spouse.addItem(BabyItem.createItem(spouse, sender, spouse.getRandom().nextLong()));

                // Record procreation time for cooldown tracking
                long gameTime = sender.getServer().overworld().getGameTime();
                lastProcreationTime.put(sender.getUUID(), gameTime);
                lastProcreationTime.put(spouse.getUUID(), gameTime);
            }
        }, () -> failMessage(sender, Component.translatable("server.spouseNotPresent")));
    }

    private void successMessage(Player player, MutableComponent message) {
        player.sendSystemMessage(message.withStyle(ChatFormatting.GREEN));
    }

    private void failMessage(Player player, MutableComponent message) {
        player.sendSystemMessage(message.withStyle(ChatFormatting.RED));
    }

    private void infoMessage(Player player, MutableComponent message) {
        player.sendSystemMessage(message.withStyle(ChatFormatting.YELLOW));
    }
}
