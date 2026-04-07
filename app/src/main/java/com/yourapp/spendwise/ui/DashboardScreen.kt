package com.yourapp.spendwise.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(vm: MainViewModel = viewModel()) {
    SpendWiseApp(vm = vm)
}
