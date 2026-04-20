
scan_inbox_screen = r"""
@Composable
private fun ScanInboxScreen(
    uiState: DashboardUiState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit
) {
    val progress  = uiState.importProgress
    val total     = progress.second
    val done      = progress.first
    val pct       = if (total > 0) done.toFloat() / total else 0f
    val isRunning = uiState.isImportingSms
    val logs      = uiState.scanLogEntries
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val added   = logs.count { it.outcome == "Added" }
    val queued  = logs.count { it.outcome == "AI Queue" }
    val skipped = logs.count { it.outcome == "Skipped" }

    androidx.compose.runtime.LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1)
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // TopBar
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Scan SMS Inbox",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Counter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScanCounter(label = "Added",    count = added,   color = AccentGreen,  modifier = Modifier.weight(1f))
                ScanCounter(label = "AI Queue", count = queued,  color = AccentPurple, modifier = Modifier.weight(1f))
                ScanCounter(label = "Skipped",  count = skipped, color = MaterialTheme.colorScheme.onSurface.copy(0.45f), modifier = Modifier.weight(1f))
            }

            // Progress bar
            if (isRunning || total > 0) {
                Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = AccentTeal,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) "Processing $done / $total..." else "Done — $done messages scanned.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Log list
            if (logs.isEmpty() && !isRunning) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Tap Start Now to scan your SMS inbox",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    androidx.compose.foundation.lazy.itemsIndexed(logs) { _, entry ->
                        ScanLogRow(entry)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    enabled = !isRunning || logs.isNotEmpty(),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                if (isRunning) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPink,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Stop", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentTeal,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (logs.isEmpty()) "Start Now" else "Scan Again",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanCounter(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun ScanLogRow(entry: ScanLogEntry) {
    val bgColor: androidx.compose.ui.graphics.Color
    val textColor: androidx.compose.ui.graphics.Color
    val tagLabel: String
    when (entry.outcome) {
        "Added" -> {
            bgColor   = AccentGreen.copy(alpha = 0.08f)
            textColor = AccentGreen
            tagLabel  = "ADDED"
        }
        "AI Queue" -> {
            bgColor   = AccentPurple.copy(alpha = 0.08f)
            textColor = AccentPurple
            tagLabel  = "AI"
        }
        else -> {
            bgColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            tagLabel  = "SKIP"
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(textColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(tagLabel, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.sender,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.bodySnippet,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
"""

with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Insert before line 1011 / index 1010 (the @Composable HomeScreen)
lines.insert(1010, scan_inbox_screen)

with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
    f.writelines(lines)

print(f"Done. Total lines: {len(lines)}")
