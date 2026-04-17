import re

def patch():
    # 1. Update TransactionRepository to not restrict to 10
    repo_file = 'app/src/main/java/com/yourapp/spendwise/data/TransactionRepository.kt'
    with open(repo_file, 'r', encoding='utf-8') as f:
        repo_content = f.read()

    old_take_last = """            .entries
            .toList()
            .takeLast(10)
            .map { (date, items) ->"""
    new_take_last = """            .entries
            .toList()
            .map { (date, items) ->"""
    
    repo_content = repo_content.replace(old_take_last, new_take_last)
    
    with open(repo_file, 'w', encoding='utf-8') as f:
        f.write(repo_content)

    # 2. Update CashflowCard in SpendWiseApp.kt
    app_file = 'app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt'
    with open(app_file, 'r', encoding='utf-8') as f:
        app_content = f.read()

    cashflow_block_start = app_content.find("private fun CashflowCard")
    cashflow_block_end = app_content.find("}", app_content.find("}", app_content.find("}", cashflow_block_start) + 1) + 1) + 1 # Hacky way to find end of CashflowCard. Let's do it safer.

    # Instead of manual parsing, let's just replace the exact internal block!
    old_cashflow_body = """            if (cashflowDays.isEmpty()) {
                Text("Daily cashflow will appear once transactions are available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                cashflowDays.forEach { day ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day.label, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("-${formatCompactCurrency(day.spent)}", color = AccentPink)
                            Text("+${formatCompactCurrency(day.income)}", color = AccentGreen)
                        }
                    }
                }
            }"""
    
    new_cashflow_body = """            if (cashflowDays.isEmpty()) {
                Text("Daily cashflow will appear once transactions are available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.heightIn(max = 280.dp).androidx.compose.foundation.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    cashflowDays.forEach { day ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day.label, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("-${formatCompactCurrency(day.spent)}", color = AccentPink)
                                Text("+${formatCompactCurrency(day.income)}", color = AccentGreen)
                            }
                        }
                    }
                }
            }"""

    app_content = app_content.replace(old_cashflow_body, new_cashflow_body)

    with open(app_file, 'w', encoding='utf-8') as f:
        f.write(app_content)

patch()
