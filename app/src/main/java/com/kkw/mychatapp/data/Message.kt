package com.kkw.mychatapp.data

data class Message(
    val senderUid: String ="",
    val sent_date: String ="",
    val content: String="",
//    val confirmed:Boolean = false,
    val date: Boolean = false,
    val unconfirmedOpponent: Map<String, Boolean> = HashMap(),
    val messageId : String=""
):java.io.Serializable{
    companion object{
        fun toObject(myMap : HashMap<String, Object>): Message {
            return Message(
                myMap["senderUid"].toString(),
                myMap["sent_date"].toString(),
                myMap["content"].toString(),
//                myMap["confirmed"] as Boolean,
                myMap["date"] as Boolean,
                myMap["unconfirmedOpponent"] as HashMap<String, Boolean>,
                myMap["messageId"].toString()
            )
        }
    }
}
