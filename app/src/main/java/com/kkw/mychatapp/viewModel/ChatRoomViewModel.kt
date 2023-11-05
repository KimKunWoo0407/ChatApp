package com.kkw.mychatapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kkw.mychatapp.data.ChatRoom
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatRoomViewModel: ViewModel() {

    private var _chatRooms = MutableLiveData<MutableList<ChatRoom>>().apply{
        value = mutableListOf()
    }

    val chatRooms: LiveData<MutableList<ChatRoom>>
        get() = _chatRooms

    fun setChatRooms(){

    }
}

