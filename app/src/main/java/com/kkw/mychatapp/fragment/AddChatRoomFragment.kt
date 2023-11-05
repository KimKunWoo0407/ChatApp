package com.kkw.mychatapp.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kkw.mychatapp.MainActivity
import com.kkw.mychatapp.adapter.RecyclerChatRoomsAdapter
import com.kkw.mychatapp.adapter.RecyclerUserAdapter
import com.kkw.mychatapp.databinding.FragmentAddChatRoomBinding

@RequiresApi(Build.VERSION_CODES.O)
class AddChatRoomFragment : Fragment() {

    private var _binding : FragmentAddChatRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    lateinit var btnExit : ImageButton
    lateinit var editOpponent : EditText
    lateinit var recyclerPeople : RecyclerView
    lateinit var recyclerChatRoom: RecyclerView
    lateinit var chatRoomsAdapter: RecyclerChatRoomsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChatRoomBinding.inflate(layoutInflater, container, false)
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
        chatRoomsAdapter.removeRegistration()
        super.onDestroy()
    }

    private fun initializeView(){
        btnExit = binding.imgBtnBack
        editOpponent = binding.editOpponentName
        recyclerPeople = binding.recyclerPeople
        recyclerChatRoom = binding.recyclerRooms
    }

    private fun initializeListener(){
        btnExit.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        editOpponent.addTextChangedListener {
            val adapter = recyclerPeople.adapter as RecyclerUserAdapter
            chatRoomsAdapter.searchItem(adapter.searchItem(it.toString()))
        }
    }

    private fun setupRecycler(){
        recyclerPeople.layoutManager = LinearLayoutManager(mainActivity)
        recyclerPeople.adapter = RecyclerUserAdapter(mainActivity)

        recyclerChatRoom.layoutManager = LinearLayoutManager(mainActivity)
        chatRoomsAdapter = RecyclerChatRoomsAdapter(mainActivity, false)
        recyclerChatRoom.adapter = chatRoomsAdapter

    }


}