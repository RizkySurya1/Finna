package com.example.finna.util

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.finna.R

/**
 * Terapkan background lingkaran (bukan kotak) berwarna soft sesuai warna kategori
 * ke sebuah container ikon/emoji. Menggantikan pemakaian setBackgroundColor() yang
 * menghapus bentuk oval bawaan drawable dan membuatnya tampak kotak.
 */
fun FrameLayout.applyCategoryCircleBackground(colorHex: String, alphaHex: String = "30") {
    background = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.parseColor(colorHex + alphaHex))
    }
}

/** Set warna teks nominal transaksi: hijau (income) / merah (expense) dari colors.xml. */
fun TextView.applyAmountColor(type: String) {
    val colorRes = if (type == "income") R.color.income else R.color.expense_color
    setTextColor(ContextCompat.getColor(context, colorRes))
}

/** Ambil warna dari colors.xml lewat Context, membungkus ContextCompat.getColor. */
fun android.content.Context.colorRes(resId: Int): Int = ContextCompat.getColor(this, resId)
