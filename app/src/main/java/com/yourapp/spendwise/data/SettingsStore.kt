package com.yourapp.spendwise.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun isDebugModeEnabled(): Boolean = prefs.getBoolean(KEY_DEBUG_MODE, false)

    fun setDebugModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
    }

    fun getDebugPhoneNumber(): String = prefs.getString(KEY_DEBUG_PHONE, "").orEmpty()

    fun setDebugPhoneNumber(phoneNumber: String) {
        prefs.edit().putString(KEY_DEBUG_PHONE, phoneNumber.trim()).apply()
    }

    fun isAiReviewEnabled(): Boolean = prefs.getBoolean(KEY_AI_REVIEW, true)

    fun setAiReviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_REVIEW, enabled).apply()
    }

    fun isCloudAiEnabled(): Boolean = prefs.getBoolean(KEY_CLOUD_AI_ENABLED, false)

    fun setCloudAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLOUD_AI_ENABLED, enabled).apply()
    }

    fun getCloudAiApiKey(): String = prefs.getString(KEY_CLOUD_AI_API_KEY, "").orEmpty()

    fun setCloudAiApiKey(key: String) {
        prefs.edit().putString(KEY_CLOUD_AI_API_KEY, key.trim()).apply()
    }

    /** "light", "dark", or "system" */
    fun getThemeMode(): String = prefs.getString(KEY_THEME_MODE, "system").orEmpty().ifBlank { "system" }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun getCustomCategories(): List<CustomCategory> {
        return readList<CustomCategory>(KEY_CUSTOM_CATEGORIES)
            .mapNotNull { item ->
                item.name.trim().takeIf { it.isNotBlank() }?.let { item.copy(name = it) }
            }
            .distinctBy { it.name.lowercase() }
            .sortedBy { it.name.lowercase() }
    }

    fun addCustomCategory(name: String) {
        val normalized = name.trim()
        if (normalized.isBlank()) return
        val updated = getCustomCategories().toMutableList()
        if (updated.any { it.name.equals(normalized, ignoreCase = true) }) return
        updated.add(CustomCategory(name = normalized))
        writeList(KEY_CUSTOM_CATEGORIES, updated.sortedBy { it.name.lowercase() })
    }

    fun removeCustomCategory(categoryId: String) {
        writeList(
            KEY_CUSTOM_CATEGORIES,
            getCustomCategories().filterNot { it.id == categoryId }
        )
    }

    fun getRules(): List<TransactionRule> {
        return readList<TransactionRule>(KEY_TRANSACTION_RULES)
            .filter { rule ->
                rule.name.isNotBlank() || rule.senderContains.isNotBlank() ||
                    rule.merchantContains.isNotBlank() || rule.smsContains.isNotBlank()
            }
    }

    fun getBudgetGoals(): List<BudgetGoal> {
        return readList<BudgetGoal>(KEY_BUDGET_GOALS)
            .filter { it.category.isNotBlank() && it.monthlyLimit > 0.0 }
            .sortedBy { it.category.lowercase() }
    }

    fun saveBudgetGoal(goal: BudgetGoal) {
        val goals = getBudgetGoals().toMutableList()
        val index = goals.indexOfFirst { it.id == goal.id || it.category.equals(goal.category, ignoreCase = true) }
        if (index >= 0) {
            goals[index] = goal
        } else {
            goals.add(goal)
        }
        writeList(KEY_BUDGET_GOALS, goals)
    }

    fun deleteBudgetGoal(goalId: String) {
        writeList(
            KEY_BUDGET_GOALS,
            getBudgetGoals().filterNot { it.id == goalId }
        )
    }

    fun saveRule(rule: TransactionRule) {
        val rules = getRules().toMutableList()
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index >= 0) {
            rules[index] = rule
        } else {
            rules.add(rule)
        }
        writeList(KEY_TRANSACTION_RULES, rules)
    }

    fun deleteRule(ruleId: String) {
        writeList(
            KEY_TRANSACTION_RULES,
            getRules().filterNot { it.id == ruleId }
        )
    }

    private inline fun <reified T> readList(key: String): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<T>>() {}.type
        return runCatching { gson.fromJson<List<T>>(raw, type).orEmpty() }
            .getOrDefault(emptyList())
    }

    private fun <T> writeList(key: String, items: List<T>) {
        prefs.edit().putString(key, gson.toJson(items)).apply()
    }

    companion object {
        private const val PREFS_NAME = "spendwise_settings"
        private const val KEY_DEBUG_MODE = "debug_mode_enabled"
        private const val KEY_DEBUG_PHONE = "debug_phone_number"
        private const val KEY_AI_REVIEW = "ai_review_enabled"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
        private const val KEY_TRANSACTION_RULES = "transaction_rules"
        private const val KEY_BUDGET_GOALS = "budget_goals"
        private const val KEY_CLOUD_AI_ENABLED = "cloud_ai_enabled"
        private const val KEY_CLOUD_AI_API_KEY = "cloud_ai_api_key"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
