# Mario AI Debug Assistant

An IntelliJ IDEA plugin for investigating bugs with local code, stack traces, Git changes, optional Loki logs, and an AI provider you configure.

## Features

- Analyze the current editor selection or file, stack-trace code snippets, and working-tree Git diff.
- Query Loki with LogQL and include recent matching logs when configured.
- Use DeepSeek, OpenAI, Ollama, or a custom OpenAI-compatible endpoint; refresh the provider's model list.
- Ask for a unified-diff repair suggestion, preview it, and apply it only after a separate confirmation.
- Store API keys and Loki tokens in IntelliJ Password Safe.

## Requirements

- IntelliJ IDEA 2025.1 or newer (platform build 251+).
- JDK 21 to build the plugin.
- Your own AI endpoint and credentials. Loki and Git are optional; Git must be on `PATH` to collect diffs or apply patches.

## Configure and use

1. In IntelliJ IDEA, open **Settings → Tools → AI Debug Assistant**. Select a provider, enter its base URL, API key and model. For Ollama, use a running local server; no API key is needed for model discovery.
2. Optionally enter a Loki base URL, bearer token, and default LogQL query.
3. Open **View → Tool Windows → AI Debug Assistant**, enter the problem or stack trace, and click **开始分析**. You can also open the tool window from the editor context menu.
4. Read the data-transfer confirmation before proceeding. Check the result and, if desired, generate and preview a patch. Applying a patch requires another confirmation.

The plugin sends code, stack traces, Git diff, and any retrieved Loki logs only when you initiate an AI action and confirm the transfer. These materials can contain secrets or personal information. Review your chosen provider's terms and your organization's policies first. Model refresh sends a request to the configured provider; Loki queries go to the configured Loki endpoint. The plugin does not operate an account or telemetry server. See [Privacy](PRIVACY.md).

## Build and verify

Use the included Gradle wrapper:

```powershell
.\gradlew.bat buildPlugin verifyPluginProjectConfiguration verifyPluginStructure
.\gradlew.bat verifyPlugin
```

The installable ZIP is generated in `build/distributions/`. To test in an isolated IDE, run `.\gradlew.bat runIde`.

## Release

The plugin is free to use under the [license](LICENSE.md); the source repository may remain private. Before Marketplace submission, create a vendor profile, accept the Marketplace Developer Agreement, provide the EULA and privacy policy, sign the ZIP with a certificate kept outside Git, and manually inspect the final ZIP and sandbox behavior. The first publication must be uploaded manually. Do not upload credentials or local `.env` files.

Contact: mrlshinian@gmail.com
