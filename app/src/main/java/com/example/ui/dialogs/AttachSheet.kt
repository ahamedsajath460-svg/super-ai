package com.example.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    // Android Zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachImageFromUri(uri)
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Attach & Prompt Starters",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Attachment Options (Image, Document Knowledge Base)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Picker Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("attach_photo_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryIndigo.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Attach Image",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Attach Image",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gemini Vision AI",
                            fontSize = 11.sp,
                            color = PrimaryIndigo
                        )
                    }
                }

                // Knowledge Base Document Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.showRagDialog.value = true
                            onDismiss()
                        }
                        .testTag("attach_doc_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryCyan.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Attach Document",
                            tint = SecondaryCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Knowledge Base",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "RAG Documents",
                            fontSize = 11.sp,
                            color = SecondaryCyan
                        )
                    }
                }
            }

            // Quick Starter Prompts
            Text(
                text = "SUGGESTED CAPABILITIES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PromptStarterItem(
                icon = Icons.Default.Code,
                title = "Coding Assistant",
                prompt = "Write a clean Kotlin coroutines StateFlow pipeline with exponential backoff retry.",
                onClick = {
                    viewModel.composerText.value = "Write a clean Kotlin coroutines StateFlow pipeline with exponential backoff retry."
                    onDismiss()
                }
            )

            PromptStarterItem(
                icon = Icons.Default.Search,
                title = "Live Google Search Grounding",
                prompt = "What are the latest breakthroughs in AI and technology announced this week?",
                onClick = {
                    viewModel.composerText.value = "What are the latest breakthroughs in AI and technology announced this week?"
                    onDismiss()
                }
            )

            PromptStarterItem(
                icon = Icons.Default.Calculate,
                title = "Math & Financial Calculation",
                prompt = "Calculate compound interest for an investment of $15,000 at 8.2% annual return compounded monthly for 10 years.",
                onClick = {
                    viewModel.composerText.value = "Calculate compound interest for an investment of $15,000 at 8.2% annual return compounded monthly for 10 years."
                    onDismiss()
                }
            )

            PromptStarterItem(
                icon = Icons.Default.Language,
                title = "Multilingual Translation",
                prompt = "Translate 'Welcome to the future of artificial intelligence' into Tamil and Sinhala with phonetic guides.",
                onClick = {
                    viewModel.composerText.value = "Translate 'Welcome to the future of artificial intelligence' into Tamil and Sinhala with phonetic guides."
                    onDismiss()
                }
            )

            PromptStarterItem(
                icon = Icons.Default.Mail,
                title = "Gmail Assistant",
                prompt = "Check my latest unread emails and draft a summary of key action items.",
                onClick = {
                    viewModel.composerText.value = "Check my latest unread emails in Gmail and draft a summary of key action items."
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PromptStarterItem(
    icon: ImageVector,
    title: String,
    prompt: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigo,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = prompt,
                fontSize = 11.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
