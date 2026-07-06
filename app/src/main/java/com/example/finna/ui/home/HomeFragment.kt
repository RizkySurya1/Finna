package com.example.finna.ui.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finna.R
import com.example.finna.adapter.TransactionAdapter
import com.example.finna.databinding.FragmentHomeBinding
import com.example.finna.data.model.Transaction
import com.example.finna.ui.dialog.AddEditTransactionBottomSheet
import com.example.finna.ui.dialog.DeleteConfirmDialog
import com.example.finna.util.Categories
import com.github.mikephil.charting.data.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))
            .format(Date()).uppercase(Locale("id", "ID"))

        adapter = TransactionAdapter(
            onEdit = { tx -> AddEditTransactionBottomSheet.newInstance(tx).show(parentFragmentManager, "edit") },
            onDelete = { tx -> DeleteConfirmDialog.newInstance(tx).show(parentFragmentManager, "delete") }
        )
        binding.recyclerRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentTransactions.adapter = adapter

        binding.btnLihatSemua.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_transactions)
        }

        // FAB tambah transaksi baru
        binding.fabAdd.setOnClickListener {
            AddEditTransactionBottomSheet.newInstance().show(parentFragmentManager, "add")
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { binding.tvIncome.text = Categories.formatRp(it) }
        viewModel.totalExpense.observe(viewLifecycleOwner) { binding.tvExpense.text = Categories.formatRp(it) }

        viewModel.allTransactions.observe(viewLifecycleOwner) { all ->
            val income = all.filter { it.type == "income" }.sumOf { it.amount }
            val expense = all.filter { it.type == "expense" }.sumOf { it.amount }
            binding.tvBalance.text = Categories.formatRp(income - expense)
            val monthExpenses = all.filter { it.type == "expense" }
            setupPieChart(monthExpenses)
            setupCategoryLegend(monthExpenses)
        }

        viewModel.recentTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupPieChart(expenses: List<Transaction>) {
        val byCategory = expenses.groupBy { it.category }
            .mapValues { e -> e.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }.take(5)

        if (byCategory.isEmpty()) { binding.pieChart.visibility = View.GONE; return }
        binding.pieChart.visibility = View.VISIBLE

        val entries = byCategory.map { (id, v) -> PieEntry(v.toFloat(), Categories.find(id).label) }
        val colors = byCategory.map { (id, _) -> Color.parseColor(Categories.find(id).colorHex) }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors; sliceSpace = 2f; setDrawValues(false)
        }
        binding.pieChart.apply {
            data = PieData(dataSet); holeRadius = 55f; transparentCircleRadius = 58f
            description.isEnabled = false; legend.isEnabled = false
            setDrawEntryLabels(false); setTouchEnabled(false); invalidate()
        }
    }

    private fun setupCategoryLegend(expenses: List<Transaction>) {
        val container = binding.layoutCategoryLegend
        container.removeAllViews()
        val byCategory = expenses.groupBy { it.category }
            .mapValues { e -> e.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }.take(5)

        if (byCategory.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "Belum ada pengeluaran bulan ini"
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
            }
            container.addView(empty)
            return
        }

        byCategory.forEach { (id, amount) ->
            val cat = Categories.find(id)
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply { marginEnd = dp(8) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(cat.colorHex))
                }
            }
            val label = TextView(requireContext()).apply {
                text = cat.label
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val value = TextView(requireContext()).apply {
                text = Categories.formatRp(amount)
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
            }
            row.addView(dot); row.addView(label); row.addView(value)
            container.addView(row)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
