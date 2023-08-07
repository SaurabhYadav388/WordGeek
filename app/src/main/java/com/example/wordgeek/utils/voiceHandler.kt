package com.example.wordgeek.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*

class voiceHandler(context: Context)
{
    var voiceWord= MutableLiveData<String>()

    val context=context
    val speechRecognizer= SpeechRecognizer.createSpeechRecognizer(context)
    val speechRecognizerIntent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)


    init {
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(p0: Bundle?) {
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(p0: Float) {
            }

            override fun onBufferReceived(p0: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(p0: Int) {
            }

            override fun onResults(bundle: Bundle?) {
                val data=bundle!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                //voiceWord=data!![0].uppercase()
                Log.d("inside result",data!![0].uppercase())
                voiceWord.value=data!![0].uppercase()///
                //activity.writeVoiceWord(voiceWord)

//                Toast.makeText(context,voiceWord,Toast.LENGTH_LONG).show()
//
            }

            override fun onPartialResults(p0: Bundle?) {
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
            }

        })

    }
}