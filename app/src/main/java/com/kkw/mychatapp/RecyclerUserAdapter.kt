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
import com.kkw.mychatapp.databinding.ListAddPersonCheckItemBinding
import com.kkw.mychatapp.databinding.ListPersonItemBinding

interface UserHolder{
    fun bind(position: Int)
}

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerUserAdapter (val context: Context, val isInRoom: Boolean = false):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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
                    if(isInRoom)
                        users = allUsers.clone() as ArrayList<User>
                    notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("UserAdaptersetup", "error")
                }

            })
    }

    fun searchItem(target: String) : ArrayList<User> {
        users.clear()
        var matchedList:List<User> = listOf()

        if(target != ""){
            //users = allUsers.clone() as ArrayList<User>
            matchedList = allUsers.filter{it.name!!.contains(target)}
            matchedList.forEach{users.add(it)}
        }
        notifyDataSetChanged()

        return matchedList.toCollection(ArrayList())
    }

    inner class ViewHolder(itemView: ListPersonItemBinding) : RecyclerView.ViewHolder(itemView.root), UserHolder{
        var background = itemView.userBackground
        var txtName = itemView.userName
        var txtEmail = itemView.userEmail

        override fun bind(position: Int){
            txtName.text = users[position].name
            txtEmail.text = users[position].email

            background.setOnClickListener(){
                addChatRoom(position)
            }
        }
    }

    inner class ToSelectViewHolder(itemView: ListAddPersonCheckItemBinding) : RecyclerView.ViewHolder(itemView.root),UserHolder{

        var background = itemView.userBackground
        var txtName = itemView.userName
        var checkIcon = itemView.checker

        var checked = false
        var included = false

        override fun bind(position: Int) {
            txtName.text = users[position].name

            background.setOnClickListener(){
                checkIcon.isSelected = !checked
                checked = !checked
            }

        }

    }


    override fun getItemViewType(position: Int): Int {
        return if(isInRoom) 1
        else 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)

        return when(viewType){
            1->{
                val view = inflater.inflate(R.layout.list_add_person_check_item, parent, false)
                ToSelectViewHolder(ListAddPersonCheckItemBinding.bind(view))
            }else->{
                val view = inflater.inflate(R.layout.list_person_item, parent, false)
                ViewHolder(ListPersonItemBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserHolder).bind(position)
    }

    private fun addChatRoom(position: Int){
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
                        //context.startActivity(Intent(context, MainActivity::class.java))
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
        //(context as AppCompatActivity).finish()
    }

    override fun getItemCount(): Int {
        return users.size
    }

}