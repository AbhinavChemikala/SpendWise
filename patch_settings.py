import os
import re

app_file = r'c:\Users\Abhinav\Downloads\SpendWise\app\src\main\java\com\yourapp\spendwise\ui\SpendWiseApp.kt'
new_settings_file = r'c:\Users\Abhinav\Downloads\SpendWise\newSettings.kt'

with open(app_file, 'r', encoding='utf-8') as f:
    app_content = f.read()

with open(new_settings_file, 'r', encoding='utf-8') as f:
    new_settings_content = f.read()

# Find the start of SettingsScreen
start_pattern = r'@OptIn\(ExperimentalLayoutApi::class\)\s*@Composable\s*private fun SettingsScreen\('
# Find the start of SpendWiseBottomBar (which is right after SettingsScreen)
end_pattern = r'@Composable\s*private fun SpendWiseBottomBar\('

start_match = re.search(start_pattern, app_content)
end_match = re.search(end_pattern, app_content[start_match.start():])

if start_match and end_match:
    start_idx = start_match.start()
    end_idx = start_idx + end_match.start()
    
    # We replace from start_idx to end_idx with new_settings_content
    # ensure we keep empty lines separation
    new_app_content = app_content[:start_idx] + new_settings_content + "\n\n" + app_content[end_idx:]
    
    with open(app_file, 'w', encoding='utf-8') as f:
        f.write(new_app_content)
    print("Successfully patched SettingsScreen!")
else:
    print("Could not find the bounds to patch!")
