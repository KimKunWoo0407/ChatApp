package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kkw.mychatapp.databinding.FragmentAddOpponentBinding

@RequiresApi(Build.VERSION_CODES.O)
class AddOpponentFragment : Fragment() {

    private var _binding: FragmentAddOpponentBinding? = null
    private val binding get() = _binding!!

    lateinit var btnExit : ImageButton
    lateinit var editOpponent : EditText
    private lateinit var confirmBtn : Button
    lateinit var firebaseDatabase : DatabaseReference

    lateinit var selectedUsers : RecyclerView
    lateinit var usersList : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddOpponentBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    private fun initializeView(){
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        selectedUsers=binding.recyclerSelected
        usersList=binding.recyclerPeopleToSelect
        btnExit = binding.imgBtnBack
        editOpponent = binding.editOpponentName
        confirmBtn = binding.confirm
    }


    private fun initializeListener(){
        btnExit.setOnClickListener(){
            var fragmentManager = activity?.supportFragmentManager
            fragmentManager?.beginTransaction()?.remove(this@AddOpponentFragment)?.commit()
            fragmentManager?.popBackStack()
        }

        editOpponent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }


            override fun afterTextChanged(s: Editable?) {
//                var adapter = recycler_people?.adapter as RecyclerUserAdapter
//
//                var roomAdapter = recyclerChatRoom?.adapter as RecyclerChatRoomsAdapter
//                roomAdapter.searchItem(adapter.searchItem(s.toString()))

            }

        })
    }

    fun setUpRecycler(){
        usersList.layoutManager = LinearLayoutManager(context)
        usersList.adapter = context?.let { RecyclerUserAdapter(it, isInRoom = true) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeListener()
        setUpRecycler()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}