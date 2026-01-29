package com.example.chatapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.model.User
import com.example.chatapp.ui.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imagePicker: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users_collection")

    private val STORAGE_REQUEST_CODE = 55555

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupImagePicker()
        setupClicks()
    }

    private fun setupClicks() {
        binding.signInButton.setOnClickListener { signIn() }
        binding.signUpButton.setOnClickListener { createAccount() }

        binding.profileImage.setOnClickListener { checkPermissionAndPickImage() }

        binding.textViewRegister.setOnClickListener { showNext() }
        binding.textViewSignIn.setOnClickListener { showPrevious() }
        binding.textViewSignUp.setOnClickListener { showPrevious() }
        binding.textViewProfilePic.setOnClickListener { showNext() }
    }

    // ---------------- IMAGE PICKER ----------------

    private fun setupImagePicker() {
        imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                imageUri = it.data?.data
                binding.profileImage.setImageURI(imageUri)
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePicker.launch(intent)
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("This permission is needed to select a profile picture")
                .setPositiveButton("Allow") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                        STORAGE_REQUEST_CODE
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            pickImage()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- AUTH ----------------

    private fun signIn() {

        binding.progressBar1.visibility = View.VISIBLE

        val email = binding.signInInputEmail.editText?.text.toString().trim()
        val password = binding.signInInputPassword.editText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                binding.progressBar1.visibility = View.INVISIBLE
                sendToChat()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message ?: "Login failed", Toast.LENGTH_LONG).show()
                binding.progressBar1.visibility = View.INVISIBLE
            }
    }

    private fun createAccount() {

        binding.progressBar2.visibility = View.VISIBLE

        val email = binding.signUpInputEmail.text.toString().trim()
        val password = binding.signUpInputPassword.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()
        val userName = binding.signUpInputUsername.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || userName.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    val user = User(userName = userName, uid = uid)

                    usersRef.document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            binding.progressBar2.visibility = View.INVISIBLE
                            sendToChat()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Firestore error", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    Toast.makeText(this, task.exception?.message ?: "Auth error", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ---------------- NAVIGATION ----------------

    private fun sendToChat() {
        startActivity(Intent(this, ChatActivity::class.java))
        finish()
    }

    // ---------------- ANIMATIONS ----------------

    private fun showNext() {
        binding.flipper.setInAnimation(this, android.R.anim.slide_in_left)
        binding.flipper.setOutAnimation(this, android.R.anim.slide_out_right)
        binding.flipper.showNext()
    }

    private fun showPrevious() {
        binding.flipper.setInAnimation(this, R.anim.slide_in_right)
        binding.flipper.setOutAnimation(this, R.anim.slide_out_left)
        binding.flipper.showPrevious()
    }
}
