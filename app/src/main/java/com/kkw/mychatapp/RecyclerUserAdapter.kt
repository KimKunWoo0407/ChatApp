package com.kkw.mychatapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.toObject
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ListAddPersonCheckItemBinding
import com.kkw.mychatapp.databinding.ListPersonItemBinding

interface UserHolder{
    fun bind(position: Int)
}

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerUserAdapter (val context: Context, val roomKey: String = ""):
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var users : ArrayList<User> = arrayListOf()
    var allUsers : ArrayList<User> = arrayListOf()
    lateinit var myUid : String

    //lateinit var currentUser: User

    lateinit var listener:IItemClickListener

    fun setOnItemClickListener(clickListener: IItemClickListener){
        listener= clickListener
    }

    init{
        setupAllUserList()
    }

    private fun setupAllUserList(){
        myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        FirebasePath.userPath
            .whereNotEqualTo("uid", myUid)
            .get()
            .addOnSuccessListener {
                documents->
                users.clear()
                for (document in documents){
                    var item = document.toObject<User>()
                    allUsers.add(item)
                }
                if(!roomKey.isNullOrEmpty())
                    users = allUsers.clone() as ArrayList<User>
                notifyDataSetChanged()
            }.addOnFailureListener { exception ->
                Log.w("UserAdapter", "Error getting documents: ", exception)
            }


//        FirebasePath.user
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    users.clear()
//                    for(data in snapshot.children){
//                        var item = data.getValue<User>()
//                        if(item?.uid.equals(myUid)){
//                            currentUser = item!!
//                            continue
//                        }
//                        allUsers.add(item!!)
//                    }
//                    if(!roomKey.isNullOrEmpty())
//                        users = allUsers.clone() as ArrayList<User>
//                    notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.d("UserAdaptersetup", "error")
//                }
//
//            })
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

                //goToProfile(users[position].uid!!)
                addChatRoom(position)
            }
        }
    }

    inner class ToSelectViewHolder(itemView: ListAddPersonCheckItemBinding) : RecyclerView.ViewHolder(itemView.root),UserHolder{

        var background = itemView.userBackground
        var txtName = itemView.userName
        var checkIcon = itemView.checker

        var checked = false

        var _included = false

        lateinit var itemListener:IItemClickListener

        private fun includedCheck(position: Int) : Boolean{
            Log.d("uAdapter", "$position")
            return ((context as ChatRoomActivity).supportFragmentManager.findFragmentById(R.id.container) as AddOpponentFragment)
                .curOpponents.contains(users[position])
        }

        override fun bind(position: Int) {
            txtName.text = users[position].name

            _included = includedCheck(position)

            if(!_included){
                itemListener=listener

                background.setOnClickListener(){
                    if(itemListener!=null){
                        itemListener.onItemClick(this@ToSelectViewHolder,it, adapterPosition)
                    }
                }
            }
            else{
                background.isClickable=false
                checkIcon.visibility = View.GONE
            }

        }

    }


    override fun getItemViewType(position: Int): Int {
        return if(roomKey.isNullOrEmpty()) 0
        else 1
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

    private fun addChatRoom(position: Int){ //1대1 채팅방 만들기
        val opponent = arrayListOf<User>()
        opponent.add(users[position])
        //var database = FirebaseDatabase.getInstance().getReference("ChatRoom")
        var chatRoom = ChatRoom(
            users = mapOf(myUid to true, opponent[0].uid!! to true)
        )

        FirebasePath.chatRoomPath
            .whereEqualTo("singleRoom", true)
            .whereEqualTo("users.${opponent[0].uid!!}", true)
            .get()
            .addOnSuccessListener { ref->
                if(!ref.isEmpty){
                    goToChatRoom(chatRoom, opponent, ref.first().id)
                }else{
                    FirebasePath.chatRoomPath
                        .add(chatRoom)
                        .addOnSuccessListener {
                            goToChatRoom(chatRoom, opponent, it.id)
                        }
                }
            } .addOnFailureListener { e ->
                Log.w("UAdapter", "Error adding document", e)
            }

        //var myUid = FirebaseAuth.getInstance().uid
            //database.child("chatRooms")
//        FirebasePath.chatRoom
//            //.orderByChild("users/${opponent[0].uid}").equalTo(true)
//            .orderByChild("singleRoom").equalTo(opponent[0].uid)
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if(snapshot.value==null){
//                        //database.child("chatRooms")
//                        FirebasePath.chatRoom
//                            .push()
//                            .setValue(chatRoom).addOnSuccessListener {
//                                goToChatRoom(chatRoom, opponent)
//                            }
//                    }else{
//                        // context.startActivity(Intent(context, MainActivity::class.java))
//                        Log.d("userAdapter", snapshot.key!!)
//                        goToChatRoom(chatRoom, opponent, snapshot.key!!)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.d("UserAdapterAdd", "error!")
//                }
//
//            })
    }

    fun goToProfile(opponentUid : String){
       var intent = Intent(context, UserProfile::class.java)
       intent.putExtra("uid", opponentUid)
       context.startActivity(intent)
    }

    fun goToChatRoom(chatRoom: ChatRoom, opponent: ArrayList<User>, roomKey:String=""){
        var intent = Intent(context, ChatRoomActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)
        intent.putExtra("Opponent", opponent)
        intent.putExtra("ChatRoomKey", roomKey)
        intent.putExtra("Name", opponent[0].name)
        context.startActivity(intent)
        //(context as AppCompatActivity).finish()
    }

    override fun getItemCount(): Int {
        return users.size
    }

}