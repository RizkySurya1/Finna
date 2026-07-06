package com.example.finna.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.finna.FinnApp

class AnalyticsViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as FinnApp).database.transactionDao()
    val allTransactions = dao.getAllTransactions().asLiveData()
}
