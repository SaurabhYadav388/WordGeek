package com.example.wordgeek.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.example.wordgeek.R

class custom_toast_helper {
    fun showCustomToast(context: Context) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT

        toast.setGravity(Gravity.CENTER, 0, 0) // Set gravity to center

        toast.view = layout

        toast.show()
    }
}




