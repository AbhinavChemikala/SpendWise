import re
import os

def patch_settings():
    file = 'app/src/main/java/com/yourapp/spendwise/data/SettingsStore.kt'
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add constants
    content = content.replace('private const val KEY_HOME_HIDDEN_CARD_IDS = "home_hidden_card_ids"', 
        'private const val KEY_HOME_HIDDEN_CARD_IDS = "home_hidden_card_ids"\n        private const val KEY_INSIGHTS_CARD_ORDER = "insights_card_order"\n        private const val KEY_INSIGHTS_HIDDEN_CARD_IDS = "insights_hidden_card_ids"')

    # Add default insights order
    default_home = """        private val DEFAULT_HOME_CARD_ORDER = listOf(
            "status",
            "quick_summary",
            "accounts",
            "hero_summary",
            "savings_score",
            "budgets",
            "anomaly_alerts",
            "cashflow",
            "insights_preview",
            "recent_transactions"
        )"""
    default_insights = """        private val DEFAULT_INSIGHTS_CARD_ORDER = listOf(
            "status",
            "quick_summary",
            "facts",
            "compare_metrics",
            "income_trend",
            "budgets",
            "anomaly_alerts",
            "cashflow",
            "special_tracking",
            "bank_split",
            "spending_breakdown",
            "top_categories",
            "payment_mode",
            "merchant_analytics",
            "recurring_insights",
            "duplicate_insights",
            "income_vs_expense_chart"
        )"""
    content = content.replace(default_home, default_home + "\n\n" + default_insights)

    # Add normalizers
    normalizers_home = """    private fun normalizeHomeCardIds(ids: List<String>): List<String> {
        val valid = DEFAULT_HOME_CARD_ORDER.toSet()
        return ids
            .filter { it in valid }
            .distinct()
    }"""
    normalizers_insights = """
    private fun normalizeInsightsCardOrder(order: List<String>): List<String> {
        val valid = DEFAULT_INSIGHTS_CARD_ORDER.toSet()
        return order
            .filter { it in valid }
            .distinct() + DEFAULT_INSIGHTS_CARD_ORDER.filterNot { it in order }
    }

    private fun normalizeInsightsCardIds(ids: List<String>): List<String> {
        val valid = DEFAULT_INSIGHTS_CARD_ORDER.toSet()
        return ids
            .filter { it in valid }
            .distinct()
    }"""
    content = content.replace(normalizers_home, normalizers_home + normalizers_insights)

    # Add getters/setters
    methods_home = """    fun setHiddenHomeCardIds(hiddenCardIds: Set<String>) {
        writeList(KEY_HOME_HIDDEN_CARD_IDS, normalizeHomeCardIds(hiddenCardIds.toList()))
    }"""
    methods_insights = """
    fun getInsightsCardOrder(): List<String> {
        val saved = readList<String>(KEY_INSIGHTS_CARD_ORDER)
        return normalizeInsightsCardOrder(saved)
    }

    fun setInsightsCardOrder(order: List<String>) {
        writeList(KEY_INSIGHTS_CARD_ORDER, normalizeInsightsCardOrder(order))
    }

    fun getHiddenInsightsCardIds(): Set<String> {
        return normalizeInsightsCardIds(readList<String>(KEY_INSIGHTS_HIDDEN_CARD_IDS)).toSet()
    }

    fun setHiddenInsightsCardIds(hiddenCardIds: Set<String>) {
        writeList(KEY_INSIGHTS_HIDDEN_CARD_IDS, normalizeInsightsCardIds(hiddenCardIds.toList()))
    }"""
    content = content.replace(methods_home, methods_home + methods_insights)

    # Add to BackupModels.kt
    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)

def patch_view_model():
    file = 'app/src/main/java/com/yourapp/spendwise/ui/MainViewModel.kt'
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()

    # UiState
    content = content.replace('val homeHiddenCardIds: Set<String> = emptySet(),',
        'val homeHiddenCardIds: Set<String> = emptySet(),\n    val insightsCardOrder: List<String> = emptyList(),\n    val insightsHiddenCardIds: Set<String> = emptySet(),')

    # Load Dashboard
    content = content.replace('homeHiddenCardIds = repository.getHiddenHomeCardIds(),',
        'homeHiddenCardIds = repository.getHiddenHomeCardIds(),\n                insightsCardOrder = repository.getInsightsCardOrder(),\n                insightsHiddenCardIds = repository.getHiddenInsightsCardIds(),')

    if "repository.getHiddenInsightsCardIds()" in content:
        # Avoid duplicate replaces if we run multiple times but above replaces exactly match 3 places usually `loadDashboard`, `init`, and `refresh`
        pass
        
    # We might need to replace `homeHiddenCardIds = repository.getHiddenHomeCardIds()` in multiple places natively inside `MainViewModel.kt`
    # Let's do it safer:
    
    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)

    # Add methods to update insights layout
    # append to end before final closing brace
    methods = """
    fun updateInsightsCardOrder(order: List<String>) {
        viewModelScope.launch {
            repository.setInsightsCardOrder(order)
            _dashboardUiState.update { it.copy(insightsCardOrder = repository.getInsightsCardOrder()) }
        }
    }

    fun toggleInsightsCardVisibility(cardId: String) {
        viewModelScope.launch {
            val currentHidden = repository.getHiddenInsightsCardIds().toMutableSet()
            if (currentHidden.contains(cardId)) {
                currentHidden.remove(cardId)
            } else {
                currentHidden.add(cardId)
            }
            repository.setHiddenInsightsCardIds(currentHidden)
            _dashboardUiState.update { it.copy(insightsHiddenCardIds = repository.getHiddenInsightsCardIds()) }
        }
    }
"""
    # find last closing brace
    if 'fun toggleInsightsCardVisibility' not in content:
        last_brace = content.rfind('}')
        if last_brace != -1:
            content = content[:last_brace] + methods + content[last_brace:]
            with open(file, 'w', encoding='utf-8') as f:
                f.write(content)

def patch_backup():
    file = 'app/src/main/java/com/yourapp/spendwise/backup/BackupModels.kt'
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()

    content = content.replace('val homeHiddenCardIds: Set<String> = emptySet()',
        'val homeHiddenCardIds: Set<String> = emptySet(),\n    val insightsCardOrder: List<String> = emptyList(),\n    val insightsHiddenCardIds: Set<String> = emptySet()')
    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)
        
    file2 = 'app/src/main/java/com/yourapp/spendwise/data/SettingsStore.kt'
    with open(file2, 'r', encoding='utf-8') as f:
        content2 = f.read()
    old_get = 'homeHiddenCardIds = getHiddenHomeCardIds()'
    new_get = 'homeHiddenCardIds = getHiddenHomeCardIds(),\n            insightsCardOrder = getInsightsCardOrder(),\n            insightsHiddenCardIds = getHiddenInsightsCardIds()'
    content2 = content2.replace(old_get, new_get)
    
    old_set = 'setHiddenHomeCardIds(settings.homeHiddenCardIds)'
    new_set = 'setHiddenHomeCardIds(settings.homeHiddenCardIds)\n        setInsightsCardOrder(settings.insightsCardOrder)\n        setHiddenInsightsCardIds(settings.insightsHiddenCardIds)'
    content2 = content2.replace(old_set, new_set)
    with open(file2, 'w', encoding='utf-8') as f:
        f.write(content2)


patch_settings()
patch_view_model()
patch_backup()
