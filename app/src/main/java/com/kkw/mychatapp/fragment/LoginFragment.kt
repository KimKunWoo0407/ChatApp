package com.kkw.mychatapp.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kkw.mychatapp.MainActivity
import com.kkw.mychatapp.databinding.FragmentLoginBinding

@RequiresApi(Build.VERSION_CODES.O)
class LoginFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var signUpBtn : Button
    lateinit var signInBtn : Button
    private lateinit var editEmail : EditText
    private lateinit var editPw: EditText

    lateinit var preference: SharedPreferences

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProperty()
        initializeView()
        initializeListener()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun initProperty(){
        auth = FirebaseAuth.getInstance()
        preference = mainActivity.getSharedPreferences("setting", AppCompatActivity.MODE_PRIVATE)
    }

    private fun initializeView(){
        signInBtn = binding.btnSignIn
        signUpBtn = binding.btnSignup
        editEmail = binding.edtEmail
        editPw = binding.edtPassword

        editEmail.setText((preference.getString("email", "")))
        editPw.setText(preference.getString("password", ""))

    }


    private fun initializeListener(){
        signInBtn.setOnClickListener{
            signInWithEmailAndPassword()
        }
        signUpBtn.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(mainActivity.container.id, SingUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun signInWithEmailAndPassword(){
        if(editEmail.text.toString().isNullOrBlank()&&
            editPw.text.toString().isNullOrBlank()){
            Log.d("로그인","비어있음")
        }else{
            auth.signInWithEmailAndPassword(
                editEmail.text.toString(),
                editPw.text.toString())
                .addOnCompleteListener(mainActivity){
                    if(it.isSuccessful){
                        Log.d("로그인","성공")
                        val user = auth.currentUser
                        updateUI(user)
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?){
        if(user!=null){
            try{
                var preference = mainActivity.getSharedPreferences("setting", AppCompatActivity.MODE_PRIVATE).edit()
                preference.putString("email", editEmail.text.toString())
                preference.putString("password",editPw.text.toString())
                preference.apply()

                parentFragmentManager.beginTransaction()
                    .replace(mainActivity.container.id, MainFragment())
                    .commit()

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }



}