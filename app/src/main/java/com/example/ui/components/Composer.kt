package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.viewmodel.MainViewModel

@Composable
fun Composer(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val composerText by viewModel.composerText.collectAsState()
    val attachedImage by viewModel.attachedImageBase64.collectAsState()
    val attachedDocName by viewModel.attachedDocumentName.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationStatus by viewModel.generationStatus.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Permission launcher for Speech-to-Text Audio Recording
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.voiceManager.startListening(
                onResult = { text ->
                    val current = viewModel.composerText.value
                    viewModel.composerText.value = if (current.isBlank()) text else "$current $text"
                },
                onError = { err ->
                    viewModel.userNotice.value = err
                }
            )
        } else {
            viewModel.userNotice.value = "Microphone permission is required for voice input."
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Generation status indicator / Stop pill
            AnimatedVisibility(visible = isGenerating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = generationStatus.ifBlank { "Super AI is generating..." },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.error)
                            .clickable { viewModel.stopGeneration() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("stop_button")
                    ) {
                        Text(
                            text = "Stop",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Attached Media Preview Row
            if (attachedImage != null || attachedDocName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (attachedImage != null) {
                        val bitmap = remember(attachedImage) {
                            try {
                                val bytes = Base64.decode(attachedImage, Base64.NO_WRAP)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, PrimaryIndigo, RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Image preview",
                                    modifier = Modifier.size(54.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .clickable { viewModel.attachedImageBase64.value = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (attachedDocName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = attachedDocName ?: "",
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove doc",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        viewModel.attachedDocumentName.value = null
                                        viewModel.attachedDocumentContent.value = null
                                    }
                            )
                        }
                    }
                }
            }

            // Quick Tool & Model Status Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { viewModel.showSettingsDialog.value = true },
                    label = {
                        Text(
                            text = settings.selectedModel.replace("-preview", ""),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    modifier = Modifier.height(26.dp)
                )

                if (settings.isSearchGroundingEnabled) {
                    AssistChip(
                        onClick = {
                            viewModel.updateSettings(
                                settings.copy(isSearchGroundingEnabled = !settings.isSearchGroundingEnabled)
                            )
                        },
                        label = { Text("Google Search", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SecondaryCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            // Input Row: Attachment Button + Auto-growing TextField + Voice Mic + Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attach button
                IconButton(
                    onClick = { viewModel.showAttachMenu.value = true },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("attach_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach file or image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text Input
                OutlinedTextField(
                    value = composerText,
                    onValueChange = { viewModel.composerText.value = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening..." else "Message Super AI...",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 130.dp)
                        .testTag("message_input"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (settings.enterToSend) ImeAction.Send else ImeAction.Default
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (settings.enterToSend && (composerText.isNotBlank() || attachedImage != null)) {
                                viewModel.sendMessage()
                            }
                        }
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Voice Microphone button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) SecondaryCyan.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable {
                            if (isListening) {
                                viewModel.voiceManager.stopListening()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.voiceManager.startListening(
                                        onResult = { text ->
                                            val current = viewModel.composerText.value
                                            viewModel.composerText.value =
                                                if (current.isBlank()) text else "$current $text"
                                        },
                                        onError = { err ->
                                            viewModel.userNotice.value = err
                                        }
                                    )
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("voice_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Voice input",
                        tint = if (isListening) SecondaryCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Send Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (composerText.isNotBlank() || attachedImage != null) PrimaryIndigo
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable(enabled = (composerText.isNotBlank() || attachedImage != null) && !isGenerating) {
                            viewModel.sendMessage()
                        }
                        .testTag("send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (composerText.isNotBlank() || attachedImage != null) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
