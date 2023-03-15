package com.kkw.mychatapp.data

import com.google.firebase.database.FirebaseDatabase

class FirebasePath {
    companion object{
        val chatRoom = FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms")

        val user = FirebaseDatabase.getInstance().getReference("User").child("users")
    }
}