package com.kkw.mychatapp.data

data class Message(
    val senderUid: String ="",
    val sent_date: String ="",
    val content: String="",
    val confirmed:Boolean = false,
    val isDate: Boolean = false
):java.io.Serializable{}
