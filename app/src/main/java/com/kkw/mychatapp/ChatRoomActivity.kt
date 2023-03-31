package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.core.View
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.Message
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ActivityChatRoomBinding
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
class ChatRoomActivity : AppCompatActivity() {

    private var _binding : ActivityChatRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var btn_exit : ImageButton
    private lateinit var btn_submit : Button
    private lateinit var addBtn : Button
    private lateinit var txt_title : TextView
    private lateinit var edit_message : EditText
    lateinit var firebaseDatabase : DatabaseReference
    lateinit var recycler_talks : RecyclerView
    lateinit var chatRoom : ChatRoom
    var opponentUser = arrayListOf<User>()
    lateinit var chatRoomKey : String
    lateinit var myUid : String
    lateinit var roomTitle : String
    lateinit var container : FrameLayout
    lateinit var messageAdapter: RecyclerMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeProperty()
        initializeView()
        initializeListener()
        setupRecycler()
        //setupChatRooms()
    }


    private fun initializeProperty(){
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!
        firebaseDatabase = FirebaseDatabase.getInstance().reference!!

        chatRoom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("ChatRoom", ChatRoom::class.java)!!


        }else{
            (intent.getSerializableExtra("ChatRoom")) as ChatRoom
        }

        opponentUser = intent.getSerializableExtra("Opponent") as ArrayList<User>
       //opponentUser = ArrayList(intent.getSerializableExtra("Opponent", List<User>::class.java)!!)
        chatRoomKey = intent.getStringExtra("ChatRoomKey")!!
        roomTitle = intent.getStringExtra("Name")!!
    }

    private fun initializeView(){
        btn_exit = binding.imgbtnQuit
        edit_message = binding.edtMessage
        recycler_talks = binding.recyclerMessages
        btn_submit = binding.btnSubmit
        addBtn = binding.addOpponentBtn
        txt_title = binding.txtTitle
        txt_title.text = roomTitle
        container = binding.container
    }


    override fun onDestroy() {
        super.onDestroy()
        messageAdapter.removeRegistration()
        _binding = null
    }

    private fun initializeListener(){
        btn_exit.setOnClickListener{
            finish()
        }
        btn_submit.setOnClickListener{
            putMessage()
        }
        addBtn.setOnClickListener{
            val transaction = supportFragmentManager.beginTransaction()
                .add(R.id.container,AddOpponentFragment(chatRoomKey, opponentUser))
            transaction.commit()
        }
    }

    fun updateOpponents(addedUser:ArrayList<User>) {
        opponentUser.addAll(addedUser)
        addedUser.forEach{
            roomTitle += (it.name + ", ")
        }
        txt_title.text = roomTitle.substring(0, roomTitle.length-3)
    }


    private fun saveIntoDB(message: Message){
//        FirebasePath.chatRoom
//            .child(chatRoomKey).child("messages")
//            .push().setValue(message).addOnSuccessListener {
//                Log.i("putMessage", "성공")
//                edit_message.text.clear()
//            }.addOnCanceledListener {
//                Log.i("putMessage", "실패")
//            }

        FirebasePath.chatRoomPath
            .document(chatRoomKey)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Log.i("putMessage", "성공")
                edit_message.text.clear()
            }.addOnCanceledListener {
                Log.i("putMessage", "실패")
            }

//        FirebasePath.chatRoomPath
//            .document(chatRoomKey)
//            .update(mapOf("messages.$messageId" to message))
//            .addOnSuccessListener {
//                Log.i("putMessage", "성공")
//                edit_message.text.clear()
//            }.addOnCanceledListener {
//                Log.i("putMessage", "실패")
//            }

    }

    private fun putMessage(){
        try{

            //날짜 구분
            var curDate = getDateTimeString()

            var latest  = (recycler_talks.adapter as RecyclerMessageAdapter).getLatestMessage()
            var dateAdd = false

            if(latest==null || curDate.substring(0,4) != latest.sent_date.substring(0,4) || curDate.substring(6,8) != latest.sent_date.substring(6,8)){
                dateAdd = true
            }

            var dateMessage:Message

            if(dateAdd){
                dateMessage = Message("0000", curDate, "", date = true)
                saveIntoDB(dateMessage)
            }

            var oppMap : HashMap<String, Boolean>  = hashMapOf()
            opponentUser.forEach { oppMap[it.uid!!] = true }

            var message = Message(senderUid = myUid, sent_date = curDate, content = edit_message.text.toString(), unconfirmedOpponent = oppMap)
            saveIntoDB(message)

        }catch (e: Exception){
            e.printStackTrace()
            Log.i("putMessage", "오류")
        }
    }

    fun setupRecycler(){

        recycler_talks.layoutManager = LinearLayoutManager(this)
        messageAdapter = RecyclerMessageAdapter(this, chatRoomKey, opponentUser)
        recycler_talks.adapter = messageAdapter
    }

    private fun getDateTimeString() : String{
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






