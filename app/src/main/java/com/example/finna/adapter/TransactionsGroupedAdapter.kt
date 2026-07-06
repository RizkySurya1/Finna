package com.example.finna.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finna.data.model.Transaction
import com.example.finna.databinding.ItemDateHeaderBinding
import com.example.finna.databinding.ItemTransactionBinding
import com.example.finna.util.Categories
import com.example.finna.util.applyAmountColor
import com.example.finna.util.applyCategoryCircleBackground
import java.text.SimpleDateFormat
import java.util.Locale

/** Item untuk RecyclerView layar Transaksi: bisa berupa header tanggal atau transaksi. */
sealed class TxListItem {
    data class Header(val dateLabel: String) : TxListItem()
    data class Row(val transaction: Transaction) : TxListItem()
}

class TransactionsGroupedAdapter(
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<TxListItem> = emptyList()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }

    /** Kelompokkan daftar transaksi (sudah urut tanggal desc) menjadi header + baris. */
    fun submitTransactions(transactions: List<Transaction>) {
        val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val grouped = mutableListOf<TxListItem>()
        var lastDate: String? = null
        transactions.forEach { tx ->
            if (tx.date != lastDate) {
                val label = try {
                    displayFormat.format(parseFormat.parse(tx.date)!!).uppercase(Locale("id", "ID"))
                } catch (e: Exception) { tx.date }
                grouped.add(TxListItem.Header(label))
                lastDate = tx.date
            }
            grouped.add(TxListItem.Row(tx))
        }
        items = grouped
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is TxListItem.Header -> TYPE_HEADER
        is TxListItem.Row -> TYPE_ROW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemDateHeaderBinding.inflate(inflater, parent, false))
        } else {
            RowViewHolder(ItemTransactionBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TxListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TxListItem.Row -> (holder as RowViewHolder).bind(item.transaction)
        }
    }

    override fun getItemCount() = items.size

    inner class HeaderViewHolder(private val b: ItemDateHeaderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(header: TxListItem.Header) { b.tvDateHeader.text = header.dateLabel }
    }

    inner class RowViewHolder(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction) {
            val cat = Categories.find(tx.category)
            b.tvEmoji.text = cat.emoji
            b.tvCategory.text = cat.label
            b.tvNote.text = tx.note.ifEmpty { "-" }
            b.tvAmount.text = (if (tx.type == "income") "+" else "-") + Categories.formatRp(tx.amount)
            b.tvAmount.applyAmountColor(tx.type)
            b.emojiContainer.applyCategoryCircleBackground(cat.colorHex)
            b.btnEdit.setOnClickListener { onEdit(tx) }
            b.btnDelete.setOnClickListener { onDelete(tx) }
        }
    }
}
