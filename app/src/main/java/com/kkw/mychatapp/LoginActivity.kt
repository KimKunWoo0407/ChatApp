package com.kkw.mychatapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kkw.mychatapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    lateinit var auth:FirebaseAuth
    lateinit var signUpBtn : Button
    lateinit var signInBtn : Button
    lateinit var edit_email : EditText
    lateinit var edit_pw: EditText

    lateinit var binding: ActivityLoginBinding
    lateinit var preference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initProperty()
        initializeView()
        initalizeListener()
    }

    fun initProperty(){
        auth = FirebaseAuth.getInstance()
        preference = getSharedPreferences("setting", MODE_PRIVATE)
    }

    fun initializeView(){
        signInBtn = binding.btnSignIn
        signUpBtn = binding.btnSignup
        edit_email = binding.edtEmail
        edit_pw = binding.edtPassword

        edit_email.setText((preference.getString("email", "")))
        edit_pw.setText(preference.getString("password", ""))

    }

    fun initalizeListener(){
        signInBtn.setOnClickListener(){
            signInWithEmailAndPassword()
        }
        signUpBtn.setOnClickListener(){
            startActivity(Intent(this,SignUpActivity::class.java ))
        }
    }

    fun signInWithEmailAndPassword(){
        if(edit_email.text.toString().isNullOrBlank()&&
                edit_pw.text.toString().isNullOrBlank()){
            Log.d("로그인","비어있음")
        }else{
            auth.signInWithEmailAndPassword(
                edit_email.text.toString(),
                edit_pw.text.toString())
                .addOnCompleteListener(this){
                    if(it.isSuccessful){
                        Log.d("로그인","성공")
                        val user = auth.currentUser
                        updateUI(user)
                        finish()
                    }
                }
        }
    }

    fun updateUI(user:FirebaseUser?){
        if(user!=null){
            try{
                var preference = getSharedPreferences("setting", MODE_PRIVATE).edit()
                preference.putString("email", edit_email.text.toString())
                preference.putString("password",edit_pw.text.toString())
                preference.apply()
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

}