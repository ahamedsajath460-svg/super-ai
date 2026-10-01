package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.AiSettings
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentSettings by viewModel.settings.collectAsState()

    var selectedModel by remember { mutableStateOf(currentSettings.selectedModel) }
    var temperature by remember { mutableFloatStateOf(currentSettings.temperature) }
    var maxTokens by remember { mutableIntStateOf(currentSettings.maxTokens) }
    var systemInstruction by remember { mutableStateOf(currentSettings.systemInstruction) }
    var isSearchGroundingEnabled by remember { mutableStateOf(currentSettings.isSearchGroundingEnabled) }
    var isAgentToolsEnabled by remember { mutableStateOf(currentSettings.isAgentToolsEnabled) }
    var isMemoryEnabled by remember { mutableStateOf(currentSettings.isMemoryEnabled) }
    var enterToSend by remember { mutableStateOf(currentSettings.enterToSend) }
    var customApiKey by remember { mutableStateOf(currentSettings.customApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Super AI Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Primary LLM Provider
                Text(
                    text = "PRIMARY AI MODEL (GEMINI)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )

                val models = listOf(
                    "gemini-3.5-flash" to "Gemini 3.5 Flash (Recommended - Fastest with Search)",
                    "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (Deep STEM Reasoning & Code)",
                    "gemini-3.1-flash-lite-preview" to "Gemini 3.1 Flash Lite (Ultra low latency)"
                )

                models.forEach { (modelId, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedModel = modelId }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedModel == modelId,
                            onClick = { selectedModel = modelId }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Google Search Grounding Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Search Grounding",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Fetch real-time web information with live citations",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSearchGroundingEnabled,
                        onCheckedChange = { isSearchGroundingEnabled = it }
                    )
                }

                // Autonomous Agent Tools Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Agent Tools & Plugins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Calculator, Date/Time, Code Runner, Knowledge Base",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAgentToolsEnabled,
                        onCheckedChange = { isAgentToolsEnabled = it }
                    )
                }

                // Memory Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Long-Term Memory",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Recall user preferences and facts across chats",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isMemoryEnabled,
                        onCheckedChange = { isMemoryEnabled = it }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // System Instruction
                Text(
                    text = "System Instruction / Persona",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = systemInstruction,
                    onValueChange = { systemInstruction = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2,
                    maxLines = 4
                )

                // Temperature Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Temperature (Creativity)", fontSize = 12.sp)
                        Text(text = "%.1f".format(temperature), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.0f..1.0f,
                        steps = 9
                    )
                }

                // Max Output Tokens Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Max Tokens", fontSize = 12.sp)
                        Text(text = "$maxTokens", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { maxTokens = it.toInt() },
                        valueRange = 512f..4096f,
                        steps = 7
                    )
                }

                // Custom API Key (optional override)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini API Key (Optional Override)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        placeholder = { Text("Paste custom Gemini API key...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Text(
                        text = "Defaults to BuildConfig injected key or Secrets panel in AI Studio.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Firebase Cloud Sync Status Card
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Cloud & User Auth",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Room provides local-first offline storage. When google-services.json is added in the app directory, conversations and memories sync with Firebase Firestore & Auth.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateSettings(
                        AiSettings(
                            selectedModel = selectedModel,
                            temperature = temperature,
                            maxTokens = maxTokens,
                            systemInstruction = systemInstruction,
                            isSearchGroundingEnabled = isSearchGroundingEnabled,
                            isAgentToolsEnabled = isAgentToolsEnabled,
                            isMemoryEnabled = isMemoryEnabled,
                            enterToSend = enterToSend,
                            customApiKey = customApiKey
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
