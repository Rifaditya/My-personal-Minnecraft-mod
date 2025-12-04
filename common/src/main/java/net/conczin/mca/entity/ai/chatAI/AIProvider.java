package net.conczin.mca.entity.ai.chatAI;

/**
 * Enum representing the available AI providers for villager chat.
 */
public enum AIProvider {
    /**
     * Default MCA AI (OpenAI/Conczin backend).
     */
    DEFAULT,

    /**
     * Google Gemini API.
     */
    GEMINI,

    /**
     * Inworld AI (requires resource name configuration).
     */
    INWORLD
}
