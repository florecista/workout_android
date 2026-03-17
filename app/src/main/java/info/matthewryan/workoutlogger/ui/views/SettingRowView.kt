package info.matthewryan.workoutlogger.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import info.matthewryan.workoutlogger.databinding.ViewSettingRowBinding

class SettingRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding =
        ViewSettingRowBinding.inflate(LayoutInflater.from(context), this, true)

    var isChecked: Boolean
        get() = binding.switchView.isChecked
        set(value) {
            binding.switchView.isChecked = value
            updateColor(value)
        }

    var isRowEnabled: Boolean = true
        set(value) {
            field = value
            binding.switchView.isEnabled = value
        }

    init {
        // Row click toggles switch
        binding.root.setOnClickListener {
            if (isRowEnabled) {
                isChecked = !isChecked
            } else {
                Toast.makeText(
                    context,
                    "This setting cannot be changed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun setTitle(text: String) {
        binding.title.text = text
    }

    fun setDescription(text: String) {
        binding.description.text = text
    }

    private fun updateColor(isChecked: Boolean) {
        binding.switchView.thumbTintList =
            android.content.res.ColorStateList.valueOf(
                if (isChecked)
                    resources.getColor(android.R.color.holo_green_dark, null)
                else
                    resources.getColor(android.R.color.darker_gray, null)
            )
    }
}