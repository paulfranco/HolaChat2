package co.paulfran.paulfranco.holachat.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import co.paulfran.paulfranco.holachat.R
import co.paulfran.paulfranco.holachat.adapters.ConversationAdapter
import co.paulfran.paulfranco.holachat.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        chatId = intent.extras.getString(PARAM_CHAT_ID)
        imageUrl = intent.extras.getString(PARAM_IMAGE_URL)
        chatName = intent.extras.getString(PARAM_CHAT_NAME)
        otherUserId = intent.extras.getString(PARAM_OTHER_USER_ID)

        if (chatId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chat Error", Toast.LENGTH_SHORT).show()
            finish()
        }

        topNameTV.text = chatName
        populateImage(this, imageUrl, topPhotoIV, R.drawable.default_user)

        messagesRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
    /*
        DUMMY DATA
        conversationAdapter.addMessage(Message(userId, "hello", 2))
        conversationAdapter.addMessage(Message("dsdsdd", "hey you", 3))

        conversationAdapter.addMessage(Message(userId, "hello", 4))
        conversationAdapter.addMessage(Message("dsdsdd", "hey you", 5))

        conversationAdapter.addMessage(Message(userId, "hello", 6))
        conversationAdapter.addMessage(Message("dsdsdd", "hey you", 7))
    */
        firebaseDB.collection(DATA_CHATS)
                .document(chatId!!)
                .collection(DATA_CHAT_MESSAGES)
                .orderBy(DATA_CHAT_MESSAGE_TIME)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        firebaseFirestoreException.printStackTrace()
                        return@addSnapshotListener
                    } else {
                        if (querySnapshot != null) {
                            for (change in querySnapshot.documentChanges) {
                                when(change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        val message = change.document.toObject(Message::class.java)
                                        if (message != null) {
                                            conversationAdapter.addMessage(message)
                                            messagesRV.post {
                                                messagesRV.smoothScrollToPosition(conversationAdapter.itemCount - 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                }
    }



    fun onSend(v: View) {

        if (!messageET.text.isNullOrEmpty()) {
            val message = Message(userId, messageET.text.toString(), System.currentTimeMillis())
            firebaseDB.collection(DATA_CHATS)
                    .document(chatId!!)
                    .collection(DATA_CHAT_MESSAGES)
                    .document()
                    .set(message)
            messageET.setText("", TextView.BufferType.EDITABLE)
        }

    }

    companion object {

        private val PARAM_CHAT_ID = "Chat Id"
        private val PARAM_IMAGE_URL = "Image Url"
        private val PARAM_OTHER_USER_ID = "Other user id"
        private val PARAM_CHAT_NAME = "Chat name"

        fun newIntent(context: Context?, chatId: String?, imageUrl: String?, otherUserId: String?, chatName: String?): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID, chatId)
            intent.putExtra(PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID, otherUserId)
            intent.putExtra(PARAM_CHAT_NAME, chatName)
            return intent
        }
    }


}
