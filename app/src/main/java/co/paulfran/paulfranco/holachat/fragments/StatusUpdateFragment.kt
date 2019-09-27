package co.paulfran.paulfranco.holachat.fragments


import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import co.paulfran.paulfranco.holachat.R
import co.paulfran.paulfranco.holachat.activities.MainActivity
import co.paulfran.paulfranco.holachat.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_status_update.*


class StatusUpdateFragment : Fragment() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val userID = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_status_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressLayout.setOnTouchListener { v, event -> true }
        sendStatusButton.setOnClickListener { onUpdate() }
        populateImage(context, imageUrl, statusIV)

        statusLayout.setOnClickListener {
            if(isAdded) {
                (activity as MainActivity).startNewActivity(REQUEST_CODE_PHOTO)
            }
        }
    }

    fun onUpdate() {
        progressLayout.visibility = View.VISIBLE
        val map = HashMap<String, Any>()
        map[DATA_USER_STATUS] = statusET.text.toString()
        map[DATA_USER_STATUS_URL] = imageUrl
        map[DATA_USER_STATUS_TIME] = getTime()

        firebaseDB.collection(DATA_USERS)
                .document(userID!!)
                .update(map)
                .addOnSuccessListener {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(activity, "Status Updated...", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    progressLayout.visibility = View.GONE
                    Toast.makeText(activity, "Status Update Failed...", Toast.LENGTH_SHORT).show()

                }

    }

    fun storeImage(imageUri: Uri?) {
        if(imageUri != null && userID != null) {
            Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show()
            progressLayout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child("${userID}_status")

            filePath.putFile(imageUri)
                    .addOnSuccessListener {
                        filePath.downloadUrl
                                .addOnSuccessListener {taskSnapshot ->
                                    val url = taskSnapshot.toString()
                                    firebaseDB.collection(DATA_USERS)
                                            .document(userID)
                                            .update(DATA_USER_STATUS, url)
                                            .addOnSuccessListener {
                                                imageUrl = url
                                                populateImage(context, imageUrl, statusIV)
                                            }
                                    progressLayout.visibility = View.GONE

                                }
                                .addOnFailureListener { onUploadFailure() }
                    }
                    .addOnFailureListener { onUploadFailure() }
        }
    }

    fun onUploadFailure() {
        Toast.makeText(activity, "Failed To Upload Picture...", Toast.LENGTH_SHORT).show()
        progressLayout.visibility = View.GONE
    }


}
