//package com.kkw.mychatapp.fragment
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import com.kkw.mychatapp.MainActivity
//import com.kkw.mychatapp.R
//import com.kkw.mychatapp.databinding.FragmentSingUpBinding
//
//class SingUpFragment : Fragment() {
//
//    lateinit var auth : FirebaseAuth
//    lateinit var signUpBtn : Button
//    lateinit var editEmail : EditText
//    lateinit var editPw: EditText
//    lateinit var editName: EditText
//
//    var db = Firebase.firestore
//
//    private var _binding: FragmentSingUpBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var mainActivity: MainActivity
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        mainActivity = context as MainActivity
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentSingUpBinding.inflate(layoutInflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        initializeView()
//        initializeListener()
//    }
//
//    override fun onDestroy() {
//        _binding = null
//        super.onDestroy()
//    }
//
//    private fun initializeView(){
//        auth = FirebaseAuth.getInstance()
//        signUpBtn = binding.btnSignup
//        editEmail = binding.edtEmail
//        editPw = binding.edtPassword
//        editName = binding.edtOpponentName
//    }
//
//    private fun initializeListener(){
//        signUpBtn.setOnClickListener(){
//            signUp()
//        }
//    }
//
//    private fun signUp(){
//        var email = editEmail.text.toString()
//        var pw = editPw.text.toString()
//        var name = editName.text.toString()
//
//
//        auth.createUserWithEmailAndPassword(email, pw)
//            .addOnCompleteListener(mainActivity){
//                if(it.isSuccessful){
//                    try{
//                        val user = auth.currentUser
//                        val userId = user?.uid
//                        val userIdSt = userId.toString()
//
//                        val userData = hashMapOf(
//                            "email" to email,
//                            "name" to name,
//                            "uid" to userIdSt
//                        )
//
//                        db.collection("Users")
//                            .document(userIdSt)
//                            .set(userData)
//                            .addOnSuccessListener { documentReference ->
//                                Log.d("Sign in", "DocumentSnapshot added with ID: $documentReference")
//                            }
//                            .addOnFailureListener { e ->
//                                Log.w("Sign in", "Error adding document", e)
//                            }
//
//                        Log.e("UserId", "$userId")
//                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
//
//                    }catch (e: Exception){
//                        Log.e("df", "실패")
//                        e.printStackTrace()
//                    }
//                }
//            }
//
//    }
//
//
//}