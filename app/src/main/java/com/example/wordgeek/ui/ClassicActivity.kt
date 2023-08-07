package com.example.wordgeek.ui

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wordgeek.DictionaryEntry
import com.example.wordgeek.MainActivity
import com.example.wordgeek.R
import com.example.wordgeek.databinding.ActivityClassicBinding
import com.example.wordgeek.utils.cryptography
import com.example.wordgeek.utils.generateLinkHandler
import com.example.wordgeek.viewmodels.GameViewModel
import com.example.wordgeek.viewmodels.GameViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit


private lateinit var binding: ActivityClassicBinding
private lateinit var gameViewModel: GameViewModel
private lateinit var gameEndDialog:Dialog
private lateinit var gameHelpDialog:Dialog
private var gameSize:Int=1
private var wordSize:Int=0
private var guessSize:Int=0
private var guessIndex:Int=0
private var wordIndex:Int=0

class ClassicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassicBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        gameSize=1
        guessSize=6////

        onNewIntent(intent)

        for (i in 0 until guessSize)
        {
            layoutInflater.inflate(R.layout.onewordrow_view, binding.OneGameLayout)

            for (j in 0 until wordSize)
            {
                Log.d("reach","here2")
                val viewGroup: ViewGroup =binding.OneGameLayout.getChildAt(i).findViewById(R.id.onewordrow_view_id)
                layoutInflater.inflate(R.layout.classictextbox_view,viewGroup)
                //as all textbox using one textbox ; replacing background ,next game uses last assigned color for all textbox
                // /can be optimized
                val gd=getTextBox(i,j).background
                if(gd is GradientDrawable)
                    gd.setColor(Color.WHITE)

            }
        }


        binding.DEL.setOnClickListener {
            gameViewModel.backspaceClick()
        }
        binding.CLR.setOnClickListener {
            gameViewModel.clearClick()
        }
        binding.submitBut.setOnClickListener {
            gameViewModel.submitClick()
            //gameViewModel.getDetail(String( gameViewModel.wordGuesses.value!![gameViewModel.guessIndexViewModel.value!!-1]),this)
        }
        binding.giveupBtn.setOnClickListener{
            gameViewModel.userGiveUp()
        }

        gameViewModel.wordIndexViewModel.observe(this) {
            wordIndex = it
            Log.d("observed1","${wordIndex}")
        }
        gameViewModel.guessIndexViewModel.observe(this) {
            guessIndex = it
        }
        gameViewModel.wordGuesses.observe(this) {
            Log.d("observed","${wordIndex}")
            //if(wordIndex!=0)Toast.makeText(this, it[guessIndex][wordIndex-1].toString(),Toast.LENGTH_SHORT).show()
            for(game in 0 until gameSize)
            {
                for(i in 0 until guessIndex+1)
                {
                    if(i== guessIndex)
                        for(j in 0 until wordIndex)
                            getTextBox(i,j).text= gameViewModel.wordGuesses.value!![game][i][j].toString()
                    else
                        for(j in 0 until wordSize)
                            getTextBox(i,j).text= gameViewModel.wordGuesses.value!![game][i][j].toString()
                }
            }


        }
        gameViewModel.wordGuessColor.observe(this){
            for(game in 0 until 1)
            {
                for(i in 0 until guessIndex+1)
                {
                    for(j in 0 until wordSize)
                            if(gameViewModel.wordGuessColor.value!![game][i][j]!=0)
                            {
                                val gd=getTextBox(i,j).background
                                if(gd is GradientDrawable)
                                    gd.setColor(computedColor(gameViewModel.wordGuessColor.value!![game][i][j]))
                                //TODO Due to changing textbox color all activity using it is using the last color assigned
                            }

                }
            }
        }


        gameEndDialog=Dialog(this)////////
        gameEndDialog.setContentView(R.layout.dialog_game_end)
        gameEndDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        gameEndDialog.setCanceledOnTouchOutside(false)
        gameEndDialog.findViewById<ImageButton>(R.id.dialogHomeBtn).setOnClickListener {
            goHome()
        }
        gameEndDialog.findViewById<ImageButton>(R.id.dialogButtonPlayAgain).setOnClickListener{
            Log.d("play again","restart")
            //gameEndDialog.dismiss()/////////////////////
            restartActivity()
        }
        gameEndDialog.findViewById<Button>(R.id.buttonCopyLinkEnd).setOnClickListener {
            val generateLinkHandlerObj=generateLinkHandler()
            generateLinkHandlerObj.generateWordLink(gameViewModel.wordsToGuess[0],applicationContext)
            gameEndDialog.findViewById<TextView>(R.id.text_word_validation).text="Link Copied to Clipboard"
        }


        gameViewModel.gameEnd.observe(this){
            gameViewModel.gameEndResultDialogShow(gameEndDialog,this)
            if(gameViewModel.gameEnd.value==true) {
                lifecycleScope.launch {
                    if (gameViewModel.gameCompleted.value!!.all { it } == true) {
                        delay(3000)
                    }
                    gameEndDialog.show()
                }

                if(gameViewModel.gameCompleted.value!!.all{it}==true )
                {
                    /*
                    val party= Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.5, 0.3)
                    )

                     */
                    val party = Party(
                        angle = Angle.TOP,
                        shapes = listOf(Shape.Square),
                        size = listOf(Size.MEDIUM),
                        speed = 0f, // Increase the start speed for a more energetic animation//15f
                        maxSpeed = 30f, // Allow for higher maximum speed for more variation//40f
                        damping = 0.9f, // Reduce the damping for more bouncing effect
                        spread = 360, // Narrow the spread to form a semi-circle effect
                        colors = listOf(0xFFE082, 0xFF6E40, 0xE040FB, 0x64B5F6), // Vibrant and lively colors
                        emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(500), // Longer and denser emission//can increase!!
                        position = Position.Relative(0.5,0.3), // Slightly above the center of the screen
                        //timeToLive = 3000, // Particles last longer on the screen
                        //fadeOutEnabled = true // Enable smooth fade-out effect
                    )

                    binding.konfettiView.start(party)
                }

            }

        }


        binding.voice.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
                checkPermissions()
            }
            gameViewModel.listenWord(this)
        }

        binding.goHomeBtn.setOnClickListener {
            goHome()
        }
        gameHelpDialog=Dialog(this)////////
        gameHelpDialog.setContentView(R.layout.wordle_help)
        gameHelpDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        gameHelpDialog.setCanceledOnTouchOutside(true)
        binding.btnHelp.setOnClickListener {
            gameHelpDialog.show()
        }
        //***************************************************************

        var myword:String
        var phonetic:String
        var audio:String
        var partOfSpeech: String
        var definition: String
        var example: String



        gameViewModel.dictionaryInfoViewModel.observe(this){
            Log.d("entry at"," response observe")

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.word_info_layout)
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
            printDictionaryInfo(dialog,it)
        }

    }
