package com.kkw.mychatapp

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.kkw.mychatapp.data.Message
import com.kkw.mychatapp.databinding.ListTalkItemBinding
import com.kkw.mychatapp.databinding.ListTalkItemOtherBinding

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerMessageAdapter(
    val context: Context,
    val chatRoomKey : String?,
    val opponentUid: String?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var messages : ArrayList<Message> = arrayListOf()
    var messagesKeys : ArrayList<String> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val recyclerView = (context as ChatRoomActivity).recycler_talks

    init{
        getMessages()
    }

    fun getMessages(){
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms").child(chatRoomKey!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for(data in snapshot.children){
                        messages.add(data.getValue<Message>()!!)
                        messagesKeys.add(data.key!!)
                    }
                    notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderUid.equals(myUid)) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            1->{
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.list_talk_item, parent, false)

                MyMessageViewHolder(ListTalkItemBinding.bind(view))

            }else->{
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.list_talk_item_other, parent, false)
                OtherMessageViewHolder(ListTalkItemOtherBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(messages[position].senderUid.equals(myUid)) {
            (holder as MyMessageViewHolder).bind(position)
        }else{
            (holder as OtherMessageViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }


    inner class MyMessageViewHolder(itemView:ListTalkItemBinding): RecyclerView.ViewHolder(itemView.root){
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown

        fun bind(position: Int){
            var message = messages[position]
            var sendDate = message.sent_date

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            if(message.confirmed.equals(true)){
                txtIsShown.visibility = View.GONE
            }else
                txtIsShown.visibility=View.VISIBLE
        }
    }

    inner class OtherMessageViewHolder(itemView: ListTalkItemOtherBinding):RecyclerView.ViewHolder(itemView.root){
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown

        fun bind(position: Int){
            var message = messages[position]
            var sendDate = message.sent_date

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            if(message.confirmed.equals(true)){
                txtIsShown.visibility = View.GONE
            }else
                txtIsShown.visibility=View.VISIBLE

            setShown(position)
        }
    }

    fun getDateText(sendDate: String): String {    //메시지 전송 시각 생성

        var dateText = ""
        var timeString = ""
        if (sendDate.isNotBlank()) {
            timeString = sendDate.substring(8, 12)
            var hour = timeString.substring(0, 2)
            var minute = timeString.substring(2, 4)

            var timeformat = "%02d:%02d"

            if (hour.toInt() > 11) {
                dateText += "오후 "
                dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
            } else {
                dateText += "오전 "
                dateText += timeformat.format(hour.toInt(), minute.toInt())
            }
        }
        return dateText
    }

    fun setShown(position: Int){
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms").child(chatRoomKey!!).child("messages")
            .child(messagesKeys[position]).child("confirmed").setValue(true)
            .addOnSuccessListener {
                Log.i("checkShown", "성공")
            }
    }
}