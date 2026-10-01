package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBackgroundDark
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ContentSegment {
    data class TextSegment(val text: String) : ContentSegment()
    data class CodeBlockSegment(val language: String, val code: String) : ContentSegment()
}

@Composable
fun MarkdownView(
    content: String,
    modifier: Modifier = Modifier
) {
    val segments = remember(content) { parseMarkdownSegments(content) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (segment in segments) {
            when (segment) {
                is ContentSegment.CodeBlockSegment -> {
                    CodeBlock(language = segment.language, code = segment.code)
                }
                is ContentSegment.TextSegment -> {
                    FormattedText(segment.text)
                }
            }
        }
    }
}

private fun parseMarkdownSegments(raw: String): List<ContentSegment> {
    val list = mutableListOf<ContentSegment>()
    val codeBlockRegex = Regex("```([a-zA-Z0-9_+#-]*)\\n([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = codeBlockRegex.findAll(raw)

    for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        if (start > lastIndex) {
            val textBefore = raw.substring(lastIndex, start).trim()
            if (textBefore.isNotEmpty()) {
                list.add(ContentSegment.TextSegment(textBefore))
            }
        }

        val lang = match.groupValues[1].ifBlank { "code" }
        val code = match.groupValues[2].trimEnd()
        list.add(ContentSegment.CodeBlockSegment(language = lang, code = code))
        lastIndex = end
    }

    if (lastIndex < raw.length) {
        val remaining = raw.substring(lastIndex).trim()
        if (remaining.isNotEmpty()) {
            list.add(ContentSegment.TextSegment(remaining))
        }
    }

    if (list.isEmpty() && raw.isNotEmpty()) {
        list.add(ContentSegment.TextSegment(raw))
    }

    return list
}

@Composable
fun CodeBlock(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CodeBackgroundDark)
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(8.dp))
    ) {
        Column {
            // Header bar with language & copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("code", code)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            scope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) SuccessGreen else Color(0xFF8B949E),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (isCopied) {
                        Text(
                            text = "Copied!",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // Code content with horizontal scrolling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFFE6EDF3),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun FormattedText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Text(
                        text = trimmed.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.removePrefix("# "),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(PrimaryIndigo, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trimmed.removePrefix("> "),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        InlineMarkdownText(trimmed.substring(2))
                    }
                }
                else -> {
                    if (trimmed.isNotEmpty()) {
                        InlineMarkdownText(trimmed)
                    }
                }
            }
        }
    }
}

@Composable
fun InlineMarkdownText(text: String) {
    val annotated = remember(text) {
        buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                // Check bold **text**
                if (text.startsWith("**", i)) {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                        continue
                    }
                }

                // Check inline code `code`
                if (text[i] == '`') {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0x336366F1),
                                color = Color(0xFF67E8F9),
                                fontSize = 13.sp
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                        continue
                    }
                }

                // Check italic *text*
                if (text[i] == '*' && (i == 0 || text[i - 1] != '*')) {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1 && (end + 1 >= text.length || text[end + 1] != '*')) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                        continue
                    }
                }

                append(text[i])
                i++
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
    )
}
