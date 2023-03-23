package com.kkw.mychatapp.data

data class Message(
    val senderUid: String ="",
    val sent_date: String ="",
    val content: String="",
    val confirmed:Boolean = false,
    val date: Boolean = false,
    val unconfirmedOpponent: Map<String, Boolean> = HashMap()
):java.io.Serializable{}
