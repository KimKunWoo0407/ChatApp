package com.kkw.mychatapp.data

data class ChatRoom(
    val users: Map<String, Boolean> = HashMap(),
    val messages: Map<String, Message>? = HashMap(),
    //val messages: ArrayList<Message>? = arrayListOf(),
    var singleRoom : Boolean = true,
    var roomKey: String=""
):java.io.Serializable{

}
