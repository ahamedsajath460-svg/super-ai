package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Composer
import com.example.ui.components.MessageItem
import com.example.ui.components.SidebarDrawer
import com.example.ui.dialogs.AttachSheet
import com.example.ui.dialogs.GmailDialog
import com.example.ui.dialogs.MemoryDialog
import com.example.ui.dialogs.RagDialog
import com.example.ui.dialogs.RenameDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentConvId by viewModel.currentConversationId.collectAsState()
    val conversations by viewModel.activeConversations.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val userNotice by viewModel.userNotice.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()

    // Dialog states
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val showMemory by viewModel.showMemoryDialog.collectAsState()
    val showRag by viewModel.showRagDialog.collectAsState()
    val showAttach by viewModel.showAttachMenu.collectAsState()
    val showGmail by viewModel.showGmailDialog.collectAsState()
    val renameTarget by viewModel.renameTargetConversation.collectAsState()

    val currentConversation = conversations.firstOrNull { it.id == currentConvId }

    // Display user notices in snackbar
    LaunchedEffect(userNotice) {
        userNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userNotice.value = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SidebarDrawer(
                    viewModel = viewModel,
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentConversation?.title ?: "Super AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SecondaryCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = settings.selectedModel.replace("-preview", ""),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open sidebar"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.createNewChat() },
                            modifier = Modifier.testTag("top_new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New chat"
                            )
                        }

                        var topMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { topMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options"
                                )
                            }

                            DropdownMenu(
                                expanded = topMenuExpanded,
                                onDismissRequest = { topMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    onClick = {
                                        topMenuExpanded = false
                                        viewModel.showSettingsDialog.value = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("AI Memory") },
                                    leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null) },
                                    onClick = {
                                        topMenuExpanded = false
                                        viewModel.showMemoryDialog.value = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Knowledge Base (RAG)") },
                                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                    onClick = {
                                        topMenuExpanded = false
                                        viewModel.showRagDialog.value = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Gmail Assistant") },
                                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
                                    onClick = {
                                        topMenuExpanded = false
                                        viewModel.showGmailDialog.value = true
                                    }
                                )
                                if (currentConversation != null) {
                                    DropdownMenuItem(
                                        text = { Text("Rename Chat") },
                                        onClick = {
                                            topMenuExpanded = false
                                            viewModel.startRenameConversation(currentConversation)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Composer(viewModel = viewModel)
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            val listState = rememberLazyListState()

            // Scroll to bottom when new messages arrive
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (messages.isEmpty()) {
                    EmptyChatGreeting(
                        onStarterSelected = { prompt ->
                            viewModel.composerText.value = prompt
                            viewModel.sendMessage()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            var isThisSpeaking by remember { mutableStateOf(false) }

                            MessageItem(
                                message = message,
                                isSpeakingThis = isThisSpeaking && isSpeaking,
                                onSpeakToggle = {
                                    if (isThisSpeaking && isSpeaking) {
                                        viewModel.voiceManager.stopSpeaking()
                                        isThisSpeaking = false
                                    } else {
                                        viewModel.voiceManager.speak(message.content)
                                        isThisSpeaking = true
                                    }
                                },
                                onRegenerate = { viewModel.regenerateLastResponse() },
                                onEdit = { viewModel.editUserMessage(message) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Secondary Dialogs
    if (showSettings) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showSettingsDialog.value = false }
        )
    }

    if (showMemory) {
        MemoryDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showMemoryDialog.value = false }
        )
    }

    if (showRag) {
        RagDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showRagDialog.value = false }
        )
    }

    if (showAttach) {
        AttachSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.showAttachMenu.value = false }
        )
    }

    if (showGmail) {
        GmailDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showGmailDialog.value = false }
        )
    }

    if (renameTarget != null) {
        RenameDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.renameTargetConversation.value = null }
        )
    }
}

@Composable
fun EmptyChatGreeting(
    onStarterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing AI Logo
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SecondaryCyan, PrimaryIndigo)
                    )
                )
                .border(2.dp, Color(0xFF67E8F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Super AI Orb",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Super AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Advanced Autonomous Agent • Multimodal • Reasoning",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Starter Capability Cards Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GreetingStarterCard(
                icon = Icons.Default.Code,
                title = "Code Assistant",
                description = "Build a Kotlin ViewModel with Room & Flow integration",
                onClick = {
                    onStarterSelected("Show me how to build a clean Kotlin MVVM ViewModel with Room Database Flow integration and coroutines error handling.")
                }
            )

            GreetingStarterCard(
                icon = Icons.Default.Search,
                title = "Search Grounding",
                description = "Search the live web with Google Search Grounding",
                onClick = {
                    onStarterSelected("What are the most exciting recent advancements in AI models and quantum computing this year?")
                }
            )

            GreetingStarterCard(
                icon = Icons.Default.Memory,
                title = "AI Memory & Reasoning",
                description = "Recall project requirements and personal preferences",
                onClick = {
                    onStarterSelected("What memories and user preferences do you currently have stored for me, and how can we use them?")
                }
            )
        }
    }
}

@Composable
fun GreetingStarterCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
