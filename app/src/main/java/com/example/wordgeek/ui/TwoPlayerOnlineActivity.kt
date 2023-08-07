package com.example.wordgeek.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.wordgeek.R
import com.example.wordgeek.databinding.ActivityTwoPlayerOnlineBinding
import com.example.wordgeek.model.GameRepository
import com.example.wordgeek.model.WordDataBase
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlin.random.Random

data class PlayerDetail(
    val creator: Boolean=false,
    var guess:MutableList<String> = MutableList(guessSize){""},
    var guessColor:MutableList<MutableList<Int>> = MutableList(guessSize){ MutableList(wordSize){0} },
    var guessIndex: Int =0
)
data class RoomDetail(
    val wordToGuess: String = "",
    val gameStarted: Boolean =false,
    val gameEnded:Int=-1,
    val creatorPlayer: PlayerDetail = PlayerDetail(true,MutableList(guessSize){""}, MutableList(guessSize){ MutableList(wordSize){0} },0),
    val joinerPlayer:PlayerDetail=PlayerDetail(false, MutableList(guessSize){""}, MutableList(guessSize){ MutableList(wordSize){0} },0)
)

private lateinit var binding:ActivityTwoPlayerOnlineBinding
private var guessSize=6
private var wordSize=5
private var guesssIndex=0
private var wordIndex=0

private lateinit var userGuesses:MutableList<String>
private lateinit var userGuessesColors:MutableList<MutableList<Int>>

private lateinit var roomRef: DatabaseReference
private lateinit var roomId: String
private  var wordtoguess:String=""

private var creatorGuy:Boolean = false
private var startGame: MutableLiveData<Boolean> = MutableLiveData(false)
private lateinit var gameStartListener: ValueEventListener

class TwoPlayerOnlineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTwoPlayerOnlineBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)


        roomId = "8765"
        userGuesses=MutableList(guessSize){""}
        userGuessesColors= MutableList(guessSize){ MutableList(wordSize){0} }

        roomRef= FirebaseDatabase.getInstance().getReference("rooms")

        for (i in 0 until guessSize)
        {
            layoutInflater.inflate(R.layout.onewordrow_view, binding.opponentLayout)
        }
        for (i in 0 until guessSize)
        {
            for (j in 0 until wordSize)
            {
                val viewGroup: ViewGroup =binding.opponentLayout.getChildAt(i).findViewById(R.id.onewordrow_view_id)
                layoutInflater.inflate(R.layout.textbox_view,viewGroup)

                //as all textbox using one textbox ; replacing background ,next game uses last assigned color for all textbox
                // /can be optimized
                val gd= binding.opponentLayout.getChildAt(i).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                    .getChildAt(j).findViewById<TextView>(R.id.textbox_view_id).background
                if(gd is GradientDrawable)
                    gd.setColor(Color.WHITE)
            }
        }

        for (i in 0 until guessSize)
        {
            layoutInflater.inflate(R.layout.onewordrow_view, binding.ourLayout)
        }
        for (i in 0 until guessSize)
        {
            for (j in 0 until wordSize)
            {
                val viewGroup: ViewGroup =binding.ourLayout.getChildAt(i).findViewById(R.id.onewordrow_view_id)
                layoutInflater.inflate(R.layout.textbox_view,viewGroup)
            }
        }

        val multiPlayerEntryDialog= Dialog(this)
        multiPlayerEntryDialog.setContentView(R.layout.dialog_multiplayer_entry)
        multiPlayerEntryDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        multiPlayerEntryDialog.setCanceledOnTouchOutside(false)
        multiPlayerEntryDialog.show()
        multiPlayerEntryDialog.findViewById<Button>(R.id.createGameButton).setOnClickListener {
            multiPlayerEntryDialog.dismiss()
            Toast.makeText(this,"creator", Toast.LENGTH_SHORT).show()
            creatorGuy=true
            createRoom()
        }

        multiPlayerEntryDialog.findViewById<Button>(R.id.joinGameButton).setOnClickListener {
            creatorGuy=false
            joinRoom()
        }

        startGame.observe(this){
            if(it==true)
            {
                Log.e("observe","startgame")
                Toast.makeText(this,"GAME STARTED", Toast.LENGTH_LONG).show()
                multiPlayerEntryDialog.dismiss()
                ///////////////////////game logic

                /*if(creatorGuy)
                    roomRef.child(roomId).child("gameEnded").onDisconnect().setValue(0)//opposite
                else
                    roomRef.child(roomId).child("gameEnded").onDisconnect().setValue(1)//opposite
*/
                startListeningForgameEnd()

                binding.DEL.setOnClickListener {
                    onClickDelete()
                }

                binding.submitBut.setOnClickListener {
                    submitGuess()
                }
                if(creatorGuy==true)
                    listenToOpponentGuess("joinerPlayer")
                else
                    listenToOpponentGuess("creatorPlayer")

            }
        }

    }

    /*override fun onBackPressed() {
        if (false) {
            //super.onBackPressed()
        } else {

        }
    }
*/
    private fun createRoom() {
        val multiPlayerCreatorDialog= Dialog(this)
        multiPlayerCreatorDialog.setContentView(R.layout.dialog_multiplayer_enter_roomid)
        multiPlayerCreatorDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        multiPlayerCreatorDialog.setCanceledOnTouchOutside(false)
        multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).hint="Create new Room ID"
        multiPlayerCreatorDialog.show()

        multiPlayerCreatorDialog.findViewById<Button>(R.id.btnPlayCreator).setOnClickListener {
            val etroomId = multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).text.toString().trim()
            if(etroomId.isEmpty())
                Toast.makeText(this, "Please enter a valid Room ID", Toast.LENGTH_SHORT).show()
            else
            {
                roomId=etroomId///
                wordtoguess = generateRandomWord()//only creator create the word
                val roomData = RoomDetail(wordToGuess = wordtoguess,gameStarted = false,gameEnded=-1,
                    creatorPlayer = PlayerDetail(true,MutableList<String>(guessSize){""}, MutableList(guessSize){ MutableList(wordSize){0} },0),
                    joinerPlayer = PlayerDetail(false,MutableList<String>(guessSize){""},MutableList(guessSize){ MutableList(wordSize){0} },0)
                )
                roomRef.child(roomId).setValue(roomData)//

                multiPlayerCreatorDialog.findViewById<Button>(R.id.btnPlayCreator).visibility=View.GONE
                multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).setText("Waiting For Joiner...")
                multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).isFocusable=false;
                multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).isEnabled=false;
                multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).isCursorVisible=false;
                multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).keyListener=null
                //editText.setKeyListener(null);
                multiPlayerCreatorDialog.findViewById<ProgressBar>(R.id.loadingIndicator).visibility=View.VISIBLE
                multiPlayerCreatorDialog.findViewById<EditText>(R.id.etRoomId).hint="Enter"
                //startListeningForJoiningPlayer
                gameStartListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val gameStarted = dataSnapshot.getValue<Boolean>()//getValue depend on type of object listening
                        if ((gameStarted != null) && gameStarted==true) {
                            // Game has started, perform relevant actions
                            Log.e("triggered","joiningplayer")
                            multiPlayerCreatorDialog.dismiss()////////
                            startGame.value=true    //for creator code observer
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                }
                roomRef.child(roomId).child("gameStarted").addValueEventListener(gameStartListener)

            }
        }

    }

    private fun joinRoom() {

        val multiPlayerJoinerDialog= Dialog(this)
        multiPlayerJoinerDialog.setContentView(R.layout.dialog_multiplayer_enter_roomid)
        multiPlayerJoinerDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        multiPlayerJoinerDialog.setCanceledOnTouchOutside(false)
        multiPlayerJoinerDialog.findViewById<EditText>(R.id.etRoomId).hint="Enter valid Room ID"
        multiPlayerJoinerDialog.show()

        multiPlayerJoinerDialog.findViewById<Button>(R.id.btnPlayCreator).setOnClickListener {
            val etroomId =
                multiPlayerJoinerDialog.findViewById<EditText>(R.id.etRoomId).text.toString().trim()
            if (etroomId.isEmpty())
                Toast.makeText(this, "Please enter a valid Room ID", Toast.LENGTH_SHORT).show()
            else {
                roomId=etroomId///
                val roomQuery: Query = roomRef.child(roomId)
                roomQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.e("roomid exist","")
                            // Room with the provided ID exists, join the room
                            val roomData = dataSnapshot.getValue(RoomDetail::class.java)
                            wordtoguess=roomData!!.wordToGuess
                            multiPlayerJoinerDialog.dismiss()///////
                            notifyGameStart()//temp..put in if later/////
                            if (roomData != null && !roomData.gameStarted) {
                                // Room exists and the game has not started
                                // Perform the join operation and notify the creator
                                ///valu.............................
                            }
                            else  {
                                // Room exists but the game has already started
                                // Handle the scenario when a joiner tries to join an ongoing game
                                // For example, show an error message to the joiner
                            }
                        }
                        else {
                            Log.e("roomid not exist",etroomId)
                            Toast.makeText(applicationContext, "Please enter a valid Room ID", Toast.LENGTH_SHORT).show()
                            // Room with the provided ID does not exist
                            // Handle the scenario when an invalid ID is provided
                            // For example, show an error message to the joiner
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })

            }

        }
    }
    private fun notifyGameStart() {
        roomRef.child(roomId).child("gameStarted").setValue(true)
        startGame.value=true //for joiner code observer
    }

    private fun submitGuess() {
        if( wordIndex== wordSize && isValidWord(currentUserGuess())==true )
        {
            Log.d("submit ke andar","age :${isValidWord(currentUserGuess())}")/////////


            userGuesses[guesssIndex]=currentUserGuess()
            userGuessesColors[guesssIndex]=calculateColors()
            ourTextBoxColorUpdate()

            var latestguess=currentUserGuess()
            guesssIndex+=1
            wordIndex=0
            if(creatorGuy==true)
            {
                roomRef.child(roomId).child("creatorPlayer").child("guessIndex").setValue(guesssIndex)
                roomRef.child(roomId).child("creatorPlayer").child("guess").setValue(userGuesses)
                roomRef.child(roomId).child("creatorPlayer").child("guessColor").setValue(userGuessesColors)
                if(latestguess== wordtoguess)
                    roomRef.child(roomId).child("gameEnded").setValue(1)
                else if(guesssIndex== guessSize)
                    roomRef.child(roomId).child("gameEnded").setValue(0)//make opponent winner as used all guesses
            }
            else
            {
                roomRef.child(roomId).child("joinerPlayer").child("guessIndex").setValue(guesssIndex)
                roomRef.child(roomId).child("joinerPlayer").child("guess").setValue(userGuesses)
                roomRef.child(roomId).child("joinerPlayer").child("guessColor").setValue(userGuessesColors)
                if(latestguess== wordtoguess)
                    roomRef.child(roomId).child("gameEnded").setValue(0)
                else if(guesssIndex== guessSize)
                    roomRef.child(roomId).child("gameEnded").setValue(1)//make opponent winner as used all guesses
            }
            ///
            {

            }
        }


    }

    private fun ourTextBoxColorUpdate() {
        for(j in 0 until wordSize)
        {
            val gd=binding.ourLayout.getChildAt(guesssIndex).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                .getChildAt(j).findViewById<TextView>(R.id.textbox_view_id).background
            if(gd is GradientDrawable)
                gd.setColor(mapColor(calculateColors()[j]))
        }

    }

    private fun listenToOpponentGuess(opponentString: String) {
        roomRef.child(roomId).child(opponentString).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val oppPlayerDetail = dataSnapshot.getValue(PlayerDetail::class.java)
                if (oppPlayerDetail != null) {
                    // Update opponent's guess TextView with room.guess
                    if(creatorGuy==true)
                        opponentTextBoxUpdate(oppPlayerDetail.guess,oppPlayerDetail.guessColor,"joinerPlayer")
                    else
                        opponentTextBoxUpdate(oppPlayerDetail.guess,oppPlayerDetail.guessColor,"creatorPlayer")

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("error","database")
                // Handle errors
            }
        })
    }

    private fun opponentTextBoxUpdate(oppGuesses: MutableList<String>,oppGuessesColor: MutableList<MutableList<Int>>,whoPlayer:String) {
            var oppguesssIndex:Int
            roomRef.child(roomId).child(whoPlayer).child("guessIndex").get().addOnSuccessListener {
                oppguesssIndex= it.getValue<Int>()!!
                Log.d("oppguessindex","$oppguesssIndex")
                Log.i("firebase", "Got value ${it.value}")
                for(i in 0 until oppguesssIndex)
                {
                    Log.d("oppindex","$i")
                    if(oppGuesses[i]!="" && oppGuessesColor[i][0]!=0)
                        for(j in 0 until wordSize)
                        {
                            binding.opponentLayout.getChildAt(i).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                                .getChildAt(j).findViewById<TextView>(R.id.textbox_view_id).text=oppGuesses[i][j].toString()

                            val gd=binding.opponentLayout.getChildAt(i).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                                .getChildAt(j).findViewById<TextView>(R.id.textbox_view_id).background
                            if(gd is GradientDrawable)
                                gd.setColor(mapColor(oppGuessesColor[i][j]))
                        }

                }

            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }

        }

    private fun mapColor(value: Int): Int {

        if(value==2)return Color.GREEN
        else if(value==1)return Color.YELLOW
        else if(value==-1)return Color.GRAY

        return Color.WHITE
    }

    // Helper function to generate a random word
    private fun generateRandomWord(): String {
        val repoWordList=GameRepository().getWordList(wordSize,this)
        return repoWordList[ (repoWordList.indices).random(Random(System.currentTimeMillis())) ]
    }

    fun isValidWord(word: String): Boolean {
        Log.d("wordis",word)
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
                Log.d("wordMid",wTemp)/////////
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

    fun onClickButton(view: View)
    {
        if(wordIndex< wordSize)
        {
            binding.ourLayout.getChildAt(guesssIndex).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                .getChildAt(wordIndex).findViewById<TextView>(R.id.textbox_view_id).text=findViewById<Button>(view.id).text.toString()
            wordIndex+=1
        }
    }
    fun onClickDelete()
    {
        if(wordIndex!=0)
        {
            wordIndex-=1
            binding.ourLayout.getChildAt(guesssIndex).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                .getChildAt(wordIndex).findViewById<TextView>(R.id.textbox_view_id).text=""
        }
    }
    fun calculateColors(): MutableList<Int>
    {
        var notGreenLetter= ArrayList<Char>()
        var colorArray= MutableList(wordSize) { 0 }
        var userGuess=currentUserGuess()

        for(i in 0 until wordSize)
        {
            if(userGuess[i]== wordtoguess[i])
            {
                colorArray[i]=2
            }
            else
                notGreenLetter.add(wordtoguess[i])
        }
        for(i in 0 until wordSize)
        {
            if(colorArray[i]== 0 )
            {
                if(userGuess[i] in notGreenLetter)
                    colorArray[i]=1
                else
                    colorArray[i]=-1
            }
        }

        return colorArray
    }
    fun currentUserGuess():String
    {
        var userGuess=""
        for(i in 0 until wordSize)
            userGuess+=binding.ourLayout.getChildAt(guesssIndex).findViewById<LinearLayout>(R.id.onewordrow_view_id)
                .getChildAt(i).findViewById<TextView>(R.id.textbox_view_id).text

        return  userGuess

    }
    private fun startListeningForgameEnd() {
        val gameEndListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val gameEnded = dataSnapshot.getValue<Int>()//getValue depend on type of object listening
                if ((gameEnded != null) && gameEnded!=-1) {
                    // Game ended
                    //startGame.value=true
                    if( (creatorGuy==true && gameEnded==1) ||(creatorGuy==false && gameEnded==0)  )
                        gameEndDialogBox("You")
                    else
                        gameEndDialogBox("Opponent")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        }
        roomRef.child(roomId).child("gameEnded").addValueEventListener(gameEndListener)
    }

    private fun gameEndDialogBox(who: String) {
        val gameEndDialog= Dialog(this)
        gameEndDialog.setContentView(R.layout.dialog_multiplayer_game_end)
        gameEndDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        gameEndDialog.setCanceledOnTouchOutside(false)
        gameEndDialog.show()
        gameEndDialog.findViewById<TextView>(R.id.tvWinner).text="$who WON!!"
        gameEndDialog.findViewById<TextView>(R.id.tvWord).text="The word was $wordtoguess"
        gameEndDialog.findViewById<Button>(R.id.btnClose).setOnClickListener { gameEndDialog.dismiss() }

    }


    override fun onDestroy() {
        super.onDestroy()
        roomRef.child(roomId).child("gameStarted").setValue(false)
        roomRef.child(roomId).child("gameEnded").setValue(-1)
        finish()
    }
}