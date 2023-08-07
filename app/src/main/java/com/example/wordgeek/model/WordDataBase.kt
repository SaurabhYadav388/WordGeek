package com.example.wordgeek.model

import android.content.Context
import java.io.InputStream

class WordDataBase()
{
    fun getWordList(wordSize: Int, context: Context): List<String> {
        val fileName = "$wordSize.txt"
        val file: InputStream = context.assets.open(fileName)
        var l= file.bufferedReader().readLines().toList().toMutableList()
        for(i in 0 until l.size)
            l[i]=l[i].uppercase()

        return l
    }

}