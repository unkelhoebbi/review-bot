package com.example.chat;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Minimale Chat Completion gegen Azure OpenAI mit dem Azure-AI-OpenAI-SDK.
 *
 * Konfiguration ueber Umgebungsvariablen:
 *   AZURE_OPENAI_ENDPOINT   Basis-Endpoint, z.B. https://<resource>.openai.azure.com
 *   DEPLOYMENT              Deployment-Name aus dem Azure-Portal (z.B. gpt-4o)
 *   AUTH_TOKEN              bestehender Bearer-/Entra-ID-Token
 *
 * Die Frage kommt als erstes CLI-Argument:
 *   mvn -q compile exec:java -Dexec.args="Sag in einem Satz Hallo."
 */
public class ChatApp {

    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String deployment = System.getenv("DEPLOYMENT");
        String token = System.getenv("AUTH_TOKEN");
        String prompt = args.length > 0 ? args[0] : "Sag in einem Satz Hallo.";

        if (endpoint == null || deployment == null || token == null || token.isBlank()) {
            System.err.println("Fehlende Konfiguration:\n"
                    + "  AZURE_OPENAI_ENDPOINT : Basis-Endpoint (https://<resource>.openai.azure.com)\n"
                    + "  DEPLOYMENT            : Deployment-Name aus dem Azure-Portal\n"
                    + "  AUTH_TOKEN            : bestehender Bearer-Token");
            System.exit(1);
        }

        // --- Client bauen: bestehenden Bearer-Token unveraendert durchreichen ---
        // (keine neue Token-Beschaffung, kein API-Key)
        TokenCredential staticToken =
                requestContext -> Mono.just(new AccessToken(token, OffsetDateTime.now().plusHours(1)));

        OpenAIClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(staticToken)
                .buildClient();

        // --- Chat Completion ----------------------------------------------
        List<ChatRequestMessage> messages = List.of(new ChatRequestUserMessage(prompt));
        ChatCompletions completions =
                client.getChatCompletions(deployment, new ChatCompletionsOptions(messages));

        // --- Response verarbeiten -----------------------------------------
        for (ChatChoice choice : completions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.println("Antwort: " + message.getContent());
            System.out.println("Finish-Reason: " + choice.getFinishReason());
        }

        CompletionsUsage usage = completions.getUsage();
        if (usage != null) {
            System.out.printf("Tokens: prompt=%d, completion=%d, total=%d%n",
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
    }
}
