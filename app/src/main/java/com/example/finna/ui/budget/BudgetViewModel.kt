package com.example.finna.ui.budget

import android.app.Application
import androidx.lifecycle.*
import com.example.finna.FinnApp
import com.example.finna.data.model.Budget
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(app: Application) : AndroidViewModel(app) {
    private val budgetDao = (app as FinnApp).database.budgetDao()
    private val txDao = (app as FinnApp).database.transactionDao()

    val budgets = budgetDao.getAllBudgets().asLiveData()

    val currentMonthTransactions = run {
        val c = Calendar.getInstance()
        val prefix = String.format("%04d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
        txDao.getTransactionsByMonth(prefix).asLiveData()
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch { budgetDao.delete(budget) }
}
