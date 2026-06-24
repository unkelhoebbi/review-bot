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
import com.azure.core.credential.AzureKeyCredential;
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
 *   AZURE_OPENAI_KEY        API-Key (Variante 1) -- alternativ:
 *   AUTH_TOKEN              bestehender Bearer-/Entra-ID-Token (Variante 2)
 *
 * Die Frage kommt als erstes CLI-Argument:
 *   mvn -q compile exec:java -Dexec.args="Sag in einem Satz Hallo."
 */
public class ChatApp {

    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String deployment = System.getenv("DEPLOYMENT");
        String apiKey = System.getenv("AZURE_OPENAI_KEY");
        String token = System.getenv("AUTH_TOKEN");
        String prompt = args.length > 0 ? args[0] : "Sag in einem Satz Hallo.";

        if (endpoint == null || deployment == null) {
            System.err.println("Fehlende Konfiguration:\n"
                    + "  AZURE_OPENAI_ENDPOINT : Basis-Endpoint (https://<resource>.openai.azure.com)\n"
                    + "  DEPLOYMENT            : Deployment-Name aus dem Azure-Portal\n"
                    + "  AZURE_OPENAI_KEY      : API-Key  -- ODER --\n"
                    + "  AUTH_TOKEN            : bestehender Bearer-Token");
            System.exit(1);
        }

        // --- Client bauen: API-Key bevorzugt, sonst statischer Bearer-Token ---
        OpenAIClientBuilder builder = new OpenAIClientBuilder().endpoint(endpoint);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.credential(new AzureKeyCredential(apiKey));
        } else if (token != null && !token.isBlank()) {
            // Bestehenden Token unveraendert durchreichen (keine neue Beschaffung).
            TokenCredential staticToken =
                    requestContext -> Mono.just(new AccessToken(token, OffsetDateTime.now().plusHours(1)));
            builder.credential(staticToken);
        } else {
            System.err.println("Weder AZURE_OPENAI_KEY noch AUTH_TOKEN gesetzt.");
            System.exit(1);
            return;
        }

        OpenAIClient client = builder.buildClient();

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
