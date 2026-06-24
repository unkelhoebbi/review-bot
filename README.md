# azure-bearer-request

Minimaler Single-File-Client auf Basis des [Azure SDK for JS](https://github.com/Azure/azure-sdk-for-js).
Ruft eine **konfigurierbare URL** mit einem **bestehenden Bearer-Token** auf.

## Setup

```bash
npm install
```

## Verwendung

URL und Token sind per CLI-Argument oder Umgebungsvariable konfigurierbar
(Argumente haben Vorrang):

```bash
# Variante 1: Argumente
npm start -- https://deine-api.example.com/pfad DEIN_BEARER_TOKEN

# Variante 2: Umgebungsvariablen
TARGET_URL=https://deine-api.example.com/pfad AUTH_TOKEN=DEIN_BEARER_TOKEN npm start
```

## Auth

Es wird ein bereits vorhandener Token verwendet (keine neue Token-Beschaffung).
Eine kleine Pipeline-Policy setzt bei jedem Request den Header
`Authorization: Bearer <token>`.
