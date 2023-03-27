package com.kkw.mychatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.kkw.mychatapp.data.FirebasePath
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ActivityUserProfileBinding

class UserProfile : AppCompatActivity() {

    lateinit var binding: ActivityUserProfileBinding

    lateinit var txtName : TextView
    lateinit var emailTxt : TextView
    lateinit var uid : String
    lateinit var chatStartBtn : Button
    lateinit var opponent : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        initializeProperty()


    }

    private fun initializeProperty(){
        uid = intent.getStringExtra("uid")!!
        FirebasePath.user.orderByKey().equalTo(uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value==null){
                        Log.d("profile", "잘못된 uid")
                    }else{
                        Log.d("snapshot", snapshot.key!!)
                        opponent = snapshot.getValue(User::class.java)!!
                        Log.d("profile", opponent.uid!!)
                        txtName.text = opponent.name
                        emailTxt.text = opponent.email

                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("profile", "불러오기 error")
                }

            })
    }

    private fun initializeView(){
        txtName = binding.profileNameTxt
        emailTxt = binding.profileEmailTxt
        chatStartBtn = binding.oneOnOneButton

    }
}