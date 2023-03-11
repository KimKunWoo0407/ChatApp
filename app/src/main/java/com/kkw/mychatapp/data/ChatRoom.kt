package com.kkw.mychatapp.data

data class ChatRoom(
    val users: Map<String, Boolean>? = HashMap(),
    val messages: Map<String, Message>? = HashMap()
):java.io.Serializable{

}
