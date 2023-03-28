package com.kkw.mychatapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kkw.mychatapp.data.User
import com.kkw.mychatapp.databinding.ActivitySignUpBinding

@RequiresApi(Build.VERSION_CODES.O)
class SignUpActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var signUpBtn : Button
    lateinit var edit_email : EditText
    lateinit var edit_pw: EditText
    lateinit var edit_name: EditText

    lateinit var binding: ActivitySignUpBinding

    var db = Firebase.firestore

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


//                        FirebaseDatabase.getInstance().getReference("User")
//                            .child("users")
//                            .child(userId.toString())
//                            .setValue(User(name, userIdSt, email))

                        val userData = hashMapOf(
                            "email" to email,
                            "name" to name,
                            "uid" to userIdSt
                        )

                        db.collection("Users")
                            .document(userIdSt)
                            .set(userData)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Sign in", "DocumentSnapshot added with ID: $documentReference")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Sign in", "Error adding document", e)
                            }

                        Log.e("UserId", "$userId")
                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))

                    }catch (e: Exception){
                        Log.e("df", "실패")
                        e.printStackTrace()
                    }
                }
            }

    }
}