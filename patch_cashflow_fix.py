import re

def patch():
    app_file = 'app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt'
    with open(app_file, 'r', encoding='utf-8') as f:
        app_content = f.read()

    old = """                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.heightIn(max = 280.dp).androidx.compose.foundation.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {"""
    
    new = """                Column(
                    modifier = Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {"""

    app_content = app_content.replace(old, new)

    with open(app_file, 'w', encoding='utf-8') as f:
        f.write(app_content)

patch()
