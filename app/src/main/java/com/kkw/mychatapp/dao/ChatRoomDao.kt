package com.kkw.mychatapp.dao

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.kkw.mychatapp.adapter.MyPair
import com.kkw.mychatapp.data.ChatRoom
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.Message

class ChatRoomDao {

    private val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    private lateinit var registration : ListenerRegistration

    interface ChatRoomListener{
        fun onItemChanged(chatRoom: ChatRoom)
    }

    private lateinit var chatRoomListener: ChatRoomListener

    fun setChatRoomListener(listener: ChatRoomListener){
        chatRoomListener = listener
    }

    fun removeRegistration() {
        registration.remove()
    }

    fun getChatRooms(){

    }

    fun initUserList(){

        var query = FirebasePath.chatRoomPath
            .whereArrayContains("currentUsers",myUid).orderBy("lastDate", Query.Direction.DESCENDING)

        registration = query
            .addSnapshotListener{
                    snapshot, error->
                if(error!=null){
                    Log.w("ChatRoom", "Listen failed.", error)
                    return@addSnapshotListener
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

                            val chatRoom = ChatRoom(
                                roomKey = docSnapshot.id,
                                users = docSnapshot.data["users"] as Map<String, Boolean>,
                                singleRoom = docSnapshot.data["singleRoom"] as Boolean,
                                lastDate = docSnapshot.data["lastDate"] as String,
                                messages = myMap,
                                currentUsers = docSnapshot.data["currentUsers"] as ArrayList<String>
                            )

                            chatRoomListener.onItemChanged(chatRoom)

//                            if(change.type == DocumentChange.Type.ADDED){
//                                chatRoomListener.onItemAdded(chatRoom)
//                                allChatRooms.add(MyPair(docSnapshot.id, chatroom))
//
//                                if(shouldShown){ //IdIndedMap에 없다는 것은 추가된 적 없었다는 것. db에 저장된 순서그대로 저장가능
//                                    if(idIndexMap[docSnapshot.id]==null)
//                                        chatRooms.add(MyPair(docSnapshot.id, chatroom))
//                                    idIndexMap[docSnapshot.id] = chatRooms.size -1
//                                    notifyItemInserted(chatRooms.size - 1)
//                                }
//                                Log.d("rAdapter", "Added")
//                            }
//                            else if(change.type== DocumentChange.Type.MODIFIED){
//                                chatRoomListener.onItemModified(chatRoom)
//                                if(idIndexMap[docSnapshot.id]!=null){
//                                    //recyclierview가 position 기반이기 때문에 키와 position을 mapping하여 변화된 id를 알아내면 그에 대한 position의 viewholder를 bind한다
//                                    chatRooms[idIndexMap[docSnapshot.id]!!].second = chatroom
//                                    notifyItemChanged(idIndexMap[docSnapshot.id]!!)
//                                }else{
//                                    Log.d("rAdapter", "null")
//                                }
//                                Log.d("rAdapter", "modified")
//                            }

                        }

                }

            }

    }

}