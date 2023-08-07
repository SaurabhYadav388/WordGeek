package com.example.wordgeek.ui

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable.ClassLoaderCreator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.example.wordgeek.R
import com.example.wordgeek.databinding.ActivityTwoPlayerDemoBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class TwoPlayerDemo : AppCompatActivity() {
    private lateinit var binding: ActivityTwoPlayerDemoBinding
    private var guessSize=6
    private var wordSize=5

    private lateinit var roomRef: DatabaseReference
    private lateinit var roomId: String
    private lateinit var word:String
    private lateinit var userGuessEditText: EditText
    private lateinit var opponentGuessTextView: TextView
    private var creatorGuy:Boolean = false
    private var startGame: MutableLiveData<Boolean> = MutableLiveData(false)
    private lateinit var gameStartListener:ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTwoPlayerDemoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        userGuessEditText = findViewById(R.id.editYourGuess)
        opponentGuessTextView = findViewById(R.id.editOpponentGuess)

        roomId = "8765"
        word = "green"
        roomRef=FirebaseDatabase.getInstance().getReference("rooms")

        val multiPlayerEntryDialog=Dialog(this)
        multiPlayerEntryDialog.setContentView(R.layout.dialog_multiplayer_entry)
        multiPlayerEntryDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        multiPlayerEntryDialog.setCanceledOnTouchOutside(false)
        multiPlayerEntryDialog.show()
        multiPlayerEntryDialog.findViewById<Button>(R.id.createGameButton).setOnClickListener {
            multiPlayerEntryDialog.dismiss()
            Toast.makeText(this,"creator",Toast.LENGTH_SHORT).show()
            creatorGuy=true
            createRoom()
        }

        multiPlayerEntryDialog.findViewById<Button>(R.id.joinGameButton).setOnClickListener {
            creatorGuy=false
            joinRoom(roomId)
        }

        startGame.observe(this){
            if(it==true)
            {
                Toast.makeText(this,"GAME STARTED",Toast.LENGTH_LONG).show()
                multiPlayerEntryDialog.dismiss()
                ///////////////////////game logic
                findViewById<Button>(R.id.buttonSubmitGuess).setOnClickListener {
                    submitGuess()
                }
                if(creatorGuy==true)
                    listenToOpponentGuess("joinerPlayer")
                else
                    listenToOpponentGuess("creatorPlayer")
            }
        }
    }


    private fun createRoom() {

        //Toast.makeText(this,"creatRoom",Toast.LENGTH_SHORT).show()
        //val roomData = RoomDetail(wordToGuess = word,gameStarted = false, creatorPlayer = PlayerDetail(true,""), joinerPlayer = PlayerDetail(false,""))
        //roomRef.child(roomId).setValue(roomData)
        //startListeningForJoiningPlayer()
    }
    private fun startListeningForJoiningPlayer() {
        gameStartListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val room = dataSnapshot.getValue(RoomDetail::class.java)
                if (room != null && room.gameStarted) {
                    // Game has started, perform relevant actions
                    notifyGameStart()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        }
        roomRef.child(roomId).addValueEventListener(gameStartListener)
    }
    private fun joinRoom(roomId: String) {
        val roomQuery: Query = roomRef.child(roomId)
        roomQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Room with the provided ID exists, join the room
                    val roomData = dataSnapshot.getValue(RoomDetail::class.java)
                    if (roomData != null && !roomData.gameStarted) {
                        // Room exists and the game has not started
                        // Perform the join operation and notify the creator
                        ///valu..............................
                        notifyGameStart()
                    }
                    else  {
                        // Room exists but the game has already started
                        // Handle the scenario when a joiner tries to join an ongoing game
                        // For example, show an error message to the joiner
                    }
                }
                else {
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
    private fun notifyGameStart() {
        roomRef.child(roomId).child("gameStarted").setValue(true)
        startGame.value=true
    }

    private fun submitGuess() {
        val userGuess = userGuessEditText.text.toString()
        userGuessEditText.text.clear()

        if(creatorGuy==true)
        {
            roomRef.child(roomId).child("creatorPlayer").child("guess").setValue(userGuess)
        }
        else
        {
            roomRef.child(roomId).child("joinerPlayer").child("guess").setValue(userGuess)
        }

    }
    private fun listenToOpponentGuess(opponentString: String) {
        roomRef.child(roomId).child(opponentString).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val playerDetail = dataSnapshot.getValue(PlayerDetail::class.java)
                if (playerDetail != null) {
                    // Update opponent's guess TextView with room.guess
                    if(creatorGuy==true);
                        //opponentGuessTextView.text = playerDetail.guess
                    else;
                        //opponentGuessTextView.text = playerDetail.guess
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("error","database")
                // Handle errors
            }
        })
    }

/*



    private fun checkWinCondition(userGuess: String) {
        roomRef.child(roomId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val room = dataSnapshot.getValue(Room::class.java)
                if (room != null) {
                    //if (!room.creator && userGuess == room.word) {
                        // User wins, perform relevant actions
                   // }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    // Helper function to generate a random word
    private fun generateRandomWord(): String {
        return ""
    }

 */

}