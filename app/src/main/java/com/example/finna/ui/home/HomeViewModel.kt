package com.example.finna.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.finna.FinnApp
import kotlinx.coroutines.flow.map

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as FinnApp).database.transactionDao()

    val allTransactions = dao.getAllTransactions().asLiveData()
    val recentTransactions = dao.getAllTransactions().map { it.take(5) }.asLiveData()
    val totalIncome = dao.getTotalIncome().map { it ?: 0.0 }.asLiveData()
    val totalExpense = dao.getTotalExpense().map { it ?: 0.0 }.asLiveData()
}
