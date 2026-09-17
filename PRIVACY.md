# Privacy Policy — Mario AI Debug Assistant

Effective date: 18 September 2026. Contact: mrlshinian@gmail.com.

Mario AI Debug Assistant is an IntelliJ IDEA plugin. The developer does not operate a server for this plugin and does not receive telemetry or account information from it.

## Data handled by the plugin

- Settings such as provider, endpoint, model, and LogQL query are stored locally by the IDE. AI API keys and Loki bearer tokens are stored in IntelliJ Password Safe.
- When you explicitly start an analysis and confirm the data-transfer dialog, the plugin may send your problem description, current editor selection or file content, stack-trace snippets, Git diff, trace identifiers, and retrieved Loki logs to the AI endpoint you configured.
- When you explicitly request a repair patch and confirm again, the same context may be sent to that endpoint.
- When you refresh the model list, the plugin contacts the configured AI provider. When an analysis uses Loki, the plugin sends the configured LogQL query to the configured Loki endpoint and receives matching logs.

No AI analysis or Loki query is started automatically in the background. The plugin does not store AI prompts, responses, or Loki logs in its persistent settings; they remain visible in the current IDE session. The plugin does not intentionally add its own telemetry.

Your configured AI and Loki services may process, retain, or log data under their own policies. You are responsible for choosing trusted endpoints and checking code and logs for secrets or personal data before confirming a transfer. Avoid unencrypted HTTP for remote services; local Ollama installations may use HTTP on localhost.

To stop future transfers, do not use the analysis/model-refresh actions, remove the configured endpoints and credentials, or uninstall the plugin. For requests about the plugin developer's practices, contact the email above. For data held by an AI or Loki provider, contact that provider.
