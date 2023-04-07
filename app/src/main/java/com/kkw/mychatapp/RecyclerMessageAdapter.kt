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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.Message
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.DateDeviderItemBinding
import com.kkw.mychatapp.databinding.ListTalkItemBinding
import com.kkw.mychatapp.databinding.ListTalkItemOtherBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface MessageHolder{

    fun bind(position: Int)
}

data class MyPair<T, U>(var first : T, var second: U)

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerMessageAdapter(
    val context: Context,
    private val chatRoomKey : String?,
    opponents: ArrayList<User>?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var messages : ArrayList<MyPair<String, Message>> = arrayListOf()

    private val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val recyclerView = (context as ChatRoomActivity).recycler_talks
    var userMap = mutableMapOf<String, User>()
    var sorted = false

    lateinit var query : CollectionReference
    lateinit var registration : ListenerRegistration

    var idIndexMap = mutableMapOf<String, Int>()

    init{
        opponents?.forEach{
            userMap[it.uid!!] = it
        }
        getMessages()

    }


    fun getLatestMessage(): Message? {
        return messages.lastOrNull()?.second
    }


    fun removeRegistration() {
        registration.remove()
    }

    fun renewLastDate(date: String = "", added: Boolean = true){
        if(date.isNotEmpty() && added)
            FirebasePath.chatRoomPath.document(chatRoomKey!!)
                .update(mapOf("lastDate" to date))
        else
            FirebasePath.chatRoomPath.document(chatRoomKey!!)
                .update(mapOf("modifiedDate" to date))
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

        messages.clear()
        query = FirebasePath.chatRoomPath.document(chatRoomKey!!)
            .collection("messages")

        registration = query
            .addSnapshotListener{
                it, e->
                if(e!=null){
                    Log.w("Message", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (it != null) {
                    it.documentChanges.forEach{
                        change->
                        var doc = change.document
                        var msg = Message.toObject(doc.data as HashMap<String, Any>)
//                        messages.add(Pair(doc.id, msg))
                        if(change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED)
                        {
//                            Log.d("mAdapterAdded", "${doc.id}")
                            Log.d("mAdapter", "Added")
                            messages.add(MyPair(doc.id, msg))
                            if(msg.senderUid!=myUid)
                                setShown(messages.size - 1)
                            idIndexMap[doc.id] = messages.size -1
//                            Log.d("mAdapterAdded", "${doc.id} : ${idIndexMap[doc.id]}")
                            if(sorted)
                            {
                                notifyItemInserted(messages.size - 1)
                                recyclerView.scrollToPosition(messages.size - 1)
                                renewLastDate(msg.sent_date)
                            }

                        }else if(change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED){
//                            Log.d("mAdapterModified", "${doc.id} : ${idIndexMap[doc.id]}")
                            Log.d("mAdapter", "modified")
                            messages[idIndexMap[doc.id]!!].second = msg
                            notifyItemChanged(idIndexMap[doc.id]!!)
                            renewLastDate(msg.sent_date, false)
                        }

                    }
                    if(!sorted)
                    {
//                        Log.d("mAdapter", "sorted")
                        messages = ArrayList(messages.sortedWith(
                            compareBy(
                                {it.second.sent_date},
                                {!it.second.date}
                            )
                        ))
                        sorted = true
                        for(pos in messages.indices){
                            idIndexMap[messages[pos].first] = pos
                        }
                        if(messages.isNotEmpty())
                            renewLastDate(messages.last().second.sent_date)
                        notifyDataSetChanged()
                    }
                    //notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }

    }

    override fun getItemViewType(position: Int): Int {
        return if(messages[position].second.date) 2
        else{
            if (messages[position].second.senderUid == myUid) 1 else 0
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
            var date = messages[position].second.sent_date
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

    fun countUnconfirmed(message : Message, position : Int = -1) : Int{
//        if(position>=0){
//            Log.d("mAdapterCount", "$position + : ${message.unconfirmedOpponent}")
//        }
        var unconfirmedNum = 0
        message.unconfirmedOpponent.forEach{
            if(it.value) unconfirmedNum++
        }
        return unconfirmedNum
    }

    fun numberingCallback(unconfirmedNum: Int, txtIsShown: TextView){
        if(unconfirmedNum>0){
            txtIsShown.visibility = View.VISIBLE
            txtIsShown.text = unconfirmedNum.toString()
        }else{
            txtIsShown.visibility = View.INVISIBLE
        }
    }

    inner class MyMessageViewHolder(itemView:ListTalkItemBinding): RecyclerView.ViewHolder(itemView.root), MessageHolder{
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown

        override fun bind(position: Int){
            var message = messages[position]
            var sendDate = message.second.sent_date

            txtMessage.text = message.second.content
            txtDate.text = getDateText(sendDate)


            numberingCallback(countUnconfirmed(message.second, position), txtIsShown = txtIsShown)
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
            var sendDate = message.second.sent_date

            var senderName = userMap[message.second.senderUid]?.name

            sender.text = senderName

            txtMessage.text = message.second.content
            txtDate.text = getDateText(sendDate)

//            Log.d("mAdapterOtherViewHolder", "$position : ${txtMessage.text}")

            numberingCallback(countUnconfirmed(message.second, position), txtIsShown=txtIsShown)

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

        var doc =  FirebasePath.chatRoomPath.document(chatRoomKey!!)
            .collection("messages")
            .document("${messages[position].first}")

//        Log.d("mAdaptersetShown", "$position : ${messages[position].second.content}")

        doc.get()
            .addOnSuccessListener { snapshot ->
                if((snapshot.data!!["unconfirmedOpponent"] as HashMap<String, Boolean>)[myUid] == true){
                    doc.update(mapOf("unconfirmedOpponent.${myUid}" to false))
//                        .addOnCompleteListener{
//                            Log.d("mAdaptersetShown", "ok")
//                        }
                }

            }

    }
}