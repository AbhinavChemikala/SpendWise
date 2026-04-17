import re

def patch():
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    new_chart = """@Composable
private fun IncomeVsExpenseChart(trend: List<TrendPoint>) {
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Income vs Expense", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Last 6 months trend", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }

            if (trend.isEmpty()) {
                EmptyStateCard("Not enough data for a trend.")
                return@Column
            }

            val maxExpense = trend.maxOfOrNull { it.expense } ?: 0.0
            val maxIncome = trend.maxOfOrNull { it.income } ?: 0.0
            val globalMax = maxOf(maxExpense, maxIncome).coerceAtLeast(10.0)

            val steps = 4
            val rawStep = globalMax / steps
            val magnitude = kotlin.math.pow(10.0, kotlin.math.floor(kotlin.math.log10(rawStep))).coerceAtLeast(1.0)
            val stepSize = kotlin.math.ceil(rawStep / magnitude) * magnitude
            val graphMax = stepSize * steps

            val yLabels = (0..steps).map { i ->
                val v = i * stepSize
                if (v >= 1000) "${(v / 1000).toInt()}k" else v.toInt().toString()
            }.reversed()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trend) {
                            androidx.compose.foundation.gestures.detectDragGestures(
                                onDragEnd = { hoveredIndex = null },
                                onDragCancel = { hoveredIndex = null }
                            ) { change, _ ->
                                val paddingLeft = 40.dp.toPx()
                                val chartWidth = size.width - paddingLeft
                                val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1)
                                val xPos = change.position.x - paddingLeft
                                val index = kotlin.math.roundToInt(xPos / widthStep).coerceIn(0, trend.lastIndex)
                                hoveredIndex = index
                            }
                        }
                        .pointerInput(trend) {
                            androidx.compose.foundation.gestures.detectTapGestures(
                                onPress = { offset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val chartWidth = size.width - paddingLeft
                                    val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1)
                                    val xPos = offset.x - paddingLeft
                                    val index = kotlin.math.roundToInt(xPos / widthStep).coerceIn(0, trend.lastIndex)
                                    hoveredIndex = index
                                    tryAwaitRelease()
                                    hoveredIndex = null
                                }
                            )
                        }
                ) {
                    val paddingLeft = 40.dp.toPx()
                    val paddingBottom = 30.dp.toPx()
                    val chartWidth = size.width - paddingLeft
                    val chartHeight = size.height - paddingBottom
                    val widthStep = if (trend.size <= 1) chartWidth else chartWidth / (trend.size - 1)
                    val yStepPx = chartHeight / steps

                    // 1. Draw Grid and Y-axis 
                    val dashPathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    val labelStyle = androidx.compose.ui.text.TextStyle(color = Color.Gray, fontSize = 11.sp)

                    yLabels.forEachIndexed { i, label ->
                        val y = i * yStepPx
                        drawText(
                            textMeasurer = textMeasurer,
                            text = label,
                            style = labelStyle,
                            topLeft = Offset(0f, y - 8.dp.toPx())
                        )
                        drawLine(
                            color = gridColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f,
                            pathEffect = dashPathEffect
                        )
                    }

                    // 2. Draw X-axis labels and vertical grids
                    trend.forEachIndexed { index, point ->
                        val x = paddingLeft + (index * widthStep)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = point.monthLabel.take(3),
                            style = labelStyle,
                            topLeft = Offset(x - 10.dp.toPx(), chartHeight + 8.dp.toPx())
                        )
                        drawLine(
                            color = gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, chartHeight),
                            strokeWidth = 2f,
                            pathEffect = dashPathEffect
                        )
                    }

                    // 3. Helper to draw smooth path
                    fun buildPath(values: List<Double>): Path {
                        val path = Path()
                        val pointsPx = values.mapIndexed { index, value ->
                            val x = paddingLeft + (index * widthStep)
                            val normalized = (value / graphMax).toFloat()
                            val y = chartHeight - (normalized * chartHeight)
                            Offset(x, y)
                        }

                        if (pointsPx.isEmpty()) return path
                        path.moveTo(pointsPx.first().x, pointsPx.first().y)

                        for (i in 0 until pointsPx.size - 1) {
                            val p0 = pointsPx[i]
                            val p1 = pointsPx[i + 1]
                            // Cubic bezier magic
                            val controlX = (p0.x + p1.x) / 2
                            path.cubicTo(
                                x1 = controlX, y1 = p0.y,
                                x2 = controlX, y2 = p1.y,
                                x3 = p1.x, y3 = p1.y
                            )
                        }
                        return path
                    }

                    // 4. Draw Income Curve
                    val incomeColor = AccentTeal
                    val incomeValues = trend.map { it.income }
                    val incomePath = buildPath(incomeValues)

                    val incomeFillPath = Path().apply {
                        addPath(incomePath)
                        lineTo(paddingLeft + chartWidth, chartHeight)
                        lineTo(paddingLeft, chartHeight)
                        close()
                    }
                    drawPath(
                        path = incomeFillPath,
                        brush = Brush.verticalGradient(listOf(incomeColor.copy(alpha = 0.2f), Color.Transparent), endY = chartHeight)
                    )
                    drawPath(path = incomePath, color = incomeColor, style = Stroke(width = 5f, cap = StrokeCap.Round))

                    // 5. Draw Expense Curve
                    val expenseColor = AccentPurple
                    val expenseValues = trend.map { it.expense }
                    val expensePath = buildPath(expenseValues)

                    val expenseFillPath = Path().apply {
                        addPath(expensePath)
                        lineTo(paddingLeft + chartWidth, chartHeight)
                        lineTo(paddingLeft, chartHeight)
                        close()
                    }
                    drawPath(
                        path = expenseFillPath,
                        brush = Brush.verticalGradient(listOf(expenseColor.copy(alpha = 0.2f), Color.Transparent), endY = chartHeight)
                    )
                    drawPath(path = expensePath, color = expenseColor, style = Stroke(width = 5f, cap = StrokeCap.Round))

                    // 6. Draw Hover Tooltip
                    hoveredIndex?.let { index ->
                        val xPos = paddingLeft + (index * widthStep)
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(xPos, 0f),
                            end = Offset(xPos, chartHeight),
                            strokeWidth = 3f
                        )

                        val incomeY = chartHeight - ((incomeValues[index] / graphMax).toFloat() * chartHeight)
                        drawCircle(Color.White, radius = 12f, center = Offset(xPos, incomeY))
                        drawCircle(incomeColor, radius = 8f, center = Offset(xPos, incomeY))

                        val expenseY = chartHeight - ((expenseValues[index] / graphMax).toFloat() * chartHeight)
                        drawCircle(Color.White, radius = 12f, center = Offset(xPos, expenseY))
                        drawCircle(expenseColor, radius = 8f, center = Offset(xPos, expenseY))

                        // Tooltip Box
                        val tooltipText = buildAnnotatedString {
                            withStyle(androidx.compose.ui.text.SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                append(trend[index].monthLabel)
                                append("\\n")
                            }
                            withStyle(androidx.compose.ui.text.SpanStyle(color = incomeColor, fontSize = 12.sp)) {
                                append("● ₹${formatRupees(trend[index].income).replace("₹", "").trim()}\\n")
                            }
                            withStyle(androidx.compose.ui.text.SpanStyle(color = expenseColor, fontSize = 12.sp)) {
                                append("● ₹${formatRupees(trend[index].expense).replace("₹", "").trim()}")
                            }
                        }

                        val textLayoutResult = textMeasurer.measure(tooltipText)
                        val tooltipWidth = textLayoutResult.size.width + 32f
                        val tooltipHeight = textLayoutResult.size.height + 24f

                        // Keep tooltip in bounds
                        val tipX = if (xPos + tooltipWidth + 20f > size.width) xPos - tooltipWidth - 20f else xPos + 20f
                        val tipY = (incomeY + expenseY) / 2f - (tooltipHeight / 2f)

                        drawRoundRect(
                            color = Color(0xFF1E2128), // Dark tooltip bg
                            topLeft = Offset(tipX, tipY),
                            size = androidx.compose.ui.geometry.Size(tooltipWidth, tooltipHeight),
                            cornerRadius = CornerRadius(24f, 24f)
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(tipX + 16f, tipY + 12f)
                        )
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AccentTeal))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Income", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AccentPurple))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Expense", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}
"""

    trend_card_regex = re.compile(r"private fun TrendCard\(.*?\n\}", re.DOTALL)
    # The regex might not capture exactly if there are nested blocks. 
    # Let's do a reliable line-based replace for TrendCard mapping:
    # Actually wait, I can just append `new_chart` to the end of the file, then replace the TrendCard invocation.
    
    # Let's find TrendCard invocation in SpendWiseApp.kt
    old_invocation = \"\"\"            TrendCard(
                trend = uiState.trend,
                selectedIndex = selectedTrendIndex,
                onSelected = { selectedTrendIndex = it },
                latestSpent = uiState.totalSpent,
                latestIncome = uiState.totalReceived
            )\"\"\"
    new_invocation = \"\"\"            IncomeVsExpenseChart(trend = uiState.trend)\"\"\"
    
    if old_invocation in content:
        content = content.replace(old_invocation, new_invocation)
    
    # Append the new function
    content += "\\n" + new_chart

    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
        f.write(content)

patch()
