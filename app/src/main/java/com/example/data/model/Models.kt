package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class ToolType(val displayName: String, val iconName: String) {
    GOOGLE_SEARCH("Google Search", "search"),
    CALCULATOR("Math Calculator", "calculate"),
    DATE_TIME("Date & Time", "schedule"),
    MEMORY("Memory Store", "memory"),
    DOCUMENT_RAG("Knowledge Base", "description"),
    CODE_RUNNER("Code Assistant", "code"),
    GMAIL("Gmail Assistant", "mail")
}

data class EmailMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val senderEmail: String,
    val recipient: String,
    val subject: String,
    val snippet: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDraft: Boolean = false
)

data class GroundingSource(
    val title: String,
    val url: String,
    val snippet: String? = null
)

data class ToolExecutionLog(
    val toolName: String,
    val input: String,
    val output: String,
    val isSuccess: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val model: String = "gemini-3.5-flash",
    val systemPrompt: String = ""
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val sender: String, // "USER" or "ASSISTANT"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null,
    val attachedDocumentName: String? = null,
    val toolLogsJson: String? = null,
    val groundingSourcesJson: String? = null
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val category: String = "FACT", // "FACT", "PREFERENCE", "PROJECT", "INSTRUCTION"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val fileType: String, // "txt", "csv", "json", "md", "code"
    val content: String,
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "document_chunks",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class DocumentChunkEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val chunkIndex: Int,
    val text: String
)

data class AiSettings(
    val selectedModel: String = "gemini-3.5-flash",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val systemInstruction: String = "You are Super AI, an advanced, highly intelligent and helpful AI assistant.",
    val isSearchGroundingEnabled: Boolean = true,
    val isAgentToolsEnabled: Boolean = true,
    val isMemoryEnabled: Boolean = true,
    val enterToSend: Boolean = true,
    val customApiKey: String = ""
)
