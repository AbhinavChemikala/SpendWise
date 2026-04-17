import re

def patch():
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    # Create new box logic cleanly
    box_start = content.find("Box(\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .height(260.dp)")
    box_end = content.find("// Legend", box_start)
    if box_start == -1 or box_end == -1:
        print("Could not find Box block")
        return

    new_box = """Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                var componentWidth by remember { mutableStateOf(0f) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .androidx.compose.ui.layout.onSizeChanged { componentWidth = it.width.toFloat() }
                        .pointerInput(trend) {
                            detectDragGestures(
                                onDragEnd = { hoveredIndex = null },
                                onDragCancel = { hoveredIndex = null }
                            ) { change, _ ->
                                val paddingLeft = 40.dp.toPx()
                                val chartWidth = componentWidth - paddingLeft
                                val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1).toFloat()
                                val xPos = change.position.x - paddingLeft
                                val index = (xPos / widthStep).roundToInt().coerceIn(0, trend.lastIndex)
                                hoveredIndex = index
                            }
                        }
                        .pointerInput(trend) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val chartWidth = componentWidth - paddingLeft
                                    val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1).toFloat()
                                    val xPos = offset.x - paddingLeft
                                    val index = (xPos / widthStep).roundToInt().coerceIn(0, trend.lastIndex)
                                    hoveredIndex = if (hoveredIndex == index) null else index
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
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                append(trend[index].monthLabel)
                                append("\\n")
                            }
                            withStyle(SpanStyle(color = incomeColor, fontSize = 12.sp)) {
                                append("● ₹${formatRupees(trend[index].income).replace("₹", "").trim()}\\n")
                            }
                            withStyle(SpanStyle(color = expenseColor, fontSize = 12.sp)) {
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

            // Legend"""
            
    content = content[:box_start] + new_box + content[box_end + 9:]
    
    # ensure math.pow is imported
    if "import kotlin.math.pow" not in content:
        content = content.replace("import kotlin.math.roundToInt", "import kotlin.math.roundToInt\nimport kotlin.math.pow\nimport androidx.compose.ui.layout.onSizeChanged")

    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
        f.write(content)

patch()
