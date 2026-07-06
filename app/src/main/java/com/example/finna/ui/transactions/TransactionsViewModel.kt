package com.example.finna.ui.transactions

import android.app.Application
import androidx.lifecycle.*
import com.example.finna.FinnApp
import com.example.finna.data.model.Transaction
import com.example.finna.util.Categories

class TransactionsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as FinnApp).database.transactionDao()
    val allTransactions = dao.getAllTransactions().asLiveData()
    val filterType = MutableLiveData("all")
    val searchQuery = MutableLiveData("")

    val filteredTransactions = MediatorLiveData<List<Transaction>>().apply {
        fun update() {
            val all = allTransactions.value ?: return
            val filter = filterType.value ?: "all"
            val query = searchQuery.value?.trim()?.lowercase() ?: ""
            value = all
                .filter { if (filter == "all") true else it.type == filter }
                .filter { tx ->
                    if (query.isEmpty()) return@filter true
                    val categoryLabel = Categories.find(tx.category).label.lowercase()
                    tx.note.lowercase().contains(query) ||
                        categoryLabel.contains(query) ||
                        tx.category.lowercase().contains(query)
                }
        }
        addSource(allTransactions) { update() }
        addSource(filterType) { update() }
        addSource(searchQuery) { update() }
    }
}
