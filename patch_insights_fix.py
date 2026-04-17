import re

def patch():
    # 1. Update TransactionRepository
    repo_file = 'app/src/main/java/com/yourapp/spendwise/data/TransactionRepository.kt'
    with open(repo_file, 'r', encoding='utf-8') as f:
        repo_content = f.read()
        
    delegates = """    fun getHomeCardOrder(): List<String> = settingsStore.getHomeCardOrder()
    fun setHomeCardOrder(order: List<String>) = settingsStore.setHomeCardOrder(order)
    fun getHiddenHomeCardIds(): Set<String> = settingsStore.getHiddenHomeCardIds()
    fun setHiddenHomeCardIds(hiddenCardIds: Set<String>) = settingsStore.setHiddenHomeCardIds(hiddenCardIds)"""
    
    new_delegates = delegates + """
    fun getInsightsCardOrder(): List<String> = settingsStore.getInsightsCardOrder()
    fun setInsightsCardOrder(order: List<String>) = settingsStore.setInsightsCardOrder(order)
    fun getHiddenInsightsCardIds(): Set<String> = settingsStore.getHiddenInsightsCardIds()
    fun setHiddenInsightsCardIds(hiddenCardIds: Set<String>) = settingsStore.setHiddenInsightsCardIds(hiddenCardIds)"""
    
    if "fun getInsightsCardOrder(" not in repo_content:
        repo_content = repo_content.replace(delegates, new_delegates)
        with open(repo_file, 'w', encoding='utf-8') as f:
            f.write(repo_content)

    # 2. Update SpendWiseApp.kt
    app_file = 'app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt'
    with open(app_file, 'r', encoding='utf-8') as f:
        app_content = f.read()
        
    old_callsite_exact = """                SpendWiseTab.INSIGHTS -> InsightsScreen(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    onSelectSummaryRange = vm::selectSummaryRange,
                    onSelectAccountFilter = vm::setSelectedAccountFilter,
                    onMonthClick = { showMonthPicker = true }
                )"""
    new_callsite_exact = """                SpendWiseTab.INSIGHTS -> InsightsScreen(
                    modifier = Modifier.padding(innerPadding),
                    uiState = uiState,
                    onSelectSummaryRange = vm::selectSummaryRange,
                    onSelectAccountFilter = vm::setSelectedAccountFilter,
                    onMonthClick = { showMonthPicker = true },
                    onUpdateInsightsCardOrder = { vm.updateInsightsCardOrder(it) },
                    onToggleInsightsCardVisibility = { vm.toggleInsightsCardVisibility(it) }
                )"""
                
    if old_callsite_exact in app_content:
        app_content = app_content.replace(old_callsite_exact, new_callsite_exact)
        
    imports = """import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
"""
    if "import androidx.compose.ui.draw.alpha" not in app_content:
        app_content = app_content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\n" + imports)

    with open(app_file, 'w', encoding='utf-8') as f:
        f.write(app_content)

patch()
