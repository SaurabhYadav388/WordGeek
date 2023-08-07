package com.example.wordgeek.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAllWords(): LiveData<List<WordEntity>>

    @Query("SELECT * FROM words WHERE LENGTH(word) = :wordSize")
    fun getWordsofSize(wordSize:Int):LiveData<List<WordEntity>>//livedata?
}