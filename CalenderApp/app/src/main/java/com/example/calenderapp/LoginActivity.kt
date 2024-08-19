package com.example.calenderapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val textView = binding.registerNow

        if (auth.currentUser != null){
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        textView.setOnClickListener{registerNowClicked()}

        binding.loginBtn.setOnClickListener{loginBtnClicked()}


    }

    private fun loginBtnClicked() {
        val email = binding.etEmailLogin.text.toString()
        val passwd = binding.etPasswordLogin.text.toString()

        Log.i("TEST", "Login Button Clicked")
        Log.i("TEST", "$")

        if (email.isEmpty()){
            Log.i("LOGIN_ACV", "Email is empty")
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwd.isEmpty()){
            Log.i("LOGIN_ACV", "Password is empty")
            Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, passwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FIREBASE", "signInWithEmail:success")
                    val intentToMainActvity = Intent(this, MainActivity::class.java)
                    startActivity(intentToMainActvity)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FIREBASE", "signInWithEmail:failure", task.exception)
                    Toast.makeText( baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerNowClicked() {
        val intent = Intent(applicationContext, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }


}