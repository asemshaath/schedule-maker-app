package com.example.calenderapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val textView = binding.loginNow

        textView.setOnClickListener{loginNowClicked()}

        binding.registerBtn.setOnClickListener{registerBtnClicked()}
    }

    private fun loginNowClicked() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerBtnClicked() {
        val email = binding.etEmailRegister.text.toString()
        val passwd = binding.etPasswordRegister.text.toString()
        val rePasswd = binding.etVerifyPasswd.text.toString()



        Log.i("TEST", "Register Button Clicked")

        if (email.isEmpty()){
            Log.i("REGISTER_ACV", "Email is empty")
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwd.isEmpty()){
            Log.i("REGISTER_ACV", "Password is empty")
            Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show()
            return
        } else if (!passwd.equals(rePasswd)){
            Log.i("REGISTER_ACV", "Passwords doesn't match")
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()

            return
        }

        auth.createUserWithEmailAndPassword(email, passwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FIREBASE", "createUserWithEmail:success")
                    Toast.makeText(this, "Signed Up Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FIREBASE", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext,"Authentication failed.",Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }


    }
}