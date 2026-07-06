package com.example.finna

import android.app.Application
import com.example.finna.data.AppDatabase

class FinnApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
