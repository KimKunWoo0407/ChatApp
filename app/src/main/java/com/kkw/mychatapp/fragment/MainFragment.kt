package com.kkw.mychatapp.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kkw.mychatapp.MainActivity
import com.kkw.mychatapp.adapter.RecyclerChatRoomsAdapter
import com.kkw.mychatapp.databinding.FragmentMainBinding

@RequiresApi(Build.VERSION_CODES.O)
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    lateinit var btnAddChatRoom: Button
    lateinit var btnSignOut: Button
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recyclerChatroom: RecyclerView
    lateinit var chatRoomsAdapter: RecyclerChatRoomsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeListener()
        setupRecycler()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun initializeView(){
        try{
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("ChatRoom")!!
            btnSignOut = binding.btnSignout
            btnAddChatRoom = binding.btnNewMessage
            recyclerChatroom = binding.recyclerChatrooms

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun initializeListener(){
        btnSignOut.setOnClickListener{
            signOut();
        }
        btnAddChatRoom.setOnClickListener{
            parentFragmentManager
                .beginTransaction()
                .replace(mainActivity.container.id, AddChatRoomFragment())
                .addToBackStack(null)
                .commit()
        }
    }


    private fun signOut(){
        try{
            val builder = AlertDialog.Builder(mainActivity)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인"){
                        dialog, id->
                    try{
                        FirebaseAuth.getInstance().signOut()
                        dialog.dismiss()
                        parentFragmentManager.beginTransaction()
                            .replace(mainActivity.container.id, LoginFragment())
                            .commit()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecycler(){
        recyclerChatroom.layoutManager = LinearLayoutManager(mainActivity)
        chatRoomsAdapter = RecyclerChatRoomsAdapter(mainActivity)
        recyclerChatroom.adapter = chatRoomsAdapter
    }

}