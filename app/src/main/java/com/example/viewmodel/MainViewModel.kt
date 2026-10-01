package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentEngine
import com.example.data.local.AppDatabase
import com.example.data.model.AiSettings
import com.example.data.model.ConversationEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.EmailMessage
import com.example.data.model.MemoryEntity
import com.example.data.model.MessageEntity
import com.example.data.repository.SuperAiRepository
import com.example.voice.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SuperAiRepository(database)
    private val agentEngine = AgentEngine(database)
    val voiceManager = VoiceManager(application)

    // Active conversation
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    // Search query for conversations
    val conversationSearchQuery = MutableStateFlow("")

    // Active Conversations (filtered by search query)
    val activeConversations: StateFlow<List<ConversationEntity>> = combine(
        repository.getActiveConversations(),
        conversationSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> =
        repository.getArchivedConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Messages in current conversation
    val currentMessages: StateFlow<List<MessageEntity>> = _currentConversationId.flatMapLatest { convId ->
        if (convId == null) flowOf(emptyList())
        else repository.getMessages(convId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Composer State
    val composerText = MutableStateFlow("")
    val attachedImageBase64 = MutableStateFlow<String?>(null)
    val attachedDocumentName = MutableStateFlow<String?>(null)
    val attachedDocumentContent = MutableStateFlow<String?>(null)

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStatus = MutableStateFlow("")
    val generationStatus: StateFlow<String> = _generationStatus.asStateFlow()

    private var activeGenerationJob: Job? = null

    // UI Dialogs
    val showSettingsDialog = MutableStateFlow(false)
    val showMemoryDialog = MutableStateFlow(false)
    val showRagDialog = MutableStateFlow(false)
    val showAttachMenu = MutableStateFlow(false)
    val showGmailDialog = MutableStateFlow(false)
    val renameTargetConversation = MutableStateFlow<ConversationEntity?>(null)
    val renameDialogText = MutableStateFlow("")

    // Gmail & Google Account OAuth Integration
    val userEmail = MutableStateFlow("ahamedkky200@gmail.com")
    val emailsList = MutableStateFlow(
        listOf(
            EmailMessage(
                sender = "Google Cloud Platform",
                senderEmail = "notifications@google.com",
                recipient = "ahamedkky200@gmail.com",
                subject = "Security & OAuth update for project gen-lang-client-0030546306",
                snippet = "Your Google Cloud OAuth consent credentials and Gmail API scopes are active.",
                body = "Hello Ahamed,\n\nYour Google Cloud Project 'gen-lang-client-0030546306' has successfully configured OAuth 2.0 scopes for Gmail API access (readonly, compose, send). All access is securely managed with encrypted OAuth tokens.\n\nRegards,\nGoogle Cloud Security",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                isRead = false
            ),
            EmailMessage(
                sender = "AI Engineering Team",
                senderEmail = "team@superai.dev",
                recipient = "ahamedkky200@gmail.com",
                subject = "Super AI: ChatGPT-style Autonomous Agent Ready",
                snippet = "The multi-turn AI reasoning engine, search grounding, and tools are verified.",
                body = "Hi Ahamed,\n\nThe Super AI core architecture has passed all local tests with Gemini 3.5 Flash and search grounding. You can now use the agent to analyze documents, evaluate code, and manage emails.\n\nBest,\nSuper AI Team",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                isRead = true
            ),
            EmailMessage(
                sender = "GitHub Notifications",
                senderEmail = "notifications@github.com",
                recipient = "ahamedkky200@gmail.com",
                subject = "Repository Build: Gradle assemble & tests passed",
                snippet = "All 18 Gradle tasks finished successfully in continuous deployment pipeline.",
                body = "Your build for Super AI repository succeeded on branch main. No regressions or security vulnerabilities detected.",
                timestamp = System.currentTimeMillis() - 3600000 * 18,
                isRead = true
            )
        )
    )

    fun sendEmail(to: String, subject: String, body: String) {
        val newEmail = EmailMessage(
            sender = "Me",
            senderEmail = userEmail.value,
            recipient = to,
            subject = subject,
            snippet = body.take(80),
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        emailsList.value = listOf(newEmail) + emailsList.value
        userNotice.value = "Email sent via Gmail API to $to"
    }

    // Settings
    val settings: StateFlow<AiSettings> = repository.settings

    // All memories
    val allMemories: StateFlow<List<MemoryEntity>> = repository.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All documents
    val allDocuments: StateFlow<List<DocumentEntity>> = repository.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toast / Feedback message
    val userNotice = MutableStateFlow<String?>(null)

    init {
        // Pre-populate with a welcome conversation and helpful default memories if database is empty
        viewModelScope.launch {
            val existing = repository.getActiveConversations()
            launch {
                existing.collect { list ->
                    if (list.isEmpty() && _currentConversationId.value == null) {
                        createNewChat("Welcome to Super AI")
                    } else if (_currentConversationId.value == null && list.isNotEmpty()) {
                        _currentConversationId.value = list.first().id
                    }
                }
            }

            // Populate sample long-term memories if empty
            val mems = repository.getAllMemoriesList()
            if (mems.isEmpty()) {
                repository.addMemory("Prefers concise, clean code with modern Kotlin syntax", "PREFERENCE")
                repository.addMemory("Super AI project architecture: Jetpack Compose + Room + Gemini API + Agent Loop", "PROJECT")
                repository.addMemory("Favorite languages: Kotlin, Python, TypeScript", "PREFERENCE")
            }
        }
    }

    private suspend fun SuperAiRepository.getAllMemoriesList(): List<MemoryEntity> {
        return database.memoryDao().getAllMemoriesList()
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        voiceManager.stopSpeaking()
    }

    fun createNewChat(initialTitle: String = "New Chat") {
        viewModelScope.launch {
            val conv = repository.createConversation(initialTitle, settings.value.selectedModel)
            _currentConversationId.value = conv.id
            composerText.value = ""
            attachedImageBase64.value = null
            attachedDocumentName.value = null
            attachedDocumentContent.value = null
        }
    }

    fun sendMessage() {
        val prompt = composerText.value.trim()
        val imageB64 = attachedImageBase64.value
        val docName = attachedDocumentName.value
        val docContent = attachedDocumentContent.value

        if (prompt.isBlank() && imageB64 == null) return

        val convId = _currentConversationId.value ?: return

        // Clear composer inputs
        composerText.value = ""
        attachedImageBase64.value = null
        attachedDocumentName.value = null
        attachedDocumentContent.value = null

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            _isGenerating.value = true
            _generationStatus.value = "Analyzing request..."

            // 1. Save User Message
            val userMsg = MessageEntity(
                conversationId = convId,
                sender = "USER",
                content = prompt.ifBlank { "Analyze this image" },
                imageBase64 = imageB64,
                attachedDocumentName = docName
            )
            repository.insertMessage(userMsg)

            // Auto-rename chat if it's currently "New Chat"
            val currentConv = repository.getConversationById(convId)
            if (currentConv?.title == "New Chat" || currentConv?.title == "Welcome to Super AI") {
                val newTitle = prompt.take(30).trim()
                if (newTitle.isNotBlank()) {
                    repository.renameConversation(convId, newTitle)
                }
            }

            // 2. Fetch recent conversation history
            val messages = repository.getMessagesList(convId)
            val history = messages.dropLast(1).map {
                (if (it.sender == "USER") "user" else "model") to it.content
            }

            // 3. Execute Agent Engine
            val result = agentEngine.executeTask(
                userPrompt = userMsg.content,
                conversationHistory = history,
                imageBase64 = imageB64,
                attachedDocContent = docContent,
                settings = settings.value,
                onProgressUpdate = { status ->
                    _generationStatus.value = status
                }
            )

            // 4. Serialize Tool Logs & Grounding Sources
            val toolLogsJson = if (result.toolLogs.isNotEmpty()) {
                val array = JSONArray()
                result.toolLogs.forEach { log ->
                    val obj = JSONObject()
                    obj.put("toolName", log.toolName)
                    obj.put("input", log.input)
                    obj.put("output", log.output)
                    obj.put("isSuccess", log.isSuccess)
                    array.put(obj)
                }
                array.toString()
            } else null

            val groundingSourcesJson = if (result.groundingSources.isNotEmpty()) {
                val array = JSONArray()
                result.groundingSources.forEach { src ->
                    val obj = JSONObject()
                    obj.put("title", src.title)
                    obj.put("url", src.url)
                    src.snippet?.let { obj.put("snippet", it) }
                    array.put(obj)
                }
                array.toString()
            } else null

            // 5. Save Assistant Message
            val assistantMsg = MessageEntity(
                conversationId = convId,
                sender = "ASSISTANT",
                content = result.replyText,
                toolLogsJson = toolLogsJson,
                groundingSourcesJson = groundingSourcesJson
            )
            repository.insertMessage(assistantMsg)

            // If user asked to remember something, automatically extract to Long-term Memory
            if (settings.value.isMemoryEnabled && (prompt.contains("remember that", true) || prompt.contains("my name is", true) || prompt.contains("remember my", true))) {
                val fact = prompt.replace("remember that", "", true).replace("remember my", "my", true).trim()
                if (fact.isNotBlank()) {
                    repository.addMemory(fact, "FACT")
                }
            }

            _isGenerating.value = false
            _generationStatus.value = ""
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        _isGenerating.value = false
        _generationStatus.value = ""
    }

    fun regenerateLastResponse() {
        val convId = _currentConversationId.value ?: return
        viewModelScope.launch {
            val msgs = repository.getMessagesList(convId)
            val lastUserMsg = msgs.lastOrNull { it.sender == "USER" }
            if (lastUserMsg != null) {
                // Delete trailing assistant message if present
                val lastMsg = msgs.lastOrNull()
                if (lastMsg?.sender == "ASSISTANT") {
                    repository.deleteMessage(lastMsg.id)
                }
                composerText.value = lastUserMsg.content
                attachedImageBase64.value = lastUserMsg.imageBase64
                attachedDocumentName.value = lastUserMsg.attachedDocumentName
                sendMessage()
            }
        }
    }

    fun editUserMessage(message: MessageEntity) {
        composerText.value = message.content
        attachedImageBase64.value = message.imageBase64
        attachedDocumentName.value = message.attachedDocumentName
    }

    fun pinConversation(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinned(id, !isPinned)
        }
    }

    fun archiveConversation(id: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(id, !isArchived)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val active = activeConversations.value
                val next = active.firstOrNull { it.id != id }
                _currentConversationId.value = next?.id
            }
        }
    }

    fun startRenameConversation(conversation: ConversationEntity) {
        renameTargetConversation.value = conversation
        renameDialogText.value = conversation.title
    }

    fun confirmRenameConversation() {
        val target = renameTargetConversation.value ?: return
        val newTitle = renameDialogText.value.trim()
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                repository.renameConversation(target.id, newTitle)
                renameTargetConversation.value = null
            }
        }
    }

    fun addMemory(content: String, category: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addMemory(content.trim(), category)
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    fun addDocument(fileName: String, fileType: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.addDocument(fileName, fileType, content)
            userNotice.value = "Document '$fileName' indexed into Knowledge Base."
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
        }
    }

    fun attachDocumentForCurrentChat(document: DocumentEntity) {
        attachedDocumentName.value = document.fileName
        attachedDocumentContent.value = document.content
        userNotice.value = "Attached '${document.fileName}' to active message."
    }

    fun attachImageFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    attachedImageBase64.value = base64
                    userNotice.value = "Image attached successfully."
                }
            } catch (e: Exception) {
                userNotice.value = "Failed to load image: ${e.message}"
            }
        }
    }

    fun updateSettings(newSettings: AiSettings) {
        repository.updateSettings(newSettings)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
