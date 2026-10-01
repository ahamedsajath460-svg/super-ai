package com.example.agent

import com.example.data.local.AppDatabase
import com.example.data.model.AiSettings
import com.example.data.model.GroundingSource
import com.example.data.model.ToolExecutionLog
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AgentExecutionResult(
    val replyText: String,
    val toolLogs: List<ToolExecutionLog> = emptyList(),
    val groundingSources: List<GroundingSource> = emptyList(),
    val isSuccess: Boolean = true
)

class AgentEngine(
    private val database: AppDatabase,
    private val geminiClient: GeminiClient = GeminiClient()
) {
    private val calculatorTool = CalculatorTool()
    private val dateTimeTool = DateTimeTool()
    private val memoryTool = MemoryTool(database.memoryDao())
    private val documentRagTool = DocumentRagTool(database.documentDao())
    private val codeAssistantTool = CodeAssistantTool()
    private val gmailTool = GmailTool()

    suspend fun executeTask(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>>,
        imageBase64: String? = null,
        attachedDocContent: String? = null,
        settings: AiSettings,
        onProgressUpdate: (String) -> Unit = {}
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val toolLogs = mutableListOf<ToolExecutionLog>()
        val promptLower = userPrompt.lowercase()

        // 1. Tool Selection & Execution
        if (settings.isAgentToolsEnabled) {
            // Check Date/Time
            if (promptLower.contains("time") || promptLower.contains("date") || promptLower.contains("today") || promptLower.contains("day is it") || promptLower.contains("what month") || promptLower.contains("current year")) {
                onProgressUpdate("Executing Tool: Date & Time...")
                val log = dateTimeTool.execute(userPrompt)
                toolLogs.add(log)
            }

            // Check Math / Calculation
            val isMathQuery = promptLower.contains("calculate") ||
                    promptLower.contains("sqrt") ||
                    promptLower.contains("%") ||
                    promptLower.matches(Regex(".*\\d+\\s*[+\\-*/xX^]\\s*\\d+.*")) ||
                    promptLower.contains("solve") ||
                    promptLower.contains("plus") ||
                    promptLower.contains("divided by")

            if (isMathQuery) {
                onProgressUpdate("Executing Tool: Math Calculator...")
                val log = calculatorTool.execute(userPrompt)
                toolLogs.add(log)
            }

            // Check Long-term Memory
            if (settings.isMemoryEnabled && (promptLower.contains("remember") || promptLower.contains("my favorite") || promptLower.contains("who am i") || promptLower.contains("preference") || promptLower.contains("what do you know about me") || promptLower.contains("recall"))) {
                onProgressUpdate("Executing Tool: Long-term Memory Lookup...")
                val log = memoryTool.execute(userPrompt)
                toolLogs.add(log)
            }

            // Check Document Knowledge Base / RAG
            if (promptLower.contains("document") || promptLower.contains("file") || promptLower.contains("rag") || promptLower.contains("knowledge base") || attachedDocContent != null) {
                onProgressUpdate("Executing Tool: Document RAG Engine...")
                val query = attachedDocContent?.take(200) ?: userPrompt
                val log = documentRagTool.execute(query)
                toolLogs.add(log)
            }

            // Check Code Assistant
            if (promptLower.contains("code") || promptLower.contains("python") || promptLower.contains("kotlin") || promptLower.contains("function") || promptLower.contains("class") || promptLower.contains("bug")) {
                onProgressUpdate("Executing Tool: Code Assistant...")
                val log = codeAssistantTool.execute(userPrompt)
                toolLogs.add(log)
            }

            // Check Gmail / Email Assistant
            if (promptLower.contains("email") || promptLower.contains("gmail") || promptLower.contains("inbox") || promptLower.contains("draft email") || promptLower.contains("send email") || promptLower.contains("mail")) {
                onProgressUpdate("Executing Tool: Gmail Assistant...")
                val log = gmailTool.execute(userPrompt)
                toolLogs.add(log)
            }
        }

        // 2. Synthesize System Instruction with Agent Observations & Memories
        val augmentedSystemPrompt = buildString {
            append(settings.systemInstruction)
            append("\n\nYou are Super AI, equipped with autonomous agent reasoning, code analysis, real-time Google search grounding, and local memory.")

            if (toolLogs.isNotEmpty()) {
                append("\n\n=== AGENT TOOL EXECUTION OBSERVATIONS ===")
                for (log in toolLogs) {
                    append("\n[Tool: ${log.toolName}] -> ${log.output}")
                }
                append("\nIntegrate these observations into your final, natural response.")
            }

            if (!attachedDocContent.isNullOrBlank()) {
                append("\n\n=== ATTACHED DOCUMENT CONTENT ===\n")
                append(attachedDocContent)
                append("\nAnswer the user query based on this document.")
            }
        }

        onProgressUpdate("Thinking with ${settings.selectedModel}...")

        // 3. Model Call (Gemini REST with Search Grounding)
        val response = geminiClient.generateResponse(
            modelName = settings.selectedModel,
            systemInstruction = augmentedSystemPrompt,
            conversationHistory = conversationHistory,
            currentPrompt = userPrompt,
            imageBase64 = imageBase64,
            enableSearchGrounding = settings.isSearchGroundingEnabled,
            temperature = settings.temperature,
            maxTokens = settings.maxTokens,
            customApiKey = settings.customApiKey.takeIf { it.isNotBlank() }
        )

        AgentExecutionResult(
            replyText = response.text,
            toolLogs = toolLogs,
            groundingSources = response.groundingSources,
            isSuccess = response.isSuccess
        )
    }
}
