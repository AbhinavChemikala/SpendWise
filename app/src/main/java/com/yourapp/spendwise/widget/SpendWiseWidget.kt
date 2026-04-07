package com.yourapp.spendwise.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yourapp.spendwise.MainActivity
import com.yourapp.spendwise.R
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

// ── Palette ───────────────────────────────────────────────────────────────────
private val WBg       = Color(0xFF12111A)
private val WCard     = Color(0xFF1C1B27)
private val WStroke   = Color(0xFF2E2D3D)
private val WPrimary  = Color.White
private val WSecond   = Color(0xFF9E9CB8)
private val WAccent   = Color(0xFF7C6AFA)
private val WSpent    = Color(0xFFE57373)
private val WIncome   = Color(0xFF81C784)

private fun rupee(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)

// ─────────────────────────────────────────────────────────────────────────────

class SpendWiseWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getInstance(context.applicationContext).transactionDao()

        val now      = LocalDate.now()
        val startMs  = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs    = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val summary      = dao.getMonthlySummary(startMs, endMs)
        val transactions = dao.getTransactionsList(startMs, endMs)   // all of this month

        provideContent {
            val size = LocalSize.current
            when {
                size.height < 130.dp ->
                    SmallWidget(context, summary.totalSpent)
                size.height < 240.dp || size.width < 240.dp ->
                    MediumWidget(context, summary.totalSpent, summary.totalReceived, transactions)
                else ->
                    LargeWidget(context, summary.totalSpent, summary.totalReceived, transactions)
            }
        }
    }

    // ── SMALL (2×2) ─────────────────────────────────────────────────────────
    @Composable
    private fun SmallWidget(ctx: Context, totalSpent: Double) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WBg)
                .cornerRadius(20.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = GlanceModifier.width(28.dp).height(3.dp).background(WAccent).cornerRadius(2.dp)) {}
            Spacer(GlanceModifier.height(10.dp))
            Text("This Month", style = ts(WSecond, 11.sp))
            Spacer(GlanceModifier.height(4.dp))
            Text(rupee(totalSpent), style = ts(WPrimary, 17.sp, bold = true))
            Spacer(GlanceModifier.height(14.dp))
            AddBtn(ctx, 36.dp)
        }
    }

    // ── MEDIUM (4×2) ────────────────────────────────────────────────────────
    @Composable
    private fun MediumWidget(
        ctx: Context,
        totalSpent: Double,
        totalReceived: Double,
        transactions: List<TransactionEntity>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WBg)
                .cornerRadius(20.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(modifier = GlanceModifier.width(3.dp).height(36.dp).background(WAccent).cornerRadius(2.dp)) {}
                Spacer(GlanceModifier.width(10.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text("SPENT", style = ts(WSpent, 10.sp))
                    Text(rupee(totalSpent), style = ts(WPrimary, 19.sp, bold = true))
                    Text("In: ${rupee(totalReceived)}", style = ts(WIncome, 11.sp))
                }
                AddBtn(ctx, 38.dp)
            }

            Spacer(GlanceModifier.height(10.dp))
            Hairline()
            Spacer(GlanceModifier.height(6.dp))

            // Scrollable transaction list via LazyColumn
            LazyColumn(modifier = GlanceModifier.fillMaxWidth().fillMaxHeight()) {
                items(transactions) { tx ->
                    TxRow(tx)
                }
            }
        }
    }

    // ── LARGE (4×4) ─────────────────────────────────────────────────────────
    @Composable
    private fun LargeWidget(
        ctx: Context,
        totalSpent: Double,
        totalReceived: Double,
        transactions: List<TransactionEntity>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WBg)
                .cornerRadius(20.dp)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Branding row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text("SpendWise", style = ts(WAccent, 13.sp, bold = true))
                    Text("This Month", style = ts(WSecond, 11.sp))
                }
                AddBtn(ctx, 40.dp)
            }

            Spacer(GlanceModifier.height(12.dp))

            // Summary cards
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                SummaryCard(label = "SPENT", value = rupee(totalSpent), accent = WSpent, modifier = GlanceModifier.defaultWeight())
                Spacer(GlanceModifier.width(10.dp))
                SummaryCard(label = "INCOME", value = rupee(totalReceived), accent = WIncome, modifier = GlanceModifier.defaultWeight())
            }

            Spacer(GlanceModifier.height(12.dp))
            Hairline()
            Spacer(GlanceModifier.height(8.dp))

            Text("RECENT", style = ts(WSecond, 10.sp, bold = true))
            Spacer(GlanceModifier.height(8.dp))

            // Scrollable transaction list via LazyColumn
            LazyColumn(modifier = GlanceModifier.fillMaxWidth().fillMaxHeight()) {
                items(transactions) { tx ->
                    TxRow(tx)
                }
            }
        }
    }

    // ── Shared components ────────────────────────────────────────────────────

    @Composable
    private fun SummaryCard(label: String, value: String, accent: Color, modifier: GlanceModifier) {
        Column(
            modifier = modifier
                .background(WCard)
                .cornerRadius(12.dp)
                .padding(12.dp)
        ) {
            Text(label, style = ts(accent, 10.sp))
            Spacer(GlanceModifier.height(2.dp))
            Text(value, style = ts(WPrimary, 15.sp, bold = true))
        }
    }

    @Composable
    private fun Hairline() {
        Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(WStroke)) {}
    }

    @Composable
    private fun TxRow(tx: TransactionEntity) {
        val isDebit  = tx.type == TransactionType.DEBIT
        val amtColor = if (isDebit) WSpent else WIncome
        val prefix   = if (isDebit) "−" else "+"

        // Outer column provides top+bottom clearance so the dark WBg 
        // shows between cards (Spacer in LazyColumn is unreliable in Glance)
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(WCard)
                    .cornerRadius(10.dp)
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(3.dp)
                        .height(22.dp)
                        .background(amtColor)
                        .cornerRadius(2.dp)
                ) {}
                Spacer(GlanceModifier.width(10.dp))
                Text(
                    tx.merchant,
                    style = ts(WPrimary, 13.sp),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    "$prefix${rupee(tx.amount)}",
                    style = ts(amtColor, 13.sp, bold = true)
                )
            }
        }
    }

    @Composable
    private fun AddBtn(ctx: Context, size: Dp) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            action = MainActivity.ACTION_ADD_TRANSACTION
            flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        Box(
            modifier = GlanceModifier
                .size(size)
                .background(WAccent)
                .cornerRadius(size / 2)
                .clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_add),
                contentDescription = "Add",
                modifier = GlanceModifier.size(size * 0.55f)
            )
        }
    }

    // tiny helper to keep TextStyle concise
    private fun ts(
        color: Color,
        size: androidx.compose.ui.unit.TextUnit,
        bold: Boolean = false
    ) = TextStyle(
        color = ColorProvider(color, color),
        fontSize = size,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
    )
}

// ── Receivers ─────────────────────────────────────────────────────────────────

class SpendWiseSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpendWiseWidget()
}

class SpendWiseMediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpendWiseWidget()
}

class SpendWiseLargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SpendWiseWidget()
}

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        SpendWiseWidget().updateAll(context)
    }
}
