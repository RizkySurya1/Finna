package com.example.finna.ui.dialog

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.finna.FinnApp
import com.example.finna.data.model.Transaction
import com.example.finna.databinding.BottomSheetAddEditTransactionBinding
import com.example.finna.util.Categories
import com.example.finna.util.Category
import com.example.finna.util.colorRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTransactionBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddEditTransactionBinding? = null
    private val binding get() = _binding!!

    private var editTransaction: Transaction? = null
    private var selectedCategory = "food"
    private var selectedDate = ""
    private var transactionType = "expense"

    companion object {
        fun newInstance(tx: Transaction? = null) = AddEditTransactionBottomSheet().apply {
            arguments = Bundle().apply {
                tx?.let {
                    putLong("id", it.id); putString("type", it.type)
                    putDouble("amount", it.amount); putString("category", it.category)
                    putString("note", it.note); putString("date", it.date)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            if (args.containsKey("id")) {
                editTransaction = Transaction(
                    id = args.getLong("id"), type = args.getString("type", "expense"),
                    amount = args.getDouble("amount"), category = args.getString("category", "food"),
                    note = args.getString("note", ""), date = args.getString("date", "")
                )
                transactionType = editTransaction!!.type
                selectedCategory = editTransaction!!.category
                selectedDate = editTransaction!!.date
                binding.tvTitle.text = "Edit Transaksi"
                binding.etAmount.setText(editTransaction!!.amount.toInt().toString())
                binding.etNote.setText(editTransaction!!.note)
            }
        }

        if (selectedDate.isEmpty()) {
            val c = Calendar.getInstance()
            selectedDate = String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        }
        updateDateLabel()

        setupTypeToggle()
        setupCategoryChips()
        binding.rowDate.setOnClickListener { showDatePicker() }
        binding.btnSimpan.setOnClickListener { save() }
        binding.btnClose.setOnClickListener { dismiss() }

        // Bersihkan "0" default begitu field disentuh, supaya user tidak perlu hapus manual
        binding.etAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.etAmount.text.toString() == "0") binding.etAmount.setText("")
        }
    }

    private fun setupTypeToggle() {
        fun update() {
            val expenseActive = transactionType == "expense"
            binding.btnExpense.setBackgroundResource(
                if (expenseActive) com.example.finna.R.drawable.bg_pill_expense_active
                else com.example.finna.R.drawable.bg_pill_inactive
            )
            binding.btnExpense.setTextColor(if (expenseActive) Color.WHITE else requireContext().colorRes(com.example.finna.R.color.muted_foreground))
            binding.btnIncome.setBackgroundResource(
                if (!expenseActive) com.example.finna.R.drawable.bg_pill_income_active
                else com.example.finna.R.drawable.bg_pill_inactive
            )
            binding.btnIncome.setTextColor(if (!expenseActive) Color.WHITE else requireContext().colorRes(com.example.finna.R.color.muted_foreground))

            // Tombol Simpan ikut warna sesuai jenis transaksi
            val accentColor = requireContext().colorRes(
                if (expenseActive) com.example.finna.R.color.expense_color else com.example.finna.R.color.income
            )
            binding.btnSimpan.backgroundTintList = ColorStateList.valueOf(accentColor)

            setupCategoryChips()
        }
        update()
        binding.btnExpense.setOnClickListener { transactionType = "expense"; update() }
        binding.btnIncome.setOnClickListener { transactionType = "income"; update() }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.removeAllViews()
        val cats = if (transactionType == "income") Categories.income else Categories.expense
        if (cats.none { it.id == selectedCategory }) selectedCategory = cats.first().id

        cats.forEach { cat ->
            val chip = buildCategoryChip(cat, isSelected = cat.id == selectedCategory) {
                selectedCategory = cat.id
                setupCategoryChips()
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    /** Chip kategori kustom: border & teks mengikuti warna kategori saat dipilih. */
    private fun buildCategoryChip(cat: Category, isSelected: Boolean, onClick: () -> Unit): TextView {
        val catColor = Color.parseColor(cat.colorHex)
        val dp = { v: Int -> (v * resources.displayMetrics.density).toInt() }
        return TextView(requireContext()).apply {
            text = "${cat.emoji}  ${cat.label}"
            textSize = 13f
            setPadding(dp(14), dp(9), dp(14), dp(9))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                if (isSelected) {
                    setColor(Color.WHITE)
                    setStroke(dp(2), catColor)
                } else {
                    setColor(requireContext().colorRes(com.example.finna.R.color.surface_muted))
                }
            }
            setTextColor(if (isSelected) catColor else requireContext().colorRes(com.example.finna.R.color.text_secondary))
            setTypeface(typeface, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { onClick() }
        }
    }

    private fun updateDateLabel() {
        val parts = selectedDate.split("-")
        try {
            val cal = Calendar.getInstance().apply {
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
            binding.tvSelectedDate.text = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(cal.time)
        } catch (e: Exception) {
            binding.tvSelectedDate.text = selectedDate
        }
    }

    private fun showDatePicker() {
        val parts = selectedDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireContext(), { _, year, month, day ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
            updateDateLabel()
        }, y, m, d).show()
    }

    private fun save() {
        // Ambil hanya digit dari input supaya tahan terhadap pemisah ribuan/desimal
        // apapun (titik, koma, spasi) yang mungkin muncul tergantung locale keyboard.
        val rawDigits = binding.etAmount.text.toString().replace(Regex("[^0-9]"), "")
        val amount = rawDigits.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Masukkan jumlah yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val dao = (requireActivity().application as FinnApp).database.transactionDao()
        val tx = Transaction(id = editTransaction?.id ?: 0, type = transactionType,
            amount = amount, category = selectedCategory,
            note = binding.etNote.text.toString(), date = selectedDate)

        binding.btnSimpan.isEnabled = false
        lifecycleScope.launch {
            try {
                if (editTransaction != null) dao.update(tx) else dao.insert(tx)
                Toast.makeText(requireContext(), "Transaksi tersimpan", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                binding.btnSimpan.isEnabled = true
                Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
