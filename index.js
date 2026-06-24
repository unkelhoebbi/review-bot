import { getClient } from "@azure-rest/core-client";

// --- Konfiguration -------------------------------------------------------
// URL und Token koennen per Umgebungsvariable ODER per CLI-Argument gesetzt
// werden. CLI-Argumente haben Vorrang:
//   npm start -- <url> <bearer-token>
// oder:
//   TARGET_URL=... AUTH_TOKEN=... npm start
const [, , urlArg, tokenArg] = process.argv;

const url = urlArg ?? process.env.TARGET_URL;
const token = tokenArg ?? process.env.AUTH_TOKEN;

if (!url || !token) {
  console.error(
    "Fehlende Konfiguration.\n" +
      "  URL  : per Argument oder Umgebungsvariable TARGET_URL\n" +
      "  Token: per Argument oder Umgebungsvariable AUTH_TOKEN\n\n" +
      "Beispiel:\n" +
      "  npm start -- https://example.com/api dein-bearer-token\n" +
      "  TARGET_URL=https://example.com/api AUTH_TOKEN=token npm start"
  );
  process.exit(1);
}

// --- Auth: bestehenden Bearer-Token verwenden ---------------------------
// Statt einer Credential, die selbst Tokens beschafft, reichen wir den
// vorhandenen Token direkt als Authorization-Header durch.
const client = getClient(url, {
  additionalPolicies: [
    {
      position: "perCall",
      policy: {
        name: "staticBearerTokenPolicy",
        sendRequest: (request, next) => {
          request.headers.set("Authorization", `Bearer ${token}`);
          return next(request);
        },
      },
    },
  ],
});

// --- Request ausfuehren --------------------------------------------------
const response = await client.pathUnchecked("").get();

console.log(`Status: ${response.status}`);
console.log("Body:");
console.log(
  typeof response.body === "string"
    ? response.body
    : JSON.stringify(response.body, null, 2)
);
