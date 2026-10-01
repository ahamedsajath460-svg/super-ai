package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.EmailMessage
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GmailDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Inbox", "Compose", "OAuth Security")

    val userEmail by viewModel.userEmail.collectAsState()
    val emails by viewModel.emailsList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedEmail by remember { mutableStateOf<EmailMessage?>(null) }

    // Compose form state
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeBody by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = "Gmail",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gmail Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Connected Account Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connected",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = userEmail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                selectedEmail = null
                            },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> { // INBOX TAB
                        if (selectedEmail != null) {
                            // Single Email View
                            val email = selectedEmail!!
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "From: ${email.sender}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = { selectedEmail = null }) {
                                        Text("← Back", fontSize = 12.sp)
                                    }
                                }

                                Text(
                                    text = "Subject: ${email.subject}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryIndigo
                                )

                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(email.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                HorizontalDivider()

                                Text(
                                    text = email.body,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        viewModel.composerText.value = "Draft a professional reply to ${email.sender} regarding '${email.subject}'. Context: ${email.snippet}"
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Draft AI Reply in Chat", fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Search Box
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search emails in inbox...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            val filteredEmails = if (searchQuery.isBlank()) emails else emails.filter {
                                it.subject.contains(searchQuery, ignoreCase = true) ||
                                        it.sender.contains(searchQuery, ignoreCase = true) ||
                                        it.snippet.contains(searchQuery, ignoreCase = true)
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filteredEmails, key = { it.id }) { email ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedEmail = email },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (email.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            else PrimaryIndigo.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = email.sender,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (email.isRead) FontWeight.Medium else FontWeight.Bold,
                                                    color = if (email.isRead) MaterialTheme.colorScheme.onSurface else PrimaryIndigo
                                                )
                                                Text(
                                                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(email.timestamp)),
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = email.subject,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = email.snippet,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // COMPOSE TAB
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = composeTo,
                                onValueChange = { composeTo = it },
                                label = { Text("To (Recipient email)", fontSize = 12.sp) },
                                placeholder = { Text("colleague@example.com", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = composeSubject,
                                onValueChange = { composeSubject = it },
                                label = { Text("Subject", fontSize = 12.sp) },
                                placeholder = { Text("Meeting updates / Project milestones", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = composeBody,
                                onValueChange = { composeBody = it },
                                label = { Text("Message Body", fontSize = 12.sp) },
                                placeholder = { Text("Type email or let Super AI draft it...", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                minLines = 3,
                                maxLines = 5
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // AI Draft Assistant Button
                                TextButton(
                                    onClick = {
                                        viewModel.composerText.value = "Draft a formal email to '$composeTo' with subject '${composeSubject.ifBlank { "Project Update" }}'."
                                        onDismiss()
                                    }
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Draft with AI", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        if (composeTo.isNotBlank() && composeBody.isNotBlank()) {
                                            viewModel.sendEmail(
                                                to = composeTo,
                                                subject = composeSubject.ifBlank { "Message from Super AI" },
                                                body = composeBody
                                            )
                                            composeTo = ""
                                            composeSubject = ""
                                            composeBody = ""
                                            selectedTab = 0
                                        }
                                    },
                                    enabled = composeTo.isNotBlank() && composeBody.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send via Gmail", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    2 -> { // OAUTH SECURITY TAB
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "OAuth 2.0 Token Authentication",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = "• Google OAuth 2.0 authorization is active for $userEmail.\n" +
                                                "• No password is ever stored or exposed in code.\n" +
                                                "• Authorized Scopes: gmail.readonly, gmail.compose, gmail.send.\n" +
                                                "• Cloud Project ID: gen-lang-client-0030546306.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Text(
                                text = "Super AI operates on the least-privilege security principle. Tokens are securely refreshed and validated through Google's identity provider.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