/*
    override fun onBackPressed() {
        if (false) {
            //super.onBackPressed()
        } else {

        }
    }
*/
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) Toast.makeText(
            applicationContext, "back press",
            Toast.LENGTH_LONG
        ).show()
        return false
        // Disable back button..............
    }

    private fun goHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun restartActivity() {
        gameEndDialog.dismiss()//////////////// dkwhy for restart it is causing leak if not used line here
        val intent = intent
        finish() // Close the current activit
        startActivity(intent) // Start the activity again
    }

    private fun printDictionaryInfo(dialog: Dialog, it: List<DictionaryEntry>?) {
        dialog.findViewById<TextView>(R.id.wordTextView).text=it!![0].word
        dialog.findViewById<TextView>(R.id.phoneticTextView).text=it[0].phonetic
        var phoneticAudioLink=""
        for(ph in it[0].phonetics)
            if(ph.audio!=null)
            {
                phoneticAudioLink=ph.audio
                break
            }
        if(phoneticAudioLink=="")
            dialog.findViewById<ImageButton>(R.id.playButton).visibility=View.INVISIBLE
        else
            dialog.findViewById<ImageButton>(R.id.playButton).setOnClickListener {
                playAudio(phoneticAudioLink)
        }

        for(i in 0 until  it[0].meanings.size)
        {
            dialog.findViewById<TextView>(R.id.definitionTextView).append("${i+1}. "+it[0].meanings[i].definitions[0].definition+"\n")
            if(it[0].meanings[i].definitions[0].example!=null)
                dialog.findViewById<TextView>(R.id.definitionTextView).append("Example: "+it[0].meanings[i].definitions[0].example+"\n")
        }


    }

    private fun computedColor(value: Int): Int {
        var colorId:Int=Color.WHITE
        when(value)
        {
            2->colorId=Color.GREEN
            1->colorId=Color.YELLOW
            -1->colorId=Color.GRAY
        }
        return  colorId

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if(intent.getAction() != null)
        {
            var word= intent.data?.getQueryParameter("word")
            binding.gameTypetextView.append("\n(Challenge mode)")

            val cryptObj=cryptography()
            word=cryptObj.Decrypt(word!!)

            wordSize=word!!.length/////
            Log.d(word!!,word.length.toString())

            gameViewModel=ViewModelProvider(this,GameViewModelFactory(1, guessSize, wordSize,word.uppercase(),this)).get(GameViewModel::class.java)
        }
        else
        {
            wordSize=intent.getIntExtra("WordSize",0)
            gameViewModel=ViewModelProvider(this,GameViewModelFactory(1,guessSize, wordSize,"",this)).get(GameViewModel::class.java)
        }
    }

    fun onClickButton(view: View)
    {
        gameViewModel.letterClick(findViewById<Button>(view.id).text.toString()[0])
    }
    private fun getTextBox(rowIndex: Int, wordIndex: Int): TextView {
        return binding.OneGameLayout.getChildAt(rowIndex).findViewById<LinearLayout>(R.id.onewordrow_view_id)
            .getChildAt(wordIndex).findViewById<TextView>(R.id.classictextbox_view_id)
    }

    private fun playAudio(audioUrl: String) {

        try {
            var mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())//before async to work

            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
            }

            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        Log.d("ondestroy","call")
        super.onDestroy()
        gameEndDialog.dismiss()
        gameHelpDialog.dismiss()

    }

//************voiceHandle******************88
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== RecordAudioRequestCode && grantResults.isNotEmpty())
        {
            Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkPermissions(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RecordAudioRequestCode)
        }
    }

    companion object
    {
        const val RecordAudioRequestCode=1
    }
}