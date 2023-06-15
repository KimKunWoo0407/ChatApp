package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kkw.mychatapp.databinding.ActivityMainBinding

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {

    lateinit var btnAddchatRoom: Button
    lateinit var btnSignout: Button
    var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_chatroom: RecyclerView
    lateinit var chatRoomsAdapter: RecyclerChatRoomsAdapter

    private val callback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            signOut()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        initializeListener()
        setupRecycler()

        this.onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onDestroy() {
        super.onDestroy()
        chatRoomsAdapter.removeRegistration()
        _binding = null
    }

    fun initializeView(){
        try{
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("ChatRoom")!!
            btnSignout = binding.btnSignout
            btnAddchatRoom = binding.btnNewMessage
            recycler_chatroom = binding.recyclerChatrooms

        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    fun initializeListener(){
        btnSignout.setOnClickListener(){
            signOut();
        }
        btnAddchatRoom.setOnClickListener(){
            startActivity(Intent(this@MainActivity, AddChatRoomActivity::class.java))
            //finish()
        }
    }

    fun signOut(){
        try{
            val builder = AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인"){
                    dialog, id->
                    try{
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        dialog.dismiss()
                        finish()
                    }catch(e:Exception){
                        e.printStackTrace()
                        dialog.dismiss()
                    }
                }.setNegativeButton("취소"){
                    dialog, id->
                    dialog.dismiss()

                }

            builder.show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun setupRecycler(){
        recycler_chatroom.layoutManager = LinearLayoutManager(this)
        chatRoomsAdapter = RecyclerChatRoomsAdapter(this)
        recycler_chatroom.adapter = chatRoomsAdapter
    }

//    override fun onBackPressed() {
//        signOut()
//    }

}