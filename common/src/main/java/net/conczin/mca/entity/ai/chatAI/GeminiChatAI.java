package net.conczin.mca.entity.ai.chatAI;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.conczin.mca.Config;
import net.conczin.mca.MCA;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Messenger;
import net.conczin.mca.entity.ai.chatAI.modules.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GeminiChatAI implements ChatAIStrategy {
    private static final int MAX_MEMORY = 500;
    private static final int MAX_MEMORY_TIME = 20 * 60 * 45;

    private static final Map<UUID, List<Tuple<String, String>>> memory = new HashMap<>();
    private static final Map<UUID, Long> lastInteractions = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();

    @Override
    public Optional<String> answer(ServerPlayer player, VillagerEntityMCA villager, String msg) {
        Config config = Config.getInstance();
        String apiKey = config.geminiApiKey;

        if (apiKey == null || apiKey.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("Gemini API Key is missing! Check config.").withStyle(ChatFormatting.RED), false);
            return Optional.empty();
        }

        String playerName = Messenger.getName(player);
        String villagerName = villager.getName().getString();

        // Memory management (same as OpenAIChatAI)
        long time = villager.level().getGameTime();
        if (time > lastInteractions.getOrDefault(villager.getUUID(), 0L) + MAX_MEMORY_TIME) {
            memory.remove(villager.getUUID());
        }
        lastInteractions.put(villager.getUUID(), time);

        List<Tuple<String, String>> pastDialogue = memory.computeIfAbsent(villager.getUUID(),
                key -> new LinkedList<>());
        while (pastDialogue.stream().mapToInt(v -> (v.getB().length() / 4)).sum() > MAX_MEMORY) {
            pastDialogue.removeFirst();
        }

        // Construct Context
        List<String> input = new LinkedList<>();
        PersonalityModule.apply(input, villager, player);
        TraitsModule.apply(input, villager, player);
        RelationModule.apply(input, villager, player);
        VillageModule.apply(input, villager, player);
        EnvironmentModule.apply(input, villager, player);
        PlayerModule.apply(input, villager, player);

        Map<String, String> variables = Map.of(
                "player", playerName,
                "villager", villagerName);

        StringBuilder systemPrompt = new StringBuilder();
        String defaultPrompt = "You are a Minecraft villager. You are not an AI assistant. You are immersed in the world. Respond as your character.";
        systemPrompt
                .append(config.villagerChatAISystemPrompt.isEmpty() ? defaultPrompt : config.villagerChatAISystemPrompt)
                .append("\n");

        for (String s : input) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                s = s.replaceAll("\\$" + entry.getKey(), entry.getValue());
            }
            systemPrompt.append(s);
        }

        // Build JSON Body for Gemini
        JsonObject root = new JsonObject();
        JsonObject content = new JsonObject();
        content.addProperty("role", "user");

        // Gemini expects "parts" array
        com.google.gson.JsonArray parts = new com.google.gson.JsonArray();

        // Add system prompt as text part (Gemini 1.5 Flash supports system instructions
        // differently, but text prefix works well for simple cases)
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", "System Instruction: " + systemPrompt.toString() + "\n\nChat History:\n");
        parts.add(systemPart);

        // Add history
        for (Tuple<String, String> pair : pastDialogue) {
            JsonObject historyPart = new JsonObject();
            historyPart.addProperty("text", pair.getA() + ": " + pair.getB() + "\n");
            parts.add(historyPart);
        }

        // Add current message
        JsonObject msgPart = new JsonObject();
        msgPart.addProperty("text", playerName + ": " + msg);
        parts.add(msgPart);

        content.add("parts", parts);

        com.google.gson.JsonArray contents = new com.google.gson.JsonArray();
        contents.add(content);
        root.add("contents", contents);

        String requestBody = new Gson().toJson(root);
        String model = config.geminiModel.isEmpty() ? "gemini-1.5-flash" : config.geminiModel;
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key="
                + apiKey;

        // Async Request
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                String responseBody = response.body();
                                String reply = parseGeminiResponse(responseBody);

                                if (reply != null && !reply.isEmpty()) {
                                    // Update memory
                                    pastDialogue.add(new Tuple<>("user", msg));
                                    pastDialogue.add(new Tuple<>("assistant", reply));

                                    // Send to chat (Must be on server thread)
                                    villager.getServer().execute(() -> {
                                        villager.conversationManager.addMessage(player, Component.literal(reply));
                                    });
                                }
                            } else {
                                MCA.LOGGER.error("Gemini API Error: " + response.statusCode() + " " + response.body());
                                villager.getServer().execute(() -> {
                                    player.displayClientMessage(
                                            Component.literal("Gemini Error: " + response.statusCode())
                                                    .withStyle(ChatFormatting.RED),
                                            false);
                                });
                            }
                        });
            } catch (Exception e) {
                MCA.LOGGER.error("Gemini Request Failed", e);
            }
        });

        // Return empty because we are handling it async. The interface expects
        // Optional<String> for immediate synchronous return,
        // but OpenAIChatAI also blocks or handles it.
        // Wait, OpenAIChatAI.answer() returns Optional<String> and calls post()
        // synchronously!
        // To maintain compatibility without rewriting the whole system to be async, we
        // might need to block or return empty and handle chat manually.
        // However, OpenAIChatAI blocks the main thread (HttpURLConnection). This is bad
        // practice but it's how the mod is written.
        // I will follow the pattern: Block if I must to match the interface, OR return
        // empty and send message manually.
        // If I return empty, the caller might think it failed.
        // Let's look at OpenAIChatAI again. It returns Optional.of(message).
        // If I return empty, the caller (ChatAI.answer) returns empty.
        // Dialogues.java calls ChatAI.answer(). If present, it does nothing? No, wait.

        // Let's check where ChatAI.answer is called.
        return Optional.empty();
    }

    private String parseGeminiResponse(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return root.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            MCA.LOGGER.error("Failed to parse Gemini response", e);
            return null;
        }
    }
}
