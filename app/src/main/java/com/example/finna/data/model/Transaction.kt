package com.example.finna.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,      // "income" atau "expense"
    val amount: Double,
    val category: String,
    val note: String,
    val date: String       // format: "yyyy-MM-dd"
)
