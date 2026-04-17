package com.yourapp.spendwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.PendingSmsEntity
import java.text.SimpleDateFormat
import java.util.*

// ─── AI Review Screen ────────────────────────────────────────────────────────

@Composable
fun AiReviewScreen(
    modifier: Modifier = Modifier,
    uiState: DashboardUiState,
    onRetryItem: (SmsReviewEntity) -> Unit,
    onRetryAllFailed: () -> Unit
) {
    val items = uiState.reviewCenterItems
    val listState = rememberLazyListState()
    val currentAiItem = uiState.currentAiReviewItem
    val engineName = if (uiState.isCloudAiEnabled && uiState.cloudAiApiKey.isNotBlank())
        "Gemma 3 27B" else "Gemini Nano"

    val acceptedCount  = items.count { it.finalStatus == "AI_CONFIRMED" }
    val rejectedCount  = items.count { it.finalStatus == "AI_REJECTED" }
    val failedCount    = items.count { it.finalStatus == "AI_FAILED" }
    val pendingCount   = items.count { it.finalStatus !in listOf("AI_CONFIRMED", "AI_REJECTED", "AI_FAILED") }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "AI Review",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Gemini-powered SMS transaction parsing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6F89B5)
                )
            }
        }

        // ── Summary counter row ─────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AiCountBadge(
                    modifier = Modifier.weight(1f),
                    count = acceptedCount,
                    label = "Accepted"
                )
                AiCountBadge(
                    modifier = Modifier.weight(1f),
                    count = failedCount + pendingCount,
                    label = "Review"
                )
                AiCountBadge(
                    modifier = Modifier.weight(1f),
                    count = rejectedCount,
                    label = "Rejected"
                )
            }
        }

        // Retry all failed button
        if (failedCount > 0) {
            item {
                OutlinedButton(
                    onClick = onRetryAllFailed,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry all failed ($failedCount)", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Empty state
        if (items.isEmpty() && currentAiItem == null) {
            item {
                EmptyStateCard("No messages have been sent to $engineName for review yet.")
            }
        } else {
            // Currently processing item FIRST
            if (currentAiItem != null) {
                item {
                    AiProcessingCard(
                        item = currentAiItem, 
                        engineName = engineName,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Reviewed items as cards (REVERSED: newest first)
            val reversedItems = items.reversed()
            items(reversedItems, key = { it.id }) { item ->
                AiReviewItemCard(
                    item = item, 
                    onRetry = onRetryItem,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ─── Count Badge ─────────────────────────────────────────────────────────────

@Composable
private fun AiCountBadge(
    modifier: Modifier,
    count: Int,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Reviewed item card ──────────────────────────────────────────────────────

@Composable
private fun AiReviewItemCard(
    item: SmsReviewEntity,
    onRetry: (SmsReviewEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReasoning by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val timeFormatted = rememberReviewTimeFormatted(item.receivedAt)
    val (statusColor, statusText, statusIcon) = aiReviewStatusDisplay(item.finalStatus)

    if (showReasoning) {
        AiReasoningDialog(
            item = item,
            onDismiss = { showReasoning = false }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // Top Section (Header + Message Body)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.sender,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = timeFormatted,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Status Badge 
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }

                // SMS body (raw)
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
            }

            // AI Parsed Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReasoning = true }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Psychology,
                        contentDescription = "AI",
                        tint = AccentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "AI Parsed Result",
                        color = AccentPurple,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                ParsedDataRow("Amount", "₹${formatAmountNoDecimals(item.previewAmount)}")
                ParsedDataRowWithArrow("Merchant", item.previewMerchant)
                ParsedDataRow("Account", item.previewBank.ifBlank { item.sender })

                if (item.finalStatus == "AI_FAILED") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.aiReason.ifBlank { "Parsing failed" },
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentAmber
                        )
                        FilledTonalButton(
                            onClick = { onRetry(item) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// ─── Parsed row helpers ──────────────────────────────────────────────────────

@Composable
private fun ParsedDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ParsedDataRowWithArrow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun formatAmountNoDecimals(amount: Double): String {
    val fmt = java.text.DecimalFormat("#,##0")
    return fmt.format(amount)
}

// ─── Currently processing card ───────────────────────────────────────────────

@Composable
private fun AiProcessingCard(
    item: PendingSmsEntity, 
    engineName: String = "Gemini Nano",
    modifier: Modifier = Modifier
) {
    val timeFormatted = rememberReviewTimeFormatted(item.receivedAt)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.sender,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = timeFormatted,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(AccentAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                color = AccentAmber,
                                strokeWidth = 1.5.dp
                            )
                            Text(
                                text = "Thinking",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentAmber
                            )
                        }
                    }
                }

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
                
            }
            // Divider
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "$engineName is analyzing...",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentAmber,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private data class StatusDisplay(val color: Color, val text: String, val icon: ImageVector)

private fun aiReviewStatusDisplay(finalStatus: String): StatusDisplay = when (finalStatus) {
    "AI_CONFIRMED" -> StatusDisplay(AccentGreen, "Accepted", Icons.Rounded.Check) 
    "AI_REJECTED" -> StatusDisplay(AccentPink, "Rejected", Icons.Rounded.Close) 
    "AI_FAILED" -> StatusDisplay(AccentAmber, "Retry", Icons.Rounded.Replay)
    else -> StatusDisplay(AccentAmber, "Review", Icons.Rounded.HourglassEmpty)
}

@Composable
private fun rememberReviewTimeFormatted(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        .format(Date(timestamp))
}

@Composable
private fun AiReasoningDialog(item: SmsReviewEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Psychology, null, tint = AccentPurple)
                Text("AI Reasoning Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .heightIn(max = 400.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text("Reasoning", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(item.aiReason.ifBlank { "No reasoning provided." }, style = MaterialTheme.typography.bodySmall)
                    }
                    if (item.aiJson.isNotBlank()) {
                        item {
                            Text("Extracted JSON", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(item.aiJson, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (item.debugLog.isNotBlank()) {
                        item {
                            Text("System Debug Log", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(item.debugLog, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
