package com.example.finna.ui.dialog

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.finna.FinnApp
import com.example.finna.data.model.Budget
import com.example.finna.databinding.BottomSheetAddBudgetBinding
import com.example.finna.util.Categories
import com.example.finna.util.Category
import com.example.finna.util.colorRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class AddBudgetBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddBudgetBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory = "food"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryChips()

        binding.etLimit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.etLimit.text.toString() == "0") binding.etLimit.setText("")
        }

        binding.btnSimpan.setOnClickListener {
            val rawDigits = binding.etLimit.text.toString().replace(Regex("[^0-9]"), "")
            val limit = rawDigits.toDoubleOrNull()
            if (limit == null || limit <= 0) {
                Toast.makeText(requireContext(), "Masukkan batas anggaran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dao = (requireActivity().application as FinnApp).database.budgetDao()
            binding.btnSimpan.isEnabled = false
            lifecycleScope.launch {
                try {
                    dao.insert(Budget(category = selectedCategory, limitAmount = limit))
                    Toast.makeText(requireContext(), "Anggaran tersimpan", Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (e: Exception) {
                    binding.btnSimpan.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnBatal.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategory.removeAllViews()
        Categories.expense.forEach { cat ->
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
