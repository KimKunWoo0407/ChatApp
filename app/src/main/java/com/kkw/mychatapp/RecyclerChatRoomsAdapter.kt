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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.Message
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ListChatroomItemBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import kotlin.reflect.typeOf

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerChatRoomsAdapter(val context: Context, val shouldShown: Boolean = true) : RecyclerView.Adapter<RecyclerChatRoomsAdapter.ViewHolder>() {

    var chatRooms: ArrayList<MyPair<String, ChatRoom>> = arrayListOf()
    var allChatRooms : ArrayList<MyPair<String, ChatRoom>> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var sorted = false

    var initNum = 0
    var count  = 0

    //lateinit var query : Query
    lateinit var registration : ListenerRegistration

    var idIndexMap = mutableMapOf<String, Int>()

    init{
        setupAllUserList()
    }

    fun sortChatRooms(){

        chatRooms.clear()

        if(allChatRooms.isEmpty())
            return

        allChatRooms = ArrayList(allChatRooms.sortedWith(
            compareBy { chatRoom ->
                chatRoom.second.messages?.values?.sortedWith(compareBy { it.sent_date })
                    ?.last()?.sent_date
            }
        ))
        sorted = true

        if(shouldShown){
            chatRooms = allChatRooms.clone() as ArrayList<MyPair<String,ChatRoom>>

            for(pos in chatRooms.indices){
                idIndexMap[chatRooms[pos].first]=pos
            }
        }

        notifyDataSetChanged()
    }

    fun removeRegistration() {
        registration.remove()
    }

    private fun setupAllUserList(){
        var query = FirebasePath.chatRoomPath
            .whereArrayContains("currentUsers",myUid).orderBy("lastDate", Query.Direction.DESCENDING)
//            .whereEqualTo("users.${myUid}", true).orderBy("lastDate")

        registration = query
            .addSnapshotListener{
                snapshot, error->
                if(error!=null){
                    Log.w("ChatRoom", "Listen failed.", error)
                    return@addSnapshotListener
                }

                Log.d("abcdefg", "${snapshot!!.documents}")
                val aaa = snapshot.documents.map {
                    docSnapshot->
                    FirebasePath.chatRoomPath.document(docSnapshot.id).collection("messages")
                        .get().addOnSuccessListener {
                            querySnapshot ->
                            val myMap = querySnapshot.documents.associate{
                                it.id to Message.toObject(it.data as HashMap<String, Any>)
                            } as HashMap<String, Message>

                            val chatroom = ChatRoom(
                                users = docSnapshot.data?.get("users") as Map<String, Boolean>,
                                singleRoom = docSnapshot.data!!["singleRoom"] as Boolean,
                                lastDate = docSnapshot.data!!["lastDate"] as String,
                                messages = myMap,
                                currentUsers = docSnapshot.data!!["currentUsers"] as ArrayList<String>
                            )
                        }

                }

                snapshot!!.documentChanges.forEach{
                    change->
                    val docSnapshot = change.document
                    FirebasePath.chatRoomPath.document(docSnapshot.id).collection("messages")
                        .get().addOnSuccessListener {
                            querySnapshot->
                            
                            val myMap = querySnapshot.documents.associate{
                                 it.id to Message.toObject(it.data as HashMap<String, Any>)
                            } as HashMap<String, Message>

                            val chatroom = ChatRoom(
                                users = docSnapshot.data["users"] as Map<String, Boolean>,
                                singleRoom = docSnapshot.data["singleRoom"] as Boolean,
                                lastDate = docSnapshot.data["lastDate"] as String,
                                messages = myMap,
                                currentUsers = docSnapshot.data["currentUsers"] as ArrayList<String>
                            )

                            if(change.type == DocumentChange.Type.ADDED){
                                allChatRooms.add(MyPair(docSnapshot.id, chatroom))
                                
                                if(shouldShown){ //IdIndedMap에 없다는 것은 추가된 적 없었다는 것. db에 저장된 순서그대로 저장가능
                                    if(idIndexMap[docSnapshot.id]==null)
                                        chatRooms.add(MyPair(docSnapshot.id, chatroom))
                                    idIndexMap[docSnapshot.id] = chatRooms.size -1
                                    notifyItemInserted(chatRooms.size - 1)
                                }
                                Log.d("rAdapter", "Added")
                            }
                            else if(change.type==DocumentChange.Type.MODIFIED){
                                if(idIndexMap[docSnapshot.id]!=null){
                                    //recyclierview가 position 기반이기 때문에 키와 position을 mapping하여 변화된 id를 알아내면 그에 대한 position의 viewholder를 bind한다
                                    chatRooms[idIndexMap[docSnapshot.id]!!].second = chatroom
                                    notifyItemChanged(idIndexMap[docSnapshot.id]!!)
                                }else{
                                    Log.d("rAdapter", "null")
                                }
                                Log.d("rAdapter", "modified")
                            }

                        }

                }

            }
    }

    fun searchItem(target: ArrayList<User>){
        chatRooms.clear()
        idIndexMap.clear()

        if(target.isEmpty()){
            Log.d("searchItem", "target null or Empty")
        }else {
            val result: ArrayList<MyPair<String, ChatRoom>> = arrayListOf()

            target.forEach{
                val uid = it.uid
                val tmp = allChatRooms.filter { chatRoom -> chatRoom.second.users.contains(uid) }
                result.addAll(tmp)
            }
            result.distinctBy { chatRoom->chatRoom.first }.forEach { chatRooms.add(it) }

        }

        for(pos in chatRooms.indices){
            idIndexMap[chatRooms[pos].first]=pos
        }

        notifyDataSetChanged()

    }

    private fun setupLastMessageAndDate(holder: ViewHolder, position: Int){

        try{
            val lastMessage = chatRooms[position].second.messages!!.values.toList().sortedWith(
                compareBy (
                    {it.sent_date},
                    {!it.date}
                )
            ).last()

            holder.txt_message.text = lastMessage.content
            holder.txt_date.text =
                getLastMessageTimeString(lastMessage.sent_date)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun setUpMessageCount(holder: ViewHolder, position: Int){

        try{

            val unconfirmedCount = chatRooms[position].second.messages!!
                .filter {
                    it.value.unconfirmedOpponent.containsKey(myUid) && it.value.unconfirmedOpponent[myUid] == true
            }.size

            if(unconfirmedCount>0){
                holder.txt_chatCount.visibility = View.VISIBLE
                holder.txt_chatCount.text = unconfirmedCount.toString()
            }else{
                holder.txt_chatCount.visibility=View.INVISIBLE
            }

        }catch (e:Exception){
            Log.d("rAdapter", "catch")
            e.printStackTrace()
        }
    }


    inner class ViewHolder(itemView: ListChatroomItemBinding) : RecyclerView.ViewHolder(itemView.root){
        var opponentUser = arrayListOf<User?>()
        var chatRoomKey = ""
        var background = itemView.background
        var txt_name = itemView.txtName
        var txt_message = itemView.txtMessage
        var txt_chatCount = itemView.txtChatCount
        var txt_date = itemView.txtMessageDate
        var profile = itemView.roomProfileImage

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_chatroom_item, parent, false)
        return ViewHolder(ListChatroomItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val roomKey = chatRooms[position].first
        val userIdList = chatRooms[position].second.users.keys
//        var opponent = userIdList.first{ it != myUid }
        //val opponent = userIdList as MutableSet<String>
        val opponents = userIdList.toMutableList()
        opponents.removeIf {it==myUid}

        holder.chatRoomKey = roomKey
        var chatRoomName = ""

        FirebasePath.userPath
            .whereIn("uid", opponents)
            .get()
            .addOnSuccessListener {
                querySnapshot->    
                querySnapshot.forEach {
                    val user = it.toObject<User>()
                    holder.opponentUser.add(user)
                    chatRoomName += user.name
                    chatRoomName+=", "
                }
                if(chatRoomName.isNotEmpty())
                    holder.txt_name.text = chatRoomName.substring(0, chatRoomName.length-2)
            }
        

//        FirebasePath.user.orderByChild("uid")
//            .addListenerForSingleValueEvent(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (data in snapshot.children){
////                        holder.chatRoomKey=data.key.toString()!!
//                        var user = data.getValue<User>()!!
//                        if(opponent.contains(user.uid)){
//                            holder.opponentUser.add(user)
//                            chatRoomName+=user.name
//                            chatRoomName+=", "
//                        }
//                    }
//                    if(chatRoomName.isNotEmpty()){
//                        holder.txt_name.text = chatRoomName.substring(0, chatRoomName.length-3)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.d("ChatRoomAdapter_bind", "error")
//                }
//
//            })

        holder.background.setOnClickListener(){
            try{
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("ChatRoom", chatRooms[position].second)
                intent.putExtra("Opponent", holder.opponentUser)
//                intent.putExtra("ChatRoomKey", chatRoomKeys[position])
                intent.putExtra("ChatRoomKey", chatRooms[position].first)
                intent.putExtra("Name", chatRoomName)
                context.startActivity(intent)
                //(context as AppCompatActivity).finish()
            }catch(e: Exception){
                e.printStackTrace()
            }
        }

        if(chatRooms[position].second.messages!!.isNotEmpty()){
            setupLastMessageAndDate(holder, position)
            setUpMessageCount(holder, position)
        }
    }

    private fun getLastMessageTimeString(lastTimeString: String):String{
        try {
            val currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()) //현재 시각
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

            val messageMonth = lastTimeString.substring(4, 6).toInt()
            val messageDate = lastTimeString.substring(6, 8).toInt()
            val messageHour = lastTimeString.substring(8, 10).toInt()
            val messageMinute = lastTimeString.substring(10, 12).toInt()

            val formattedCurrentTimeString = currentTime.format(dateTimeFormatter)
            val currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
            val currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
            val currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
            val currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()

            val monthAgo = currentMonth - messageMonth
            val dayAgo = currentDate - messageDate
            val hourAgo = currentHour - messageHour
            val minuteAgo = currentMinute - messageMinute

            if (monthAgo > 0)
                return monthAgo.toString() + "개월 전"
            else {
                return if (dayAgo > 0) {
                    if (dayAgo == 1)
                        "어제"
                    else
                        dayAgo.toString() + "일 전"
                } else {
                    if (hourAgo > 0)
                        hourAgo.toString() + "시간 전"
                    else {
                        if (minuteAgo > 0)
                            minuteAgo.toString() + "분 전"
                        else
                            "방금"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

}