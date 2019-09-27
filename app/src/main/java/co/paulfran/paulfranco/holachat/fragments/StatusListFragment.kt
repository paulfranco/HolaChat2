package co.paulfran.paulfranco.holachat.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.paulfran.paulfranco.holachat.R
import co.paulfran.paulfranco.holachat.activities.StatusActivity
import co.paulfran.paulfranco.holachat.adapters.StatusListAdapter
import co.paulfran.paulfranco.holachat.listeners.StatusItemClickListener
import co.paulfran.paulfranco.holachat.util.DATA_USERS
import co.paulfran.paulfranco.holachat.util.DATA_USER_CHATS
import co.paulfran.paulfranco.holachat.util.StatusListElement
import co.paulfran.paulfranco.holachat.util.User
import com.google.api.Distribution
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_status_list.*

class StatusListFragment : Fragment(), StatusItemClickListener {


    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onItemClicked(statusElement: StatusListElement) {

        startActivity(StatusActivity.getIntent(context, statusElement))

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusListAdapter.setOnItemClickListener(this)
        statusListRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = statusListAdapter
            addItemDecoration(DividerItemDecoration(this@StatusListFragment.context, DividerItemDecoration.VERTICAL))

        }
    }

    fun onVisible() {
        statusListAdapter.onRefresh()
        refreshList()
    }

    fun refreshList() {
        firebaseDb.collection(DATA_USERS)
                .document(userId!!)
                .get()
                .addOnSuccessListener { doc ->
                    if(doc.contains(DATA_USER_CHATS)) {
                        val partners = doc[DATA_USER_CHATS]
                        for (partner in (partners as HashMap<String, String>).keys) {
                            firebaseDb.collection(DATA_USERS)
                                    .document(partner)
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val partner = documentSnapshot.toObject(User::class.java)
                                        if (partner != null) {
                                            if(!partner.status.isNullOrEmpty() || !partner.statusUrl.isNullOrEmpty()) {
                                                val newElement = StatusListElement(partner.name, partner.imageUrl, partner.status, partner.statusUrl, partner.statusTime)
                                                statusListAdapter.addElement(newElement)
                                            }
                                        }
                                    }
                        }
                    }
                }

    }


}
