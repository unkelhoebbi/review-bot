import ModelClient, { isUnexpected } from "@azure-rest/ai-inference";

// --- Konfiguration -------------------------------------------------------
// Endpoint, Token, Modell und die Frage sind konfigurierbar.
// CLI-Argumente haben Vorrang vor Umgebungsvariablen:
//   npm run chat -- "<deine frage>"
// mit:
//   TARGET_URL=https://<modell-endpoint>   (Basis-Endpoint, ohne /chat/completions)
//   AUTH_TOKEN=<bestehender bearer token>
//   MODEL=<modellname>                     (optional, je nach Endpoint noetig)
const endpoint = process.env.TARGET_URL;
const token = process.env.AUTH_TOKEN;
const model = process.env.MODEL;
const deployment = process.env.DEPLOYMENT;     // optional, fuer Azure OpenAI
const apiVersion = process.env.API_VERSION;    // optional, z.B. 2024-08-01-preview
const prompt = process.argv[2] ?? "Sag in einem Satz Hallo.";

if (!endpoint || !token) {
  console.error(
    "Fehlende Konfiguration:\n" +
      "  TARGET_URL : Basis-Endpoint des Modells (ohne /chat/completions)\n" +
      "  AUTH_TOKEN : bestehender Bearer-Token\n" +
      "  MODEL      : (optional) Modellname\n\n" +
      'Beispiel:\n  TARGET_URL=https://my-endpoint AUTH_TOKEN=token npm run chat -- "Hallo!"'
  );
  process.exit(1);
}

// --- Auth: bestehenden Bearer-Token als statische TokenCredential --------
// ModelClient akzeptiert eine TokenCredential und setzt daraus den Header
// `Authorization: Bearer <token>`. Da der Token bereits existiert, gibt
// getToken() ihn einfach unveraendert zurueck (Scope ist irrelevant).
const credential = {
  getToken: async () => ({
    token,
    expiresOnTimestamp: Date.now() + 60 * 60 * 1000,
  }),
};

// api-version kann global am Client gesetzt werden:
const client = ModelClient(endpoint, credential, {
  ...(apiVersion ? { apiVersion } : {}),
});

// --- Minimale Chat Completion -------------------------------------------
// deployment -> Pfad (Azure OpenAI). Ohne deployment der generische Pfad.
const path = deployment
  ? `/openai/deployments/${deployment}/chat/completions`
  : "/chat/completions";

const response = await client.path(path).post({
  // api-version kann alternativ auch pro Request gesetzt werden:
  ...(apiVersion ? { queryParameters: { "api-version": apiVersion } } : {}),
  body: {
    messages: [{ role: "user", content: prompt }],
    ...(model ? { model } : {}),
  },
});

if (isUnexpected(response)) {
  throw response.body.error;
}

console.log(response.body.choices[0].message.content);
