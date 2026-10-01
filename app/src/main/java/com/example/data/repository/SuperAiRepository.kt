package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AiSettings
import com.example.data.model.ConversationEntity
import com.example.data.model.DocumentChunkEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.MemoryEntity
import com.example.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SuperAiRepository(private val database: AppDatabase) {

    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val memoryDao = database.memoryDao()
    private val documentDao = database.documentDao()

    private val _settings = MutableStateFlow(AiSettings())
    val settings: StateFlow<AiSettings> = _settings.asStateFlow()

    // Conversations
    fun getActiveConversations(): Flow<List<ConversationEntity>> = conversationDao.getActiveConversations()
    fun getArchivedConversations(): Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()

    suspend fun getConversationById(id: String): ConversationEntity? = conversationDao.getConversationById(id)

    suspend fun createConversation(title: String = "New Chat", model: String = "gemini-3.5-flash"): ConversationEntity {
        val conv = ConversationEntity(
            title = title,
            model = model
        )
        conversationDao.insertOrUpdate(conv)
        return conv
    }

    suspend fun renameConversation(id: String, newTitle: String) {
        conversationDao.renameConversation(id, newTitle)
    }

    suspend fun setPinned(id: String, isPinned: Boolean) {
        conversationDao.setPinned(id, isPinned)
    }

    suspend fun setArchived(id: String, isArchived: Boolean) {
        conversationDao.setArchived(id, isArchived)
    }

    suspend fun deleteConversation(id: String) {
        conversationDao.deleteById(id)
    }

    // Messages
    fun getMessages(conversationId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForConversation(conversationId)

    suspend fun getMessagesList(conversationId: String): List<MessageEntity> = messageDao.getMessagesList(conversationId)

    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insert(message)
        // Update conversation last updated timestamp
        val conv = conversationDao.getConversationById(message.conversationId)
        if (conv != null) {
            conversationDao.update(conv.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteMessage(id: String) {
        messageDao.deleteById(id)
    }

    // Memories
    fun getAllMemories(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun addMemory(content: String, category: String = "FACT") {
        memoryDao.insert(MemoryEntity(content = content, category = category))
    }

    suspend fun deleteMemory(id: String) {
        memoryDao.deleteById(id)
    }

    suspend fun clearAllMemories() {
        memoryDao.deleteAll()
    }

    // Documents & RAG
    fun getAllDocuments(): Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    suspend fun addDocument(fileName: String, fileType: String, content: String) {
        val docId = UUID.randomUUID().toString()
        val doc = DocumentEntity(
            id = docId,
            fileName = fileName,
            fileType = fileType,
            content = content,
            summary = content.take(150).replace("\n", " ") + if (content.length > 150) "..." else ""
        )
        documentDao.insertDocument(doc)

        // Chunk document into ~300 character chunks for RAG
        val chunks = mutableListOf<DocumentChunkEntity>()
        val paragraphs = content.split("\n\n")
        var chunkIdx = 0

        for (p in paragraphs) {
            val trimmed = p.trim()
            if (trimmed.isNotBlank()) {
                if (trimmed.length > 400) {
                    val words = trimmed.split(" ")
                    val sb = StringBuilder()
                    for (w in words) {
                        if (sb.length + w.length > 350) {
                            chunks.add(DocumentChunkEntity(documentId = docId, chunkIndex = chunkIdx++, text = sb.toString()))
                            sb.clear()
                        }
                        sb.append(w).append(" ")
                    }
                    if (sb.isNotBlank()) {
                        chunks.add(DocumentChunkEntity(documentId = docId, chunkIndex = chunkIdx++, text = sb.toString()))
                    }
                } else {
                    chunks.add(DocumentChunkEntity(documentId = docId, chunkIndex = chunkIdx++, text = trimmed))
                }
            }
        }

        if (chunks.isEmpty() && content.isNotBlank()) {
            chunks.add(DocumentChunkEntity(documentId = docId, chunkIndex = 0, text = content.take(500)))
        }

        documentDao.insertChunks(chunks)
    }

    suspend fun deleteDocument(id: String) {
        documentDao.deleteDocumentById(id)
    }

    // Settings
    fun updateSettings(newSettings: AiSettings) {
        _settings.value = newSettings
    }
}
