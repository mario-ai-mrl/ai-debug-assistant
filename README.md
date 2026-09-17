# AI Debug Assistant

IntelliJ IDEA plugin MVP for analyzing local code and Loki logs with DeepSeek or another OpenAI-compatible model.

## Run

Requires JDK 21 and Gradle.

```powershell
gradle runIde
```

In the sandbox IDE, open **View | Tool Windows | AI Debug Assistant**. The first version reads the current editor selection (or current file), optionally queries Loki with LogQL, and sends the context to the configured AI endpoint.

## Build

```powershell
gradle compileKotlin
gradle buildPlugin
```

The plugin archive is generated under `build/distributions/`.

## Git

The repository ignores IDEA metadata, Gradle caches, generated IntelliJ platform files, build output, plugin ZIPs, and local credentials. Copy `.env.example` only as a reference; API keys are configured inside IDEA and stored with Password Safe.

## Current configuration

The settings service has fields for:

- OpenAI-compatible AI base URL, API key, and model
- Loki base URL and bearer token
- Loki LogQL query

The next implementation step is to expose these fields in an IDEA Settings page and move API keys to Password Safe before sharing the plugin.
