package com.example.wordgeek.model

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WordEntity::class], version = 1)
abstract class WordDatabaseRoom :RoomDatabase(){

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: WordDatabaseRoom? = null

        fun getInstance(context: Context): WordDatabaseRoom {
            return INSTANCE ?: synchronized(this) {//synchronized only one thread Volatile only one access point
                val instance = Room.databaseBuilder(//persist in local strage
                    context.applicationContext,
                    WordDatabaseRoom::class.java,
                    "word_database")
                    .createFromAsset("merge456.txt")
                    .build()


                Log.e("init room database","first time")
                INSTANCE = instance
                instance
            }
        }
    }
}