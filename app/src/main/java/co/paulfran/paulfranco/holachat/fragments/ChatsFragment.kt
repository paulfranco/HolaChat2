package co.paulfran.paulfranco.holachat.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import co.paulfran.paulfranco.holachat.R
import co.paulfran.paulfranco.holachat.adapters.ChatsAdapter
import co.paulfran.paulfranco.holachat.listeners.ChatClickListener
import co.paulfran.paulfranco.holachat.listeners.FailureCallback
import co.paulfran.paulfranco.holachat.util.Chat
import co.paulfran.paulfranco.holachat.util.DATA_CHATS
import co.paulfran.paulfranco.holachat.util.DATA_USERS
import co.paulfran.paulfranco.holachat.util.DATA_USER_CHATS
import com.google.api.Distribution
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chats.*


class ChatsFragment : Fragment(), ChatClickListener {

    private var chatsAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureCallback: FailureCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (userId.isNullOrEmpty()) {
            failureCallback?.onUserError()
        }
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsAdapter.setOnItemClickListener(this)

        chatsRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        firebaseDB.collection(DATA_USERS).document(userId!!).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                refreshChats()
            }
        }

    }

    private fun refreshChats() {
        firebaseDB.collection(DATA_USERS)
                .document(userId!!)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.contains(DATA_USER_CHATS)) {
                        val partners = documentSnapshot[DATA_USER_CHATS]
                        val chats = arrayListOf<String>()
                        for (partner in (partners as HashMap<String, String>).keys) {
                            if (partners[partner] != null) {
                                chats.add(partners[partner]!!)
                            }
                        }
                        chatsAdapter.updateChats(chats)
                    }
                }
    }

    fun newChat(partnerId: String) {
        firebaseDB.collection(DATA_USERS)
                .document(userId!!)
                .get()
                .addOnSuccessListener {userDocument ->
                    val userChatPartners = hashMapOf<String, String>()
                    if (userDocument[DATA_USER_CHATS] != null && userDocument[DATA_USER_CHATS] is HashMap<*,*>) {
                        val userDocumentMap = userDocument[DATA_USER_CHATS] as HashMap<String, String>
                        if (userDocumentMap.containsKey(partnerId)){
                            return@addOnSuccessListener
                        } else {
                            userChatPartners.putAll(userDocumentMap)
                        }
                    }
                    firebaseDB.collection(DATA_USERS)
                            .document(partnerId)
                            .get()
                            .addOnSuccessListener { partnerDocument ->
                                val partnerChatPartners = hashMapOf<String, String>()
                                if (partnerDocument[DATA_USER_CHATS] != null && partnerDocument[DATA_USER_CHATS] is HashMap<*, *>) {
                                    val partnerDocumentMap = partnerDocument[DATA_USER_CHATS] as HashMap<String, String>
                                    partnerChatPartners.putAll(partnerDocumentMap)
                                }
                                val chatParticipants = arrayListOf(userId, partnerId)
                                val chat = Chat(chatParticipants)
                                val chatRef = firebaseDB.collection(DATA_CHATS).document()
                                val userRef = firebaseDB.collection(DATA_USERS).document(userId)
                                val partnerRef = firebaseDB.collection(DATA_USERS).document(partnerId)

                                userChatPartners[partnerId] = chatRef.id
                                partnerChatPartners[userId] =chatRef.id

                                val batch = firebaseDB.batch()
                                batch.set(chatRef, chat)
                                batch.update(userRef, DATA_USER_CHATS, userChatPartners)
                                batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                                batch.commit()


                            }
                            .addOnFailureListener{e ->
                                e.printStackTrace()
                            }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
    }

    override fun onChatClicked(name: String?, otheruserId: String?, chatImageUrl: String?, chatName: String?) {
        Toast.makeText(context, "$name clicked", Toast.LENGTH_SHORT).show()
    }

}
