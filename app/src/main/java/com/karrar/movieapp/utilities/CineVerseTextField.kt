package com.karrar.movieapp.utilities

import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.karrar.movieapp.R

class CineVerseTextField(
    private val container: View,
    leadingIconRes: Int,
    private val isPassword: Boolean = false
) {
    private val label: TextView = container.findViewById(R.id.textFieldLabel)
    private val inputContainer: LinearLayout = container.findViewById(R.id.inputContainer)
    private val leadingIcon: ImageView = container.findViewById(R.id.leadingIcon)
    private val editText: EditText = container.findViewById(R.id.editText)
    private val trailingIcon: ImageView = container.findViewById(R.id.trailingIcon)
    private val errorMessage: TextView = container.findViewById(R.id.errorMessage)
    private val forgotPassword: TextView = container.findViewById(R.id.forgotPassword)

    private var passwordVisible = false

    init {
        leadingIcon.setImageResource(leadingIconRes)

        // Set the custom cursor
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            editText.textCursorDrawable = ContextCompat.getDrawable(
                container.context,
                R.drawable.cineverse_cursor
            )
        } else {
            // For older versions, use reflection or set via theme
            try {
                val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                f.isAccessible = true
                f.set(editText, R.drawable.cineverse_cursor)
            } catch (e: Exception) {
                // Fallback - cursor will use default color
            }
        }

        if (isPassword) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            forgotPassword.visibility = View.VISIBLE
            trailingIcon.visibility = View.VISIBLE
            trailingIcon.setImageResource(R.drawable.outline_eye_closed)
            trailingIcon.setColorFilter(
                ContextCompat.getColor(container.context, R.color.shade_color_secondary),
                android.graphics.PorterDuff.Mode.SRC_IN
            )

            trailingIcon.setOnClickListener {
                togglePasswordVisibility()
            }
        }
    }

    fun setLabel(text: String) {
        label.text = text
        label.visibility = View.VISIBLE
    }

    fun setHint(hint: String) {
        editText.hint = hint
    }

    fun setError(error: String?) {
        if (error.isNullOrEmpty()) {
            errorMessage.visibility = View.GONE
            inputContainer.isActivated = false
            if (!isPassword) trailingIcon.visibility = View.GONE
        } else {
            errorMessage.text = error
            errorMessage.visibility = View.VISIBLE
            inputContainer.isActivated = true
            if (!isPassword) {
                trailingIcon.visibility = View.VISIBLE
                trailingIcon.setImageResource(R.drawable.outline_danger_triangle)
                trailingIcon.setColorFilter(
                    ContextCompat.getColor(
                        container.context,
                        R.color.additional_color_primary_color_red
                    ),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    fun getText(): String = editText.text.toString()

    fun setText(text: String) {
        editText.setText(text)
    }

    fun addTextChangedListener(listener: (String) -> Unit) {
        editText.addTextChangedListener {
            listener(it.toString())
        }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        if (passwordVisible) {
            editText.transformationMethod = null
            trailingIcon.setImageResource(R.drawable.outline_eye_opened)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            trailingIcon.setImageResource(R.drawable.outline_eye_closed)
        }
        editText.setSelection(editText.text.length)
    }
}