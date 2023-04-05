package com.kkw.mychatapp.data

data class ChatRoom(
    val users: Map<String, Boolean> = HashMap(),
    val messages: Map<String, Message>? = HashMap(),
    //val messages: ArrayList<Message>? = arrayListOf(),
    var singleRoom: Boolean = true,
    var roomKey: String="",
    var lastDate: String=""
):java.io.Serializable{

    companion object{
        fun toObject(myMap : HashMap<String, Any>): ChatRoom{
            var msg : HashMap<String, Message> = HashMap()
            (myMap["messages"] as HashMap<String, HashMap<String, Any>>).forEach {
                msg[it.key] = Message.toObject(it.value)
            }

            return ChatRoom(
                users = myMap["users"] as HashMap<String, Boolean>,
                messages = msg,
                singleRoom = myMap["singleRoom"] as Boolean,
                roomKey = myMap["roomKey"].toString()

            )
        }
    }

}
