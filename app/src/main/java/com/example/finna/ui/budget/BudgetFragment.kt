package com.example.finna.ui.budget

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finna.adapter.BudgetAdapter
import com.example.finna.databinding.FragmentBudgetBinding
import com.example.finna.ui.dialog.AddBudgetBottomSheet

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDate.text = java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale("id", "ID"))
            .format(java.util.Date()).uppercase(java.util.Locale("id", "ID"))

        adapter = BudgetAdapter(
            onDelete = { budget -> viewModel.deleteBudget(budget) },
            getSpent = { category ->
                viewModel.currentMonthTransactions.value
                    ?.filter { it.category == category && it.type == "expense" }
                    ?.sumOf { it.amount } ?: 0.0
            }
        )
        binding.recyclerBudgets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBudgets.adapter = adapter

        binding.btnTambahAnggaran.setOnClickListener {
            AddBudgetBottomSheet().show(parentFragmentManager, "add_budget")
        }
        binding.fabAddBudget.setOnClickListener {
            AddBudgetBottomSheet().show(parentFragmentManager, "add_budget")
        }

        viewModel.budgets.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.currentMonthTransactions.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
