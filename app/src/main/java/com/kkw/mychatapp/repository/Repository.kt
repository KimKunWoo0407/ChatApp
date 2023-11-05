package com.kkw.mychatapp.repository

import android.content.Context
import com.kkw.mychatapp.dao.ChatRoomDao

class Repository private constructor(context: Context) {

    private val chatRoomDao = ChatRoomDao()



    companion object{
        private var INSTANCE: Repository? = null

        fun initialize(context: Context){
            if(INSTANCE == null){
                INSTANCE = Repository(context)
            }
        }

        fun get(): Repository{
            return INSTANCE?:
            throw IllegalStateException("repository must be initialized")
        }

    }

}