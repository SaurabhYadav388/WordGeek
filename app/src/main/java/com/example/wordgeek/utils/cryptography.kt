package com.example.wordgeek.utils

class cryptography {
    private val key=5
    fun Encrypt(word:String) :String
    {
        val shiftedWord = StringBuilder()

        for (i in 0 until word.length) {
            shiftedWord.append(  ( ('A'+(word[i].uppercaseChar().code-65+key)%26) )  )
        }

        return shiftedWord.toString()
    }
    fun Decrypt(word:String) :String
    {
        val shiftedWord = StringBuilder()

        for (i in 0 until word.length) {
            shiftedWord.append(  'A'+((word[i].code-65-key +26)%26) )
        }

        return shiftedWord.toString()
    }
}