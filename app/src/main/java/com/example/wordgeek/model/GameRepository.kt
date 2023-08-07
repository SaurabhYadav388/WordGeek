package com.example.wordgeek.model

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

class GameRepository() {

    //add word dao if used
    val wordDataBaseObj=WordDataBase()

    fun getWordList(wordSize:Int,context: Context): List<String> {
        return wordDataBaseObj.getWordList(wordSize, context)
    }



   /*fun getwordsof(lifecycleOwner: LifecycleOwner,wordDao:WordDao, wordSize:Int): LiveData<List<WordEntity>> {

        /*val wordinEntity= wordDao.getWordsofSize(wordSize)
        val myList: MutableList<String> = mutableListOf()
        for(we in wordinEntity)
            myList.add(we.word)
        return myList.toList()*/
    }*/
}