package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.ConversationEntity
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.MainViewModel

@Composable
fun SidebarDrawer(
    viewModel: MainViewModel,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeConversations by viewModel.activeConversations.collectAsState()
    val archivedConversations by viewModel.archivedConversations.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()
    val searchQuery by viewModel.conversationSearchQuery.collectAsState()
    val memories by viewModel.allMemories.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    var showArchived by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Super AI Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Super AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Agent & Reasoning Engine",
                        fontSize = 11.sp,
                        color = SecondaryCyan
                    )
                }
            }

            // New Chat Button
            Button(
                onClick = {
                    viewModel.createNewChat()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("new_chat_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Chat",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Conversations Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.conversationSearchQuery.value = it },
                placeholder = { Text("Search conversations...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("search_chats_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Conversations List (Pinned, Recent, Archived)
            val pinnedChats = activeConversations.filter { it.isPinned }
            val recentChats = activeConversations.filter { !it.isPinned }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (pinnedChats.isNotEmpty()) {
                    item {
                        Text(
                            text = "PINNED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                    items(pinnedChats, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            isSelected = conv.id == currentConvId,
                            onClick = {
                                viewModel.selectConversation(conv.id)
                                onCloseDrawer()
                            },
                            onPinToggle = { viewModel.pinConversation(conv.id, conv.isPinned) },
                            onRename = { viewModel.startRenameConversation(conv) },
                            onArchive = { viewModel.archiveConversation(conv.id, conv.isArchived) },
                            onDelete = { viewModel.deleteConversation(conv.id) }
                        )
                    }
                }

                item {
                    Text(
                        text = "RECENT CHATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                    )
                }

                if (recentChats.isEmpty() && pinnedChats.isEmpty()) {
                    item {
                        Text(
                            text = "No conversations found.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(recentChats, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            isSelected = conv.id == currentConvId,
                            onClick = {
                                viewModel.selectConversation(conv.id)
                                onCloseDrawer()
                            },
                            onPinToggle = { viewModel.pinConversation(conv.id, conv.isPinned) },
                            onRename = { viewModel.startRenameConversation(conv) },
                            onArchive = { viewModel.archiveConversation(conv.id, conv.isArchived) },
                            onDelete = { viewModel.deleteConversation(conv.id) }
                        )
                    }
                }

                if (archivedConversations.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showArchived = !showArchived }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ARCHIVED (${archivedConversations.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (showArchived) {
                        items(archivedConversations, key = { it.id }) { conv ->
                            ConversationItem(
                                conversation = conv,
                                isSelected = conv.id == currentConvId,
                                onClick = {
                                    viewModel.selectConversation(conv.id)
                                    onCloseDrawer()
                                },
                                onPinToggle = { viewModel.pinConversation(conv.id, conv.isPinned) },
                                onRename = { viewModel.startRenameConversation(conv) },
                                onArchive = { viewModel.archiveConversation(conv.id, conv.isArchived) },
                                onDelete = { viewModel.deleteConversation(conv.id) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Bottom Navigation Tools: Memory, RAG Knowledge Base, Settings
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // AI Memory Manager
                DrawerActionItem(
                    icon = Icons.Default.Memory,
                    label = "AI Memory (${memories.size})",
                    testTag = "open_memory_button",
                    onClick = {
                        viewModel.showMemoryDialog.value = true
                        onCloseDrawer()
                    }
                )

                // RAG Knowledge Base
                DrawerActionItem(
                    icon = Icons.Default.Description,
                    label = "Knowledge Base (${documents.size})",
                    testTag = "open_rag_button",
                    onClick = {
                        viewModel.showRagDialog.value = true
                        onCloseDrawer()
                    }
                )

                // Gmail & Email Assistant
                DrawerActionItem(
                    icon = Icons.Default.Mail,
                    label = "Gmail Assistant",
                    testTag = "open_gmail_button",
                    onClick = {
                        viewModel.showGmailDialog.value = true
                        onCloseDrawer()
                    }
                )

                // Settings
                DrawerActionItem(
                    icon = Icons.Default.Settings,
                    label = "Settings & Models",
                    testTag = "open_settings_button",
                    onClick = {
                        viewModel.showSettingsDialog.value = true
                        onCloseDrawer()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Authenticated Google Account Profile
                val userEmail by viewModel.userEmail.collectAsState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.12f))
                        .clickable {
                            viewModel.showGmailDialog.value = true
                            onCloseDrawer()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA4335)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userEmail,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Google OAuth 2.0 Active",
                            fontSize = 10.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Online/Offline Status Indicator
                val isLive = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isLive) SuccessGreen else Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLive) "ONLINE: Gemini 3.5 Flash" else "LOCAL AGENT MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) PrimaryIndigo.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (conversation.isPinned) Icons.Default.PushPin else Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = conversation.title,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (conversation.isPinned) "Unpin" else "Pin") },
                    leadingIcon = {
                        Icon(
                            if (conversation.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onPinToggle()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (conversation.isArchived) "Unarchive" else "Archive") },
                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onArchive()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
