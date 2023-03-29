package com.kkw.mychatapp

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.toObject
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.Message
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.DateDeviderItemBinding
import com.kkw.mychatapp.databinding.ListTalkItemBinding
import com.kkw.mychatapp.databinding.ListTalkItemOtherBinding
import java.text.MessageFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface MessageHolder{

    fun bind(position: Int)
}

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerMessageAdapter(
    val context: Context,
    val chatRoomKey : String?,
    private val opponents: ArrayList<User>?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var messages : ArrayList<Message> = arrayListOf()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val recyclerView = (context as ChatRoomActivity).recycler_talks
    private var chatRoomPath : DatabaseReference = FirebasePath.chatRoom.child(chatRoomKey!!)
    var userMap = mutableMapOf<String, User>()

    init{
        opponents?.forEach{
            userMap[it.uid!!] = it
        }
        getMessages()
    }

    fun getLatestMessage(): Message? {
        return messages.lastOrNull()
    }


    private fun getMessages(){
//        chatRoomPath.child("messages")
//            .addValueEventListener(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    messages.clear()
//                    for(data in snapshot.children){
//                        messages.add(data.getValue<Message>()!!)
//                        messagesKeys.add(data.key!!)
//                    }
//                    notifyDataSetChanged()
//                    recyclerView.scrollToPosition(messages.size - 1)
//                }
//                override fun onCancelled(error: DatabaseError) {
//                }
//
//            })

        FirebasePath.chatRoomPath.document(chatRoomKey!!)
            .addSnapshotListener{
                it, e ->
                messages.clear()
                if(e!=null){
                    Log.w("Message", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (it != null) {
                    (it.data!!["messages"] as HashMap<String, HashMap<String, Object>>)
                        .forEach { msg->
                        messages.add(Message.toObject(msg.value))
                    }
                    messages = ArrayList(messages.sortedWith(
                        compareBy(
                            {it.sent_date},
                            {!it.date}
                        )
                    ))
                    notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    override fun getItemViewType(position: Int): Int {
        return if(messages[position].date) 2
        else{
            if (messages[position].senderUid == myUid) 1 else 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType){
            1->{
                val view = inflater
                    .inflate(R.layout.list_talk_item, parent, false)
                MyMessageViewHolder(ListTalkItemBinding.bind(view))
            }0->{
                val view = inflater
                    .inflate(R.layout.list_talk_item_other, parent, false)
                OtherMessageViewHolder(ListTalkItemOtherBinding.bind(view))
            }else->{
                val view = inflater
                    .inflate(R.layout.date_devider_item, parent, false)
                DateDivider(DateDeviderItemBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as MessageHolder).bind(position)
    }

    override fun getItemCount(): Int {
        return messages.size
    }


    inner class DateDivider(itemView:DateDeviderItemBinding): RecyclerView.ViewHolder(itemView.root), MessageHolder{
        private var txtDate = itemView.txtDate

        override fun bind(position: Int){
            var date = messages[position].sent_date
            txtDate.text = getDateText(date.substring(0,8))
        }

        private fun getDateText(sentDate: String):String{
            var dateText = ""

            if(sentDate.isNotBlank()){
                var format = "yyyyMMdd"
                var formatter = DateTimeFormatter.ofPattern(format)
                var d = LocalDate.parse(sentDate, formatter)
                dateText = "${d.year}년 ${d.monthValue}월 ${d.dayOfMonth}일 ${d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)}"
            }

            return dateText
        }
    }

    fun countUnconfirmed(message : Message) : Int{
        var unconfirmedNum = 0
        message.unconfirmedOpponent.forEach{
            if(it.value) unconfirmedNum++
        }
        return unconfirmedNum
    }

    fun numberingCallback(unconfirmedNum: Int, txtIsShown: TextView){
        if(unconfirmedNum>0){
            txtIsShown.text = unconfirmedNum.toString()
        }else{
            txtIsShown.visibility = View.GONE
        }
    }

    inner class MyMessageViewHolder(itemView:ListTalkItemBinding): RecyclerView.ViewHolder(itemView.root), MessageHolder{
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown

        override fun bind(position: Int){
            var message = messages[position]
            var sendDate = message.sent_date

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)


            numberingCallback(countUnconfirmed(message), txtIsShown = txtIsShown)


//            if(message.confirmed){
//                txtIsShown.visibility = View.GONE
//            }else
//                txtIsShown.visibility=View.VISIBLE
        }
    }

    inner class OtherMessageViewHolder(itemView: ListTalkItemOtherBinding):RecyclerView.ViewHolder(itemView.root), MessageHolder{
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown
        var sender = itemView.senderName

        override fun bind(position: Int){
            var message = messages[position]
            var sendDate = message.sent_date

            var senderName = userMap[message.senderUid]?.name

            sender.text = senderName

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            
            numberingCallback(countUnconfirmed(message), txtIsShown=txtIsShown)

//            if(message.confirmed){
//                txtIsShown.visibility = View.GONE
//            }else
//                txtIsShown.visibility=View.VISIBLE

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
//        chatRoomPath.child("messages")
//            .child(messagesKeys[position])
////            .child("confirmed").setValue(true)
//            .child("unconfirmedOpponent")
//            .updateChildren(hashMapOf<String, Any>(myUid to false))
//            .addOnSuccessListener {
////                Log.i("checkShown", "성공")
//            }

        FirebasePath.chatRoomPath.document(chatRoomKey!!)
            .update(mapOf("messages.${messages[position].messageId}.unconfirmedOpponent.${myUid}" to false))
            .addOnSuccessListener {
                Log.i("checkShown", "성공")
            }

    }
}