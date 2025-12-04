package net.conczin.mca.network.c2s;

import net.conczin.mca.MCA;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.network.HandleablePayload;
import net.conczin.mca.network.Network;
import net.conczin.mca.network.s2c.InteractionDialogueResponse;
import net.conczin.mca.resources.Dialogues;
import net.conczin.mca.resources.data.dialogue.Question;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import net.conczin.mca.entity.interaction.Constraint;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

public record InteractionDialogueInitMessage(UUID villagerUUID) implements HandleablePayload {
    public static final CustomPacketPayload.Type<InteractionDialogueInitMessage> TYPE = new CustomPacketPayload.Type<>(
            MCA.locate("interaction_dialogue_init"));
    public static final StreamCodec<FriendlyByteBuf, InteractionDialogueInitMessage> STREAM_CODEC = StreamCodec
            .composite(
                    UUIDUtil.STREAM_CODEC, InteractionDialogueInitMessage::villagerUUID,
                    InteractionDialogueInitMessage::new);

    @Override
    public void handleServer(ServerPlayer player) {
        Entity v = player.serverLevel().getEntity(villagerUUID);
        if (v instanceof VillagerEntityMCA villager) {
            Question question = Dialogues.getInstance().getQuestion("root");

            if (question != null) {
                if (question.isAuto()) {
                    Dialogues.getInstance().selectAnswer(villager, player, question.getName(),
                            question.getRandomAnswer().getName());
                } else {
                    // Debug logging
                    Set<Constraint> matchingConstraints = Constraint.allMatching(villager, player);
                    MCA.LOGGER.info("Matching constraints for player {}: {}", player.getName().getString(),
                            matchingConstraints.stream().map(Enum::name).collect(Collectors.joining(", ")));

                    List<String> validAnswers = question.getValidAnswers(player, villager);
                    MCA.LOGGER.info("Valid answers for root: {}", String.join(", ", validAnswers));

                    InteractionDialogueResponse response = new InteractionDialogueResponse(question, player,
                            villager);
                    Network.sendToPlayer(response, player);
                }
            }
        }
    }

    @Override
    public Type<InteractionDialogueInitMessage> type() {
        return TYPE;
    }
}
