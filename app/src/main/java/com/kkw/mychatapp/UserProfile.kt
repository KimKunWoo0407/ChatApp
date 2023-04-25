package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.toObject
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ActivityUserProfileBinding

@RequiresApi(Build.VERSION_CODES.O)
class UserProfile : AppCompatActivity() {

    lateinit var binding: ActivityUserProfileBinding

    lateinit var txtName : TextView
    lateinit var emailTxt : TextView
    lateinit var uid : String
    lateinit var myUid : String
    lateinit var chatStartBtn : Button
    lateinit var opponent : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        initializeProperty()
    }

    private fun initializeProperty(){
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!
        uid = intent.getStringExtra("uid")!!
        FirebasePath.userPath.document(uid)
            .get()
            .addOnSuccessListener {
                opponent = it.toObject<User>()!!
                txtName.text = opponent.name
                emailTxt.text = opponent.email
            }
    }

    private fun initializeView(){
        txtName = binding.profileNameTxt
        emailTxt = binding.profileEmailTxt
        chatStartBtn = binding.oneOnOneButton
        chatStartBtn.setOnClickListener{
            addChatRoom()
        }
    }


    private fun addChatRoom(){ //1대1 채팅방 만들기
        val opponents = arrayListOf<User>()
        opponents.add(opponent)
        var curUsers = arrayListOf(myUid!!, opponent.uid!!)
        var chatRoom = ChatRoom(
            users = mapOf(myUid to true, opponent.uid!! to true),
            currentUsers = curUsers
        )

        FirebasePath.chatRoomPath
            .whereEqualTo("singleRoom", true)
            .whereEqualTo("users.${opponent.uid!!}", true)
            .get()
            .addOnSuccessListener { ref->
                if(!ref.isEmpty){
                    goToChatRoom(chatRoom, opponents, ref.first().id)
                }else{
                    goToChatRoom(chatRoom, opponents)
                }
            } .addOnFailureListener { e ->
                Log.w("UAdapter", "Error adding document", e)
            }

    }


    fun goToChatRoom(chatRoom: ChatRoom, opponent: ArrayList<User>, roomKey:String=""){
        var intent = Intent(this@UserProfile, ChatRoomActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)
        intent.putExtra("Opponent", opponent)
        intent.putExtra("ChatRoomKey", roomKey)
        intent.putExtra("Name", opponent[0].name)
        startActivity(intent)
    }

}