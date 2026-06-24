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

const client = ModelClient(endpoint, credential);

// --- Minimale Chat Completion -------------------------------------------
const response = await client.path("/chat/completions").post({
  body: {
    messages: [{ role: "user", content: prompt }],
    ...(model ? { model } : {}),
  },
});

if (isUnexpected(response)) {
  throw response.body.error;
}

console.log(response.body.choices[0].message.content);
