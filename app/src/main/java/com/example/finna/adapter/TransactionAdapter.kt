package com.example.finna.adapter

import android.view.*
import androidx.recyclerview.widget.*
import com.example.finna.data.model.Transaction
import com.example.finna.databinding.ItemTransactionBinding
import com.example.finna.util.Categories
import com.example.finna.util.applyAmountColor
import com.example.finna.util.applyCategoryCircleBackground

class TransactionAdapter(
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction) {
            val cat = Categories.find(tx.category)
            b.tvEmoji.text = cat.emoji
            b.tvCategory.text = cat.label
            b.tvNote.text = tx.note.ifEmpty { tx.date }
            b.tvAmount.text = (if (tx.type == "income") "+" else "-") + Categories.formatRp(tx.amount)
            b.tvAmount.applyAmountColor(tx.type)
            b.emojiContainer.applyCategoryCircleBackground(cat.colorHex)
            b.btnEdit.setOnClickListener { onEdit(tx) }
            b.btnDelete.setOnClickListener { onDelete(tx) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
        override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
    }
}
