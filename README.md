# azure-openai-chat

Minimale **Chat Completion** gegen Azure OpenAI in **plain Java**, mit dem
[`com.azure:azure-ai-openai`](https://central.sonatype.com/artifact/com.azure/azure-ai-openai)
SDK aus Maven Central.

## Voraussetzungen

- Java 17+
- Maven 3.9+

## Konfiguration

Per Umgebungsvariable:

- `AZURE_OPENAI_ENDPOINT` – Basis-Endpoint, z. B. `https://<resource>.openai.azure.com`
- `DEPLOYMENT` – Deployment-Name aus dem Azure-Portal (z. B. `gpt-4o`)
- `AZURE_OPENAI_KEY` – API-Key **oder**
- `AUTH_TOKEN` – bestehender Bearer-/Entra-ID-Token

Die Frage wird als CLI-Argument übergeben.

## Verwendung

```bash
AZURE_OPENAI_ENDPOINT=https://<resource>.openai.azure.com \
DEPLOYMENT=gpt-4o \
AZURE_OPENAI_KEY=<api-key> \
mvn -q compile exec:java -Dexec.args="Deine Frage hier"
```

Auth-Variante 2 (bestehender Bearer-Token statt API-Key):

```bash
AZURE_OPENAI_ENDPOINT=https://<resource>.openai.azure.com \
DEPLOYMENT=gpt-4o \
AUTH_TOKEN=<bearer-token> \
mvn -q compile exec:java -Dexec.args="Deine Frage hier"
```

## Aufbau

- `pom.xml` – Maven-Build mit `azure-ai-openai`-Dependency
- `src/main/java/com/example/chat/ChatApp.java` – Chat Completion + Auswertung der Response
  (Antwort-Text, Finish-Reason, Token-Usage)
