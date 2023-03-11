package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.Message
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ActivityChatRoomBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
class ChatRoomActivity : AppCompatActivity() {

    lateinit var binding : ActivityChatRoomBinding
    lateinit var btn_exit : ImageButton
    lateinit var btn_submit : Button
    lateinit var txt_title : TextView
    lateinit var edit_message : EditText
    lateinit var firebaseDatabase : DatabaseReference
    lateinit var recycler_talks : RecyclerView
    lateinit var chatRoom : ChatRoom
    lateinit var opponentUser : User
    lateinit var chatRoomKey : String
    lateinit var myUid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeProperty()
        initializeView()
        initializeListener()
        setupChatRooms()
    }

    fun initializeProperty(){
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!
        firebaseDatabase = FirebaseDatabase.getInstance().reference!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chatRoom = intent.getSerializableExtra("ChatRoom", ChatRoom::class.java)!!
            opponentUser = intent.getSerializableExtra("Opponent", User::class.java)!!
        }else{
            chatRoom = (intent.getSerializableExtra("ChatRoom")) as ChatRoom
            opponentUser = (intent.getSerializableExtra("Opponent")) as User
        }
        chatRoomKey = intent.getStringExtra("ChatRoomKey")!!
    }

    fun initializeView(){
        btn_exit = binding.imgbtnQuit
        edit_message = binding.edtMessage
        recycler_talks = binding.recyclerMessages
        btn_submit = binding.btnSubmit
        txt_title = binding.txtTitle
        txt_title.text = opponentUser!!.name ?: ""
    }

    fun initializeListener(){
        btn_exit.setOnClickListener(){
            startActivity(Intent(this@ChatRoomActivity, MainActivity::class.java))
        }
        btn_submit.setOnClickListener(){
            putMessage()
        }
    }

    fun setupChatRooms(){
        if(chatRoomKey.isNullOrBlank())
            setupChatRoomKey()
        else
            setupRecycler()
    }

    fun saveIntoDB(message: Message){
        FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
            .child(chatRoomKey).child("messages")
            .push().setValue(message).addOnSuccessListener {
                Log.i("putMessage", "성공")
                edit_message.text.clear()
            }.addOnCanceledListener {
                Log.i("putMessage", "실패")
            }
    }

    fun putMessage(){
        try{

            //날짜 구분
            var curDate = getDateTimeString()
            var latest  = (recycler_talks.adapter as RecyclerMessageAdapter).getLatestMessage()
            var dateAdd = false

            if(latest==null || curDate.substring(6,2) != latest.sent_date.substring(6,2)){
                dateAdd = true
            }

            var dateMessage:Message

            if(dateAdd){
                dateMessage = Message("0000", curDate, "", isDate = true)
                saveIntoDB(dateMessage)
            }

            var message = Message(myUid, curDate, edit_message.text.toString())
            Log.i("ChatRoomKey", chatRoomKey)
            saveIntoDB(message)

        }catch (e: Exception){
            e.printStackTrace()
            Log.i("putMessage", "오류")
        }
    }

    fun setupChatRoomKey(){
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms").orderByChild("users/${opponentUser.uid}").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(data in snapshot.children){
                        chatRoomKey = data.key!!
                        setupRecycler()
                        break
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("setupKey", "에러")
                }

            })
    }

    fun setupRecycler(){
        recycler_talks.layoutManager = LinearLayoutManager(this)
        recycler_talks.adapter = RecyclerMessageAdapter(this, chatRoomKey, opponentUser.uid)
    }

    fun getDateTimeString() : String{
        try {
            var localDateTime = LocalDateTime.now()
            localDateTime.atZone(TimeZone.getDefault().toZoneId())
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return localDateTime.format(dateTimeFormatter).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("getTimeError")
        }
    }

}