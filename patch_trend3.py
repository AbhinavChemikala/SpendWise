import re

def patch():
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Update tryAwaitRelease logic to onTap
    old_pointer_input = """                        .pointerInput(trend) {
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
                        }"""
    new_pointer_input = """                        .pointerInput(trend) {
                            androidx.compose.foundation.gestures.detectTapGestures(
                                onTap = { offset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val chartWidth = size.width - paddingLeft
                                    val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1)
                                    val xPos = offset.x - paddingLeft
                                    val index = kotlin.math.roundToInt(xPos / widthStep).coerceIn(0, trend.lastIndex)
                                    hoveredIndex = if (hoveredIndex == index) null else index
                                }
                            )
                        }"""
    if old_pointer_input in content:
        content = content.replace(old_pointer_input, new_pointer_input)
    else:
        print("Could not find tap gestures to replace")

    # 2. Fix buildAnnotatedString
    old_tooltip_text = """                        val tooltipText = androidx.compose.ui.text.buildAnnotatedString {
                            androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                append(trend[index].monthLabel)
                                append("\\n")
                            }
                            androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = incomeColor, fontSize = 12.sp)) {
                                append("● ₹${formatRupees(trend[index].income).replace("₹", "").trim()}\\n")
                            }
                            androidx.compose.ui.text.withStyle(androidx.compose.ui.text.SpanStyle(color = expenseColor, fontSize = 12.sp)) {
                                append("● ₹${formatRupees(trend[index].expense).replace("₹", "").trim()}")
                            }
                        }"""
    new_tooltip_text = """                        val tooltipText = buildAnnotatedString {
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
                        }"""
    if old_tooltip_text in content:
        content = content.replace(old_tooltip_text, new_tooltip_text)
    else:
        print("Could not find string styling to replace")

    # 3. Add `sp` import and `SpanStyle`
    imports_to_add = """
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.SpanStyle
"""
    content = content.replace("import androidx.compose.ui.text.buildAnnotatedString", "import androidx.compose.ui.text.buildAnnotatedString" + imports_to_add)

    # 4. Remove `androidx.compose.ui.text.TextStyle` prefix from `val labelStyle = ...`
    content = content.replace("val labelStyle = androidx.compose.ui.text.TextStyle(color = Color.Gray, fontSize = 11.sp)", "val labelStyle = androidx.compose.ui.text.TextStyle(color = Color.Gray, fontSize = 11.sp)") # It actually probably resolves with package name so it's fine, but requires sp. `sp` import is added.

    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
        f.write(content)

patch()
