package com.example.wordgeek.viewmodels

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.*
import com.example.wordgeek.DictionaryEntry
import com.example.wordgeek.model.GameRepository
import com.example.wordgeek.utils.voiceHandler
import kotlin.random.Random


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.example.wordgeek.DictionaryService
import com.example.wordgeek.R
import com.example.wordgeek.model.WordDatabaseRoom
import com.example.wordgeek.model.WordEntity
import com.example.wordgeek.utils.custom_toast_helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(val gameSize:Int,val guessSize:Int,val wordSize:Int,val linkWord:String,context:Context):ViewModel()
{
    var dictionaryInfoViewModel =MutableLiveData<List<DictionaryEntry>>()
    var voiceWordObserveFirstTime=true
    var guessIndexViewModel=MutableLiveData<Int>(0)
    var wordIndexViewModel=MutableLiveData<Int>(0)

    var wordGuesses=MutableLiveData<Array<Array<CharArray>>>(Array(gameSize){Array(guessSize) { CharArray(wordSize) {' '} }})
    var latestGuess=CharArray(wordSize){' '}
    var wordGuessColor=MutableLiveData<Array<Array<IntArray>>>(Array(gameSize){Array(guessSize) { IntArray(wordSize) {0} }})
    var voiceHandlerobj=voiceHandler(context)

    var repository= GameRepository()
    var wordList=repository.getWordList(wordSize,context)//

    //var repository=GameRepository()
    //val database=WordDatabaseRoom.getInstance(context)
    //val wordDao = database.wordDao()


    var wordsToGuess=if(linkWord=="") MutableList(gameSize){index ->  giveRandomWords(index)}
                                else { MutableList(1){ linkWord}}

    var gameCompleted=MutableLiveData<Array<Boolean>>(Array(gameSize){false})
    var gameEnd=MutableLiveData<Boolean>(false)

    val context_vm=context/////


    fun giveRandomWords(dummy: Int): String {

        return wordList[ (0 until wordList.size).random(Random(System.currentTimeMillis()+dummy)) ]
    }

    fun letterClick(btnText: Char)
    {
        Log.d("letter click",btnText.toString())

        if (wordIndexViewModel.value!! < wordSize) {

            //for keeping latest guess
            latestGuess[wordIndexViewModel.value!!] = btnText
            Log.d("lg", "${latestGuess[wordIndexViewModel.value!!]}")
            ////////////////////////

            val temp = wordGuesses.value

            for (game in 0 until gameSize) {
                if (gameCompleted.value!![game] == false) {
                    temp!![game][guessIndexViewModel.value!!][wordIndexViewModel.value!!] = btnText
                }
            }
            wordIndexViewModel.value =
                wordIndexViewModel.value!! + 1//first increase observe than rest
            wordGuesses.value = temp
        }


    }
    fun backspaceClick()
    {
        if (wordIndexViewModel.value!! > 0)
        {
            //for keeping latest guess
            latestGuess[wordIndexViewModel.value!!-1]=' '
            ////////////////////////

            val temp = wordGuesses.value
            for(game in 0 until gameSize)
            {
                if (gameCompleted.value!![game] == false)
                {
                    temp!![game][(guessIndexViewModel.value!!)][wordIndexViewModel.value!! - 1]= ' '//remove prev
                }
            }
            wordGuesses.value = temp
            wordIndexViewModel.value =
                wordIndexViewModel.value!! - 1// observe than decrease
        }


    }
    fun clearClick()
    {
        val temp = wordGuesses.value
        for(game in 0 until gameSize) {
            if (gameCompleted.value!![game] == false) {
                if (wordIndexViewModel.value != 0) {
                    var k = wordIndexViewModel.value!!
                    while (k != 0) {
                        temp!![game][guessIndexViewModel.value!!][k - 1]=' '
                        k--
                    }
                }
            }
        }
        latestGuess.fill(' ')
        wordGuesses.value = temp
        wordIndexViewModel.value = 0
    }
    fun submitClick()
    {
        for(words in wordsToGuess)
            Log.d("word:","$words ")
        if(wordIndexViewModel.value==wordSize)
        {   Log.d("inside","if ${wordsToGuess[0]}" )
            //val currentGuess= wordGuesses.value!![guessIndexViewModel.value!!]

            if(validGuessWord( String(latestGuess)) )
            {
                computeLetterColors(latestGuess)
                guessIndexViewModel.value=guessIndexViewModel.value!!+1
                wordIndexViewModel.value=0

                //checks if all value boolarray true (win) or ri=rs(lose)
                if(gameCompleted.value!!.all { it })
                {
                    gameEnd.value=true
                }

                else if(guessIndexViewModel.value==guessSize)
                    gameEnd.value=true

            }
            else
            {
                //invalid word
                //Toast.makeText(context_vm,"Invalid Word",Toast.LENGTH_SHORT).show()
                val ctoast=custom_toast_helper()
                ctoast.showCustomToast(context_vm)

            }
        }
    }

    fun listenWord(lifecycleOwner: LifecycleOwner)
    {
        voiceHandlerobj.speechRecognizer.startListening(voiceHandlerobj.speechRecognizerIntent)
        Log.e("bool val",voiceWordObserveFirstTime.toString())
        if(voiceWordObserveFirstTime)
        {
            Log.d("inside if","enter")
            voiceHandlerobj.voiceWord.observe(lifecycleOwner){
                Log.d("voice word:",it)

                clearClick()///

                latestGuess=it.substring(0,wordSize).toCharArray()////////////giving error on smaller string TODO
                Log.d("voice latestguess:",String(latestGuess))


                var temp=wordGuesses.value

                for(game in 0 until gameSize)
                {
                    if (gameCompleted.value!![game] == false) {
                        var tempWordIndex=0
                        while(tempWordIndex< minOf(wordSize,it.length)) {
                            temp!![game][guessIndexViewModel.value!!][tempWordIndex] = it[tempWordIndex]
                            tempWordIndex++
                        }
                    }
                }
                wordIndexViewModel.value=minOf(wordSize,it.length)
                Log.d("wordIndexViewModel.value","${wordIndexViewModel.value}")
                wordGuesses.value=temp

            }
        }
        voiceWordObserveFirstTime=false

    }

    private fun computeLetterColors(currentGuess: CharArray) {
        val temp=wordGuessColor.value

        for(game in 0 until gameSize)
        {
            if(gameCompleted.value!![game] == false)
            {
                if(String(currentGuess) == wordsToGuess[game])
                {
                    gameCompleted.value!![game]=true
                }
                var notGreenLetter= ArrayList<Char>()
                for(i in 0 until wordSize)
                {
                    if(currentGuess[i]==wordsToGuess[game][i])
                    {
                        temp!![game][guessIndexViewModel.value!!][i]=2
                    }
                    else
                        notGreenLetter.add(wordsToGuess[game][i])
                }
                for(i in 0 until wordSize)
                {
                    if(temp!![game][guessIndexViewModel.value!!][i] == 0 )
                    {
                        if(currentGuess[i] in notGreenLetter)
                            temp!![game][guessIndexViewModel.value!!][i]=1
                        else
                            temp!![game][guessIndexViewModel.value!!][i]=-1
                    }
                }
            }
        }
        wordGuessColor.value=temp
    }

    private fun validGuessWord(userGuess: String): Boolean {
        Log.d("compute valid","${userGuess}" )

        var s=0
        var l=wordList.size-1//should be size-1
        while (s<=l)
        {

            var mid=(s+l)/2
            var wTemp=wordList[mid]

            if(wTemp==userGuess)
            {
                return true
            }
            else if(userGuess<wTemp)
                l=mid-1
            else
                s=mid+1
        }
        return false

    }

    fun gameEndResultDialogShow(gameEndDialog: Dialog,context:Context)
    {
        if(gameEnd.value==true)
        {
            gameEndDialog.findViewById<TextView>(R.id.textViewResult).text=
                if(gameCompleted.value!!.all { it }){"YOU WON"}
                else{"YOU LOST"}

            for(word in wordsToGuess)
                gameEndDialog.findViewById<TextView>(R.id.textViewWordtoGuess).append(word+"\n")

            // Enable clickable links within the TextView
            gameEndDialog.findViewById<TextView>(R.id.textViewWordtoGuess).movementMethod = LinkMovementMethod.getInstance()

            // Set a custom ClickableSpan to handle clicks on the words
            val spannable = gameEndDialog.findViewById<TextView>(R.id.textViewWordtoGuess).text as Spannable

            val words = spannable.toString().split("\n")
            var startIndex = 0
            for (word in words) {
                val clickSpan = object : ClickableSpan() {
                    override fun onClick(view: View) {
                        Log.d("Clicked:"," $word")
                        viewModelScope.launch {
                            getWordDictionaryDetails(word, context)
                        }

                    }
                }
                val endIndex = startIndex + word.length
                spannable.setSpan(clickSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)//give position
                startIndex = endIndex + 1  // Add 1 for the newline character
            }
/*
//used dialogshow logic in view itself
            viewModelScope.launch {
                if(gameCompleted.value!!.all{it}==true)
                    delay(3000)

                gameEndDialog.show()
            }
*/

        }
    }

//**********************************Meaning API***************************************************

    // Create Retrofit instance
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.dictionaryapi.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create DictionaryService instance
    val dictionaryService = retrofit.create(DictionaryService::class.java)

    // Make API request
    fun getWordDictionaryDetails(word:String,context: Context)
    {
        val call = dictionaryService.getWordDefinition(word)
        call.enqueue(object : Callback<List<DictionaryEntry>>
        {
            override fun onResponse(call: Call<List<DictionaryEntry>>, response: Response<List<DictionaryEntry>>)
            {
                if (response.isSuccessful) {
                    Log.d("response", "successfull");
                    val entries = response.body()
                    // Process the response data here
                    if (entries != null) {
                        Log.d("result", "${entries?.get(0).toString()}");
                        handleResponse(entries)
                    } else {
                        Log.d("entries null", "is null");
                        Toast.makeText(context, "null element", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.d("resp not successfull", "!succ");
                    // Handle API error
                    Toast.makeText(context, "Response unsuccessfull", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<DictionaryEntry>>, t: Throwable) {
                // Handle network or other errors
                Toast.makeText(context, "Failure:network!!", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun handleResponse(entries: List<DictionaryEntry>) {

        dictionaryInfoViewModel.value=entries
        Log.d("entry at","handle response")
    }

    fun userGiveUp() {
        Log.d("give","up")
        gameEnd.value=true
    }


}



