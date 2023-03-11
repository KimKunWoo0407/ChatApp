package com.kkw.mychatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var signUpBtn : Button
    lateinit var edit_email : EditText
    lateinit var edit_pw: EditText
    lateinit var edit_name: EditText

    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        initializeListener()
    }

    fun initializeView(){
        auth = FirebaseAuth.getInstance()
        signUpBtn = binding.btnSignup
        edit_email = binding.edtEmail
        edit_pw = binding.edtPassword
        edit_name = binding.edtOpponentName
    }

    fun initializeListener(){
        signUpBtn.setOnClickListener(){
            signUp()
        }
    }

    fun signUp(){
        var email = edit_email.text.toString()
        var pw = edit_pw.text.toString()
        var name = edit_name.text.toString()


        auth.createUserWithEmailAndPassword(email, pw)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    try{
                        val user = auth.currentUser
                        val userId = user?.uid
                        val userIdSt = userId.toString()

                        FirebaseDatabase.getInstance().getReference("User")
                            .child("users")
                            .child(userId.toString())
                            .setValue(User(name, userIdSt, email))

                        Log.e("UserId", "$userId")
                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }

    }
}