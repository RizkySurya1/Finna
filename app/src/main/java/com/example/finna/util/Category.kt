package com.example.finna.util

data class Category(val id: String, val label: String, val emoji: String, val colorHex: String)

object Categories {
    val all = listOf(
        Category("food",          "Makanan",    "🍔", "#FF9F43"),
        Category("transport",     "Transport",  "🚚", "#3498DB"),
        Category("shopping",      "Belanja",    "🛍", "#9B59B6"),
        Category("health",        "Kesehatan",  "💊", "#E74C3C"),
        Category("entertainment", "Hiburan",    "🎬", "#F39C12"),
        Category("education",     "Pendidikan", "📚", "#2ECC71"),
        Category("bills",         "Tagihan",    "🧾", "#1ABC9C"),
        Category("salary",        "Gaji",       "💼", "#27AE60"),
        Category("freelance",     "Freelance",  "💻", "#008080"),
        Category("investment",    "Investasi",  "📈", "#16A085"),
        Category("other",         "Lainnya",    "✨", "#95A5A6")
    )
    val income = all.filter { it.id in listOf("salary", "freelance", "investment", "other") }
    val expense = all.filter { it.id !in listOf("salary", "freelance", "investment") }

    fun find(id: String) = all.find { it.id == id } ?: all.last()

    fun formatRp(amount: Double): String =
        "Rp " + String.format("%,.0f", amount).replace(",", ".")
}
