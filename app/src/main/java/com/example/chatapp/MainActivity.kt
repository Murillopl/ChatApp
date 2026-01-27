package com.example.chatapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.signInButton.setOnClickListener {
            signIn()
        }

        mBinding.signUpButton.setOnClickListener {
            createAccount()
        }

        mBinding.textViewRegister.setOnClickListener {
            mBinding.flipper.setInAnimation(this, android.R.anim.slide_in_left)
            mBinding.flipper.setOutAnimation(this, android.R.anim.slide_out_right)
            mBinding.flipper.showNext()
        }

        mBinding.textViewSignIn.setOnClickListener {
            mBinding.flipper.setInAnimation(this, R.anim.slide_in_right)
            mBinding.flipper.setOutAnimation(this, R.anim.slide_out_left)
            mBinding.flipper.showPrevious()
        }
    }

    private fun signIn() {
        val email = mBinding.signInInputEmail.editText?.text.toString().trim()
        val password = mBinding.signInInputPassword.editText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "You should provide an email and a password", Toast.LENGTH_SHORT)
                .show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun createAccount() {
        val email = mBinding.signUpInputEmail.text?.toString()?.trim()
        val password = mBinding.signUpInputPassword.text?.toString()?.trim()
        val confirmPassword = mBinding.confirmPassword.text?.toString()?.trim()

        if (email.isNullOrEmpty() || password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
            Toast.makeText(this, "Please provide and email and a password", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(this, "Can't create account", Toast.LENGTH_SHORT).show()
                }
            }
    }
}