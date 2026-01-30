package com.example.chatapp.ui

import MessagesAdaptor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users_collection")
    private val messagesRef: CollectionReference = db.collection("messages_collection")
    private lateinit var sendButton: Button
    private lateinit var editTextMessage: EditText
    private lateinit var messagesAdapator: MessagesAdaptor
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messages: MutableList<ChatMessage>
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        messagesRecyclerView = findViewById(R.id.message_recycler_view)
        sendButton = findViewById(R.id.send_message_button)
        editTextMessage = findViewById(R.id.input_message)

        initRecyclerView()
        getCurrentUser()

        sendButton.setOnClickListener {
            insertMessage()
        }

    }

    override fun onStart() {
        super.onStart()

        messagesRef.addSnapshotListener { snapshots, error ->
            error?.let {
                return@addSnapshotListener
            }

            snapshots?.let {
                for (dc in it.documentChanges) {
                    val oldIndex = dc.oldIndex
                    val newIndex = dc.newIndex

                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val snapshot = dc.document
                            val message = snapshot.toObject(ChatMessage::class.java)
                            messages.add(message)
                            messagesAdapator.notifyItemInserted(newIndex)
                        }

                        DocumentChange.Type.REMOVED -> {

                        }

                        DocumentChange.Type.MODIFIED -> {

                        }
                    }

                }

            }
        }
    }

    private fun initRecyclerView() {
        messages = mutableListOf()
        messagesAdapator = MessagesAdaptor(this, messages)
        messagesRecyclerView.adapter = messagesAdapator
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.setHasFixedSize(true)
    }

    private fun getCurrentUser() {
        usersRef.whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .addOnSuccessListener {
                for (snapshot in it) {
                    currentUser = snapshot.toObject(User::class.java)
                }
            }
    }

    private fun insertMessage() {
        val message = editTextMessage.text.toString().trim()

        if ( message.isNotEmpty()) {
            messagesRef.document()
                .set(ChatMessage(currentUser, message))
        }
    }
}