package com.kkw.mychatapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ListPersonItemBinding

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerUserAdapter (val context: Context):
    RecyclerView.Adapter<RecyclerUserAdapter.ViewHolder>(){


    var users : ArrayList<User> = arrayListOf()
    var allUsers : ArrayList<User> = arrayListOf()
    lateinit var currentUser: User

    init{
        setupAllUserList()
    }

    fun setupAllUserList(){
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebasePath.user
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    users.clear()
                    for(data in snapshot.children){
                        var item = data.getValue<User>()
                        if(item?.uid.equals(myUid)){
                            currentUser = item!!
                            continue
                        }
                        allUsers.add(item!!)
                    }
                    users = allUsers.clone() as ArrayList<User>
                    notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("UserAdaptersetup", "error")
                }

            })
    }

    fun searchItem(target: String){
        if(target.equals("")){
            users = allUsers.clone() as ArrayList<User>
        }else{
            var matchedList = allUsers.filter{it.name!!.contains(target)}
            users.clear()
            matchedList.forEach{users.add(it)}
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: ListPersonItemBinding) : RecyclerView.ViewHolder(itemView.root){
        var background = itemView.userBackground
        var txt_name = itemView.userName
        var txt_email = itemView.userEmail
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_person_item, parent, false)
        return ViewHolder(ListPersonItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_name.text = users[position].name
        holder.txt_email.text = users[position].email

        holder.background.setOnClickListener(){
            addChatRoom(position)
        }
    }

    fun addChatRoom(position: Int){
        val opponent = users[position]
        //var database = FirebaseDatabase.getInstance().getReference("ChatRoom")
        var chatRoom = ChatRoom(
            mapOf(currentUser.uid!! to true, opponent.uid!! to true), null
        )

        //var myUid = FirebaseAuth.getInstance().uid
            //database.child("chatRooms")
        FirebasePath.chatRoom
            .orderByChild("users/${opponent.uid}").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value==null){
                        //database.child("chatRooms")
                        FirebasePath.chatRoom
                            .push()
                            .setValue(chatRoom).addOnSuccessListener {
                                goToChatRoom(chatRoom, opponent)
                            }
                    }else{
                        context.startActivity(Intent(context, MainActivity::class.java))
                        goToChatRoom(chatRoom, opponent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("UserAdapterAdd", "error")
                }

            })
    }


    fun goToChatRoom(chatRoom: ChatRoom, opponentUid: User){
        var intent = Intent(context, ChatRoomActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)
        intent.putExtra("Opponent", opponentUid)
        intent.putExtra("ChatRoomKey", "")
        context.startActivity(intent)
        (context as AppCompatActivity).finish()
    }

    override fun getItemCount(): Int {
        return users.size
    }
}