package com.example.finna.ui.analytics

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.finna.R
import com.example.finna.data.model.Transaction
import com.example.finna.databinding.FragmentAnalyticsBinding
import com.example.finna.util.Categories
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))
            .format(Date()).uppercase(Locale("id", "ID"))

        viewModel.allTransactions.observe(viewLifecycleOwner) { list ->
            val income = list.filter { it.type == "income" }.sumOf { it.amount }
            val expense = list.filter { it.type == "expense" }.sumOf { it.amount }
            binding.tvTotalIncome.text = Categories.formatRp(income)
            binding.tvTotalExpense.text = Categories.formatRp(expense)
            setupBarChart(list)
            val monthExpenses = list.filter { it.type == "expense" }
            setupPieChart(monthExpenses)
            setupComposition(monthExpenses)
        }
    }

    private fun setupPieChart(expenses: List<Transaction>) {
        val byCategory = expenses.groupBy { it.category }
            .mapValues { e -> e.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
        if (byCategory.isEmpty()) { binding.pieChart.visibility = View.GONE; return }
        binding.pieChart.visibility = View.VISIBLE

        val entries = byCategory.map { (id, v) -> PieEntry(v.toFloat(), Categories.find(id).label) }
        val colors = byCategory.map { (id, _) -> Color.parseColor(Categories.find(id).colorHex) }

        val dataSet = PieDataSet(entries, "").apply { this.colors = colors; sliceSpace = 2f; setDrawValues(false) }
        binding.pieChart.apply {
            data = PieData(dataSet); holeRadius = 55f; transparentCircleRadius = 58f
            description.isEnabled = false; legend.isEnabled = false
            setDrawEntryLabels(false); setTouchEnabled(false); invalidate()
        }
    }

    private fun setupBarChart(all: List<Transaction>) {
        val labels = mutableListOf<String>()
        val incomeEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()

        (5 downTo 0).forEachIndexed { index, offset ->
            val c = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            val key = String.format("%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
            labels.add(SimpleDateFormat("MMM", Locale("id")).format(c.time))
            incomeEntries.add(BarEntry(index.toFloat(), all.filter { it.type == "income" && it.date.startsWith(key) }.sumOf { it.amount }.toFloat()))
            expenseEntries.add(BarEntry(index.toFloat(), all.filter { it.type == "expense" && it.date.startsWith(key) }.sumOf { it.amount }.toFloat()))
        }

        val incomeColor = ContextCompat.getColor(requireContext(), R.color.income)
        val expenseColor = ContextCompat.getColor(requireContext(), R.color.expense_color)
        val incomeSet = BarDataSet(incomeEntries, "Pemasukan").apply { color = incomeColor; setDrawValues(false) }
        val expenseSet = BarDataSet(expenseEntries, "Pengeluaran").apply { color = expenseColor; setDrawValues(false) }
        val barData = BarData(incomeSet, expenseSet).apply { barWidth = 0.35f; groupBars(0f, 0.1f, 0.05f) }

        binding.barChart.apply {
            renderer = com.example.finna.util.RoundedBarChartRenderer(
                this,
                com.github.mikephil.charting.animation.ChartAnimator(),
                viewPortHandler,
                cornerRadiusPx = 12f
            )
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f; xAxis.setCenterAxisLabels(true); xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true); axisRight.isEnabled = false
            description.isEnabled = false; invalidate()
        }
    }

    /** List komposisi pengeluaran per kategori dengan progress bar (mengganti pie chart lama). */
    private fun setupComposition(expenses: List<Transaction>) {
        val container = binding.layoutComposition
        container.removeAllViews()

        val total = expenses.sumOf { it.amount }
        val byCategory = expenses.groupBy { it.category }
            .mapValues { e -> e.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }

        if (byCategory.isEmpty() || total <= 0.0) {
            val empty = TextView(requireContext()).apply {
                text = "Belum ada data pengeluaran"
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
            }
            container.addView(empty)
            return
        }

        byCategory.forEach { (id, amount) ->
            val cat = Categories.find(id)
            val percent = ((amount / total) * 100).toInt()

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(14) }
            }

            val headerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(6) }
            }
            val label = TextView(requireContext()).apply {
                text = "${cat.emoji}  ${cat.label}"
                textSize = 13f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val percentLabel = TextView(requireContext()).apply {
                text = "$percent%"
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
            }
            headerRow.addView(label); headerRow.addView(percentLabel)

            val progress = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = percent
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(8)
                )
                progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(cat.colorHex))
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.progress_track)
                )
                progressBackgroundTintMode = android.graphics.PorterDuff.Mode.SRC_IN
            }

            row.addView(headerRow); row.addView(progress)
            container.addView(row)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
