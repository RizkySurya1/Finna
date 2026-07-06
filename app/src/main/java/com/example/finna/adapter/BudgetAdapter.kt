package com.example.finna.adapter

import android.content.res.ColorStateList
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.example.finna.R
import com.example.finna.data.model.Budget
import com.example.finna.databinding.ItemBudgetBinding
import com.example.finna.util.Categories

class BudgetAdapter(
    private val onDelete: (Budget) -> Unit,
    private val getSpent: (String) -> Double
) : ListAdapter<Budget, BudgetAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val b: ItemBudgetBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(budget: Budget) {
            val ctx = b.root.context
            val cat = Categories.find(budget.category)
            val spent = getSpent(budget.category)
            val pct = ((spent / budget.limitAmount) * 100).coerceIn(0.0, 100.0).toInt()

            b.tvEmoji.text = cat.emoji
            b.tvCategory.text = cat.label
            b.tvSpent.text = "${Categories.formatRp(spent)} / ${Categories.formatRp(budget.limitAmount)}"
            b.progressBar.progress = pct
            b.tvPercent.text = "$pct%"

            val barColor = when {
                spent > budget.limitAmount -> ContextCompat.getColor(ctx, R.color.expense_color)
                pct > 80 -> ContextCompat.getColor(ctx, R.color.warning)
                else -> ContextCompat.getColor(ctx, R.color.income)
            }
            b.progressBar.progressTintList = ColorStateList.valueOf(barColor)
            b.tvOver.visibility = if (spent > budget.limitAmount) View.VISIBLE else View.GONE
            b.btnDelete.setOnClickListener { onDelete(budget) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Budget>() {
        override fun areItemsTheSame(a: Budget, b: Budget) = a.id == b.id
        override fun areContentsTheSame(a: Budget, b: Budget) = a == b
    }
}
