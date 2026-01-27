package com.example.chatapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityMainBinding

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

        }

        mBinding.signUpButton.setOnClickListener {

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
}