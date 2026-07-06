package com.example.finna.ui.transactions

import android.os.Bundle
import android.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finna.R
import com.example.finna.adapter.TransactionsGroupedAdapter
import com.example.finna.databinding.FragmentTransactionsBinding
import com.example.finna.ui.dialog.AddEditTransactionBottomSheet
import com.example.finna.ui.dialog.DeleteConfirmDialog
import java.text.SimpleDateFormat
import java.util.*

class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionsGroupedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))
            .format(Date()).uppercase(Locale("id", "ID"))

        adapter = TransactionsGroupedAdapter(
            onEdit = { tx -> AddEditTransactionBottomSheet.newInstance(tx).show(parentFragmentManager, "edit") },
            onDelete = { tx -> DeleteConfirmDialog.newInstance(tx).show(parentFragmentManager, "delete") }
        )
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTransactions.adapter = adapter
        binding.etSearch.doAfterTextChanged { viewModel.searchQuery.value = it.toString() }

        selectTab("all")
        binding.tabAll.setOnClickListener { selectTab("all") }
        binding.tabIncome.setOnClickListener { selectTab("income") }
        binding.tabExpense.setOnClickListener { selectTab("expense") }

        viewModel.filteredTransactions.observe(viewLifecycleOwner) { list ->
            adapter.submitTransactions(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /** Segmented control manual (Semua / Pemasukan / Pengeluaran) dengan pill aktif. */
    private fun selectTab(type: String) {
        viewModel.filterType.value = type
        val activeColor = resources.getColor(R.color.white, null)
        val inactiveColor = resources.getColor(R.color.muted_foreground, null)

        listOf(binding.tabAll, binding.tabIncome, binding.tabExpense).forEach { tab ->
            tab.setBackgroundResource(0)
            tab.setTextColor(inactiveColor)
        }
        val selected = when (type) {
            "income" -> binding.tabIncome
            "expense" -> binding.tabExpense
            else -> binding.tabAll
        }
        selected.setBackgroundResource(R.drawable.bg_pill_active)
        selected.setTextColor(activeColor)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
