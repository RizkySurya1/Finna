package com.example.finna.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.finna.FinnApp
import com.example.finna.data.model.Transaction
import kotlinx.coroutines.launch

class DeleteConfirmDialog : DialogFragment() {
    companion object {
        fun newInstance(tx: Transaction) = DeleteConfirmDialog().apply {
            arguments = Bundle().apply {
                putLong("id", tx.id); putString("type", tx.type)
                putDouble("amount", tx.amount); putString("category", tx.category)
                putString("note", tx.note); putString("date", tx.date)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val tx = Transaction(id = args.getLong("id"), type = args.getString("type", ""),
            amount = args.getDouble("amount"), category = args.getString("category", ""),
            note = args.getString("note", ""), date = args.getString("date", ""))

        return AlertDialog.Builder(requireContext())
            .setTitle("Hapus Transaksi?")
            .setMessage("Tindakan ini tidak bisa dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                val dao = (requireActivity().application as FinnApp).database.transactionDao()
                lifecycleScope.launch { dao.delete(tx) }
            }
            .setNegativeButton("Batal", null)
            .create()
    }
}
