import re

def patch():
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    category_gradient = """private fun categoryGradient(category: String): androidx.compose.ui.graphics.Brush {
    return when (category.lowercase()) {
        "food", "food & dining" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)))
        "shopping" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC)))
        "bills", "bills & utilities" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)))
        "travel" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7)))
        "rent" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140)))
        "health" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF3EECAC), Color(0xFFEE74E1)))
        "income", "salary", "refunds" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4)))
        "entertainment" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFCCB90), Color(0xFFD57EEB)))
        "upi" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF00C6FB), Color(0xFF005BEA)))
        "loans & emi" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF77062), Color(0xFFFE5196)))
        "gifts & rewards" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF6D365), Color(0xFFFDA085)))
        else -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
    }
}

private fun donutColors(count: Int)"""

    content = content.replace("private fun donutColors(count: Int)", category_gradient)

    content = content.replace(
        "private fun DonutChart(values: List<CategoryTotal>, colors: List<Color>, animate: Boolean = true)",
        "private fun DonutChart(values: List<CategoryTotal>, brushes: List<androidx.compose.ui.graphics.Brush>, animate: Boolean = true)"
    )

    content = content.replace(
        "color = colors[index % colors.size],",
        "brush = brushes[index % brushes.size],"
    )

    old_insights_preview = """    val chartValues = topCategories.ifEmpty { listOf(CategoryTotal("Other", 1.0)) }
    val chartColors = donutColors(chartValues.size)"""
    new_insights_preview = """    val chartValues = topCategories.ifEmpty { listOf(CategoryTotal("Other", 1.0)) }
    val chartBrushes = chartValues.map { categoryGradient(it.category) }"""
    content = content.replace(old_insights_preview, new_insights_preview)

    content = content.replace("colors = chartColors,", "brushes = chartBrushes,")
    content = content.replace(
        "drawCircle(color = chartColors[index % chartColors.size])",
        "drawCircle(brush = chartBrushes[index % chartBrushes.size])"
    )

    old_legend_row = """private fun LegendRow(color: Color, label: String, percent: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )"""
    new_legend_row = """private fun LegendRow(brush: androidx.compose.ui.graphics.Brush, label: String, percent: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(brush = brush)
            )"""
    content = content.replace(old_legend_row, new_legend_row)

    content = re.sub(
        r'colors = donutColors\(topCategories\.size\)',
        r'brushes = (topCategories.ifEmpty { listOf(CategoryTotal("Other", 1.0)) }).map { categoryGradient(it.category) }',
        content
    )
    content = re.sub(
        r'color = donutColors\(topCategories\.size\)\[index % donutColors\(topCategories\.size\)\.size\],',
        r'brush = categoryGradient(category.category),',
        content
    )

    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
        f.write(content)

patch()
