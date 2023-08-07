package com.example.wordgeek.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameViewModelFactory(val gameSize:Int,val guessSize:Int,val wordSize:Int,val linkWord:String,val context: Context) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(gameSize, guessSize, wordSize,linkWord,context) as T
    }

}