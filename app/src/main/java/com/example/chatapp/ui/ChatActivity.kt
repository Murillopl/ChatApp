package com.example.chatapp.ui

import MessagesAdaptor
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomInset = maxOf(systemBars.bottom, imeInsets.bottom)

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                bottomInset
            )
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.chat_toolbar)
        setSupportActionBar(toolbar)

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

        messagesRef.orderBy("timeStamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
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
                                messages.add(newIndex, message)
                                messagesAdapator.notifyItemInserted(newIndex)
                                messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
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

        if (message.isNotEmpty()) {
            messagesRef.document()
                .set(ChatMessage(currentUser, message, null))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                Intent(this@ChatActivity, MainActivity::class.java).also {
                    startActivity(it)
                }
                return true
            }
        }
        return false
    }
}