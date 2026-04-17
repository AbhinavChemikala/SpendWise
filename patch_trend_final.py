import re

def patch():
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Update pointerInput for detectDragGestures and detectTapGestures
    old_drag = """                        .pointerInput(trend) {
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
                        }"""
    new_drag = """                        .pointerInput(trend) {
                            val scopeSize = this.size
                            detectDragGestures(
                                onDragEnd = { hoveredIndex = null },
                                onDragCancel = { hoveredIndex = null }
                            ) { change, _ ->
                                val paddingLeft = 40.dp.toPx()
                                val chartWidth = scopeSize.width - paddingLeft
                                val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1)
                                val xPos = change.position.x - paddingLeft
                                val index = (xPos / widthStep).roundToInt().coerceIn(0, trend.lastIndex)
                                hoveredIndex = index
                            }
                        }"""
    
    old_tap = """                        .pointerInput(trend) {
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
    new_tap = """                        .pointerInput(trend) {
                            val scopeSize = this.size
                            detectTapGestures(
                                onTap = { offset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val chartWidth = scopeSize.width - paddingLeft
                                    val widthStep = chartWidth / (trend.size - 1).coerceAtLeast(1)
                                    val xPos = offset.x - paddingLeft
                                    val index = (xPos / widthStep).roundToInt().coerceIn(0, trend.lastIndex)
                                    hoveredIndex = if (hoveredIndex == index) null else index
                                }
                            )
                        }"""

    if old_drag in content:
        content = content.replace(old_drag, new_drag)
    if old_tap in content:
        content = content.replace(old_tap, new_tap)

    # 2. Add imports
    imports_to_add = """
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.roundToInt
"""
    content = content.replace("import androidx.compose.ui.text.SpanStyle", "import androidx.compose.ui.text.SpanStyle" + imports_to_add)

    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
        f.write(content)

patch()
