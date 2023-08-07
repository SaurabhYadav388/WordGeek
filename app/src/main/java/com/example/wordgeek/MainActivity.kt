package com.example.wordgeek

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wordgeek.databinding.ActivityMainBinding
import com.example.wordgeek.model.WordDataBase
import com.example.wordgeek.ui.ClassicActivity
import com.example.wordgeek.ui.MultiordleActivity
import com.example.wordgeek.ui.TwoPlayerDemo
import com.example.wordgeek.ui.TwoPlayerOnlineActivity
import com.example.wordgeek.utils.cryptography
import com.example.wordgeek.utils.generateLinkHandler


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.classicBtn.setOnClickListener {
            val intent= Intent(this,ClassicActivity::class.java)
            intent.putExtra("WordSize", binding.counterClassic.textViewCounter.text.toString().toInt());
            startActivity(intent)
        }
        binding.dordleBtn.setOnClickListener {
            val intent= Intent(this,MultiordleActivity::class.java)
            intent.putExtra("GameSize", 2)
            intent.putExtra("GuessSize", 7)
            intent.putExtra("WordSize", binding.counterDordle.textViewCounter.text.toString().toInt());
            startActivity(intent)

        }
        binding.quordleBtn.setOnClickListener {
            val intent= Intent(this,MultiordleActivity::class.java)
            intent.putExtra("GameSize", 4)
            intent.putExtra("GuessSize", 9)
            intent.putExtra("WordSize", binding.counterQuordle.textViewCounter.text.toString().toInt());
            startActivity(intent)
        }
        binding.octordleBtn.setOnClickListener {
            val intent= Intent(this,MultiordleActivity::class.java)
            intent.putExtra("GameSize", 8)
            intent.putExtra("GuessSize", 13)
            intent.putExtra("WordSize", binding.counterOctordle.textViewCounter.text.toString().toInt());
            startActivity(intent)
        }
        binding.challengeFriendBtn.setOnClickListener {
            showChallengeDialog()
        }

        binding.multiPlayerbtn.setOnClickListener {
            //val intent= Intent(this,TwoPlayerDemo::class.java)
            val intent= Intent(this,TwoPlayerOnlineActivity::class.java)
            startActivity(intent)
        }



    }

    fun decreaseCounter(view: View) {
        val parent = view.parent as ViewGroup
        val textViewCounter = parent.findViewById<TextView>(R.id.textViewCounter)
        val counter = textViewCounter.text.toString().toInt()
        val minCounter = 4 // Set your minimum counter value here

        if (counter > minCounter) {
            textViewCounter.text = (counter - 1).toString()
        }
    }

    fun increaseCounter(view: View) {
        val parent = view.parent as ViewGroup
        val textViewCounter = parent.findViewById<TextView>(R.id.textViewCounter)
        val counter = textViewCounter.text.toString().toInt()
        val maxCounter = 6 // Set your maximum counter value here

        if (counter < maxCounter) {
            textViewCounter.text = (counter + 1).toString()
        }
    }

    ///////////Challenge dialog////////////////////////////////
    fun showChallengeDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_challenge)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)


        val wordInput = dialog.findViewById<EditText>(R.id.edit_word)

        dialog.findViewById<Button>(R.id.button_copy_link).setOnClickListener {
            val enteredWord = wordInput.text.toString().uppercase()

            if (isValidWord(enteredWord)) {
                val generateLinkHandlerObj= generateLinkHandler()
                generateLinkHandlerObj.generateWordLink(enteredWord,applicationContext)
                dialog.findViewById<TextView>(R.id.text_word_validation).text="Valid Word"
            } else {
                dialog.findViewById<TextView>(R.id.text_word_validation).text="Invalid Word"
            }
        }

        dialog.findViewById<ImageButton>(R.id.button_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun isValidWord(word: String): Boolean {
        val wordLength = word.length
        if(wordLength in 4..6 )
        {
            val wordDataBaseObj= WordDataBase()
            val givenWordList=wordDataBaseObj.getWordList(wordLength,this)
            var s=0
            var l=givenWordList.size-1//should be size-1
            while (s<=l)
            {

                val mid=(s+l)/2
                val wTemp=givenWordList[mid]
                Log.d(wTemp,word)
                if(wTemp==word)
                    return true
                else if(word<wTemp)
                    l=mid-1
                else
                    s=mid+1
            }
        }
        return false
    }

}