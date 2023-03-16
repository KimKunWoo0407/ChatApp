package com.kkw.mychatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kkw.mychatapp.databinding.ActivityUserProfileBinding

class UserProfile : AppCompatActivity() {

    lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}