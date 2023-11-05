package com.kkw.mychatapp

import android.app.Application
import com.kkw.mychatapp.repository.Repository

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
    }

}