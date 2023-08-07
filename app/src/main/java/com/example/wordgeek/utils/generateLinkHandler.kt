package com.example.wordgeek.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log

class generateLinkHandler
{
    fun generateWordLink(enteredWord: String,applicationContext: Context){
        val cryptObj=cryptography()
        var generatedLink="http://wordgeek.challenge.play/?word="+cryptObj.Encrypt(enteredWord)
        Log.d(cryptObj.Encrypt(enteredWord),cryptObj.Decrypt(cryptObj.Encrypt(enteredWord)))
        val clipboardManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", generatedLink)
        clipboardManager.setPrimaryClip(clipData)

    }
}