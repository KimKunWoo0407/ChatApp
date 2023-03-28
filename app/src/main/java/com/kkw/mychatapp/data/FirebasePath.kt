package com.kkw.mychatapp.data

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebasePath {
    companion object{
        val chatRoom = FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms")

        val user = FirebaseDatabase.getInstance().getReference("User").child("users")

        val chatRoomPath = Firebase.firestore.collection("ChatRoom")
        val userPath = Firebase.firestore.collection("Users")
    }
}