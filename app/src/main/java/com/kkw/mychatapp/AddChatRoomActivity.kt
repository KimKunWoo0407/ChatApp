package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kkw.mychatapp.databinding.ActivityAddChatRoomBinding

@RequiresApi(Build.VERSION_CODES.O)
class AddChatRoomActivity : AppCompatActivity() {

    var _binding : ActivityAddChatRoomBinding? = null
    val binding get() = _binding!!
    lateinit var btn_exit : ImageButton
    lateinit var edit_opponent : EditText
    lateinit var firebaseDatabase : DatabaseReference
    lateinit var recycler_people : RecyclerView
    lateinit var recyclerChatRoom: RecyclerView
    lateinit var chatRoomsAdapter: RecyclerChatRoomsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        initializeListener()
        setupRecycler()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatRoomsAdapter.removeRegistration()
        _binding = null
    }

    fun initializeView(){
        //firebaseDatabase = FirebaseDatabase.getInstance().reference!!
        btn_exit = binding.imgBtnBack
        edit_opponent = binding.editOpponentName
        recycler_people = binding.recyclerPeople
        recyclerChatRoom = binding.recyclerRooms
    }

    fun initializeListener(){
        btn_exit.setOnClickListener(){
            finish()
        }

        edit_opponent.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }


            override fun afterTextChanged(s: Editable?) {
                var adapter = recycler_people?.adapter as RecyclerUserAdapter

                //adapter.searchItem(s.toString())

//                var roomAdapter = recyclerChatRoom?.adapter as RecyclerChatRoomsAdapter
//                roomAdapter.searchItem(adapter.searchItem(s.toString()))
                chatRoomsAdapter.searchItem(adapter.searchItem(s.toString()))

            }

        })
    }



    fun setupRecycler(){
        recycler_people.layoutManager = LinearLayoutManager(this)
        recycler_people.adapter = RecyclerUserAdapter(this)

        recyclerChatRoom.layoutManager = LinearLayoutManager(this)
        chatRoomsAdapter = RecyclerChatRoomsAdapter(this, false)
        recyclerChatRoom.adapter = chatRoomsAdapter

    }
}