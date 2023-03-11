package com.kkw.mychatapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ListChatroomItemBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerChatRoomsAdapter(val context: Context) : RecyclerView.Adapter<RecyclerChatRoomsAdapter.ViewHolder>() {

    var chatRooms: ArrayList<ChatRoom> = arrayListOf()
    var chatRoomKeys: ArrayList<String> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    init{
        setupAllUserList()
    }

    fun setupAllUserList(){
        FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
            .orderByChild("users/$myUid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatRooms.clear()
                    for(data in snapshot.children){
                        chatRooms.add(data.getValue<ChatRoom>()!!)
                        chatRoomKeys.add(data.key!!)
                    }
                    notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                  Log.d("ChatRoomAdapter_setup", "error")
                }

            })
    }

    fun setupLastMessageAndDate(holder: ViewHolder, position: Int){
        try{
            var lastMessage = chatRooms[position].messages!!.values.sortedWith(compareBy { it.sent_date }).last()
            holder.txt_message.text = lastMessage.content
            holder.txt_date.text =
                getLastMessageTimeString(lastMessage.sent_date)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun setUpMessageCount(holder: ViewHolder, position: Int){
        try{
            var unconfirmedCount = chatRooms[position].messages!!.filter {
                !it.value.confirmed && !it.value.senderUid.equals(myUid)
            }.size

            if(unconfirmedCount>0){
                holder.txt_chatCount.visibility = View.VISIBLE
                holder.txt_chatCount.text = unconfirmedCount.toString()
            }else{
                holder.txt_chatCount.visibility=View.GONE
            }

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun getLastMessageTimeString(lastTimeString: String):String{
        try {
            var currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()) //현재 시각
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

            var messageMonth = lastTimeString.substring(4, 6).toInt()
            var messageDate = lastTimeString.substring(6, 8).toInt()
            var messageHour = lastTimeString.substring(8, 10).toInt()
            var messageMinute = lastTimeString.substring(10, 12).toInt()

            var formattedCurrentTimeString = currentTime.format(dateTimeFormatter)
            var currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
            var currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
            var currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
            var currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()

            var monthAgo = currentMonth - messageMonth
            var dayAgo = currentDate - messageDate
            var hourAgo = currentHour - messageHour
            var minuteAgo = currentMinute - messageMinute

            if (monthAgo > 0)
                return monthAgo.toString() + "개월 전"
            else {
                if (dayAgo > 0) {
                    if (dayAgo == 1)
                        return "어제"
                    else
                        return dayAgo.toString() + "일 전"
                } else {
                    if (hourAgo > 0)
                        return hourAgo.toString() + "시간 전"
                    else {
                        if (minuteAgo > 0)
                            return minuteAgo.toString() + "분 전"
                        else
                            return "방금"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }


    inner class ViewHolder(itemView: ListChatroomItemBinding) : RecyclerView.ViewHolder(itemView.root){
        var opponentUser = User("","")
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
        val userIdList = chatRooms[position].users!!.keys
        var opponent = userIdList.first{!it.equals(myUid)}

        FirebaseDatabase.getInstance().getReference("User").child("users").orderByChild("uid")
            .equalTo(opponent)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children){
                        holder.chatRoomKey=data.key.toString()!!
                        holder.opponentUser=data.getValue<User>()!!
                        holder.txt_name.text = holder.opponentUser.name.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatRoomAdapter_bind", "error")
                }

            })

        holder.background.setOnClickListener(){

            try{
                var intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("ChatRoom", chatRooms.get(position))
                intent.putExtra("Opponent", holder.opponentUser)
                intent.putExtra("ChatRoomKey", chatRoomKeys[position])
                context.startActivity(intent)
                (context as AppCompatActivity).finish()
            }catch(e: Exception){
                e.printStackTrace()
            }
        }

        if(chatRooms[position].messages!!.isNotEmpty()){
            setupLastMessageAndDate(holder, position)
            setUpMessageCount(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

}