package com.example.finna.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.finna.data.dao.BudgetDao
import com.example.finna.data.dao.TransactionDao
import com.example.finna.data.model.Budget
import com.example.finna.data.model.Transaction

@Database(entities = [Transaction::class, Budget::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "finna_db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
