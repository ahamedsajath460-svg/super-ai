package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroundingSource
import com.example.data.model.MessageEntity
import com.example.data.model.ToolExecutionLog
import com.example.ui.theme.AssistantBubbleDark
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.UserBubbleDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageItem(
    message: MessageEntity,
    isSpeakingThis: Boolean,
    onSpeakToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Super AI Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo)
                    .border(1.5.dp, SecondaryCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Super AI",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.92f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Message Card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.testTag("message_bubble_${message.id.take(8)}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Display attached image if present
                    if (!message.imageBase64.isNullOrBlank()) {
                        val bitmap = remember(message.imageBase64) {
                            try {
                                val bytes = Base64.decode(message.imageBase64, Base64.NO_WRAP)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Attached Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Display attached document pill if present
                    if (!message.attachedDocumentName.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Document",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.attachedDocumentName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Display Agent Tool Logs (expandable trace)
                    if (!isUser && !message.toolLogsJson.isNullOrBlank()) {
                        val toolLogs = remember(message.toolLogsJson) {
                            parseToolLogs(message.toolLogsJson)
                        }
                        if (toolLogs.isNotEmpty()) {
                            AgentToolTraceView(toolLogs = toolLogs)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Message Text with Markdown & Code formatting
                    MarkdownView(content = message.content)

                    // Grounding Sources (Search citations)
                    if (!isUser && !message.groundingSourcesJson.isNullOrBlank()) {
                        val sources = remember(message.groundingSourcesJson) {
                            parseGroundingSources(message.groundingSourcesJson)
                        }
                        if (sources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SearchSourcesRow(sources = sources)
                        }
                    }
                }
            }

            // Message Footer & Action Buttons
            Row(
                modifier = Modifier
                    .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val timeString = remember(message.timestamp) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
                }
                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (isUser) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    val context = LocalContext.current
                    var isCopied by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    // Copy Response button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Super AI Response", message.content)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            scope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy response",
                            tint = if (isCopied) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Text to Speech Read Aloud button
                    IconButton(
                        onClick = onSpeakToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThis) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = if (isSpeakingThis) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Regenerate button
                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentToolTraceView(toolLogs: List<ToolExecutionLog>) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Agent Tools Used (${toolLogs.size}): ${toolLogs.joinToString { it.toolName }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryCyan
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = SecondaryCyan,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (log in toolLogs) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Tool: ${log.toolName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        if (log.input.isNotBlank()) {
                            Text(
                                text = "Query: ${log.input}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = log.output,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSourcesRow(sources: List<GroundingSource>) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Google Search Sources:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (src in sources.take(3)) {
                SuggestionChip(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(src.url))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    label = {
                        Text(
                            text = src.title.take(20),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open source",
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

private fun parseToolLogs(json: String): List<ToolExecutionLog> {
    return try {
        val array = JSONArray(json)
        val list = mutableListOf<ToolExecutionLog>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ToolExecutionLog(
                    toolName = obj.optString("toolName"),
                    input = obj.optString("input"),
                    output = obj.optString("output"),
                    isSuccess = obj.optBoolean("isSuccess", true)
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

private fun parseGroundingSources(json: String): List<GroundingSource> {
    return try {
        val array = JSONArray(json)
        val list = mutableListOf<GroundingSource>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                GroundingSource(
                    title = obj.optString("title"),
                    url = obj.optString("url"),
                    snippet = obj.optString("snippet")
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}
