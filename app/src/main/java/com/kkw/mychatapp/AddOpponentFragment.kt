package com.kkw.mychatapp

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FieldValue
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.FragmentAddOpponentBinding

@RequiresApi(Build.VERSION_CODES.O)
class AddOpponentFragment(val chatRoomKey:String, val curOpponents: ArrayList<User>): Fragment() {

    private var _binding: FragmentAddOpponentBinding? = null
    private val binding get() = _binding!!

    lateinit var btnExit : ImageButton
    lateinit var editOpponent : EditText
    private lateinit var confirmBtn : Button
    lateinit var firebaseDatabase : DatabaseReference

    lateinit var selectedUsers : RecyclerView
    lateinit var usersList : RecyclerView

    var addedOpponenet = arrayListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddOpponentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeListener()
        setUpRecycler()
    }


    private fun initializeView(){
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        selectedUsers=binding.recyclerSelected
        usersList=binding.recyclerPeopleToSelect
        btnExit = binding.imgBtnBack
        editOpponent = binding.editOpponentName
        confirmBtn = binding.confirm
    }


    fun fragmentFinish(){
        var fragmentManager = activity?.supportFragmentManager
        fragmentManager?.beginTransaction()?.remove(this@AddOpponentFragment)?.commit()
        fragmentManager?.popBackStack()
    }

    private fun initializeListener(){
        btnExit.setOnClickListener {
            fragmentFinish()
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

        confirmBtn.setOnClickListener{
            addOpponent()
        }
    }

    private fun addOpponent(){
        var doc =  FirebasePath.chatRoomPath
            .document("$chatRoomKey")

        addedOpponenet.forEach {
           doc.get()
                .addOnSuccessListener {
                    snapshot->
                    if((snapshot.data!!["users"] as HashMap<String, Boolean>)[it.uid] == null)
                        doc.update(mapOf("users.${it.uid}" to true))
                        doc.update("currentUsers", FieldValue.arrayUnion(it.uid))
                }
        }

        (activity as ChatRoomActivity).updateOpponents(addedOpponenet)
        fragmentFinish()
    }


    fun setUpRecycler(){
        usersList.layoutManager = LinearLayoutManager(context)
        usersList.adapter = context?.let { RecyclerUserAdapter(it, roomKey = chatRoomKey) }

        var uAdapter = usersList.adapter as RecyclerUserAdapter

        uAdapter.setOnItemClickListener(object : IItemClickListener{
            override fun onItemClick(holder: RecyclerView.ViewHolder, v: View, position: Int) {
                var userHolder = holder as RecyclerUserAdapter.ToSelectViewHolder
                var checked = userHolder.checked
                if(!checked)
                {
                    addedOpponenet.add(uAdapter.users[position])
                }else{
                    var uid = uAdapter.users[position].uid
                    addedOpponenet.removeIf{it.uid==uid}
                }
                userHolder.checked = !checked
                userHolder.checkIcon.isSelected = !checked


            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}