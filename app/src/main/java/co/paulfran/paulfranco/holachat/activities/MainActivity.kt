package co.paulfran.paulfranco.holachat.activities

import android.Manifest.permission.READ_CONTACTS
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import co.paulfran.paulfranco.holachat.R
import co.paulfran.paulfranco.holachat.fragments.ChatsFragment
import co.paulfran.paulfranco.holachat.fragments.StatusFragment
import co.paulfran.paulfranco.holachat.fragments.StatusUpdateFragment
import co.paulfran.paulfranco.holachat.listeners.FailureCallback
import co.paulfran.paulfranco.holachat.util.DATA_USERS
import co.paulfran.paulfranco.holachat.util.DATA_USER_PHONE
import co.paulfran.paulfranco.holachat.util.PERMISSIONS_REQUEST_READ_CONTACTS
import co.paulfran.paulfranco.holachat.util.REQUEST_NEW_CHAT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), FailureCallback {


    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private val chatsFragment = ChatsFragment()
    private val statusUpdateFragment = StatusUpdateFragment()
    private val statusFragment = StatusFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatsFragment.setFailureCallbackListener(this)

        setSupportActionBar(toolbar)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTabs()
        tabs.getTabAt(1)?.select()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {fab.hide()}
                    1 -> {fab.show()}
                    2 -> {fab.hide()}
                }
            }

        })



//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

    }

    override fun onUserError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }


    fun resizeTabs() {
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    fun onNewChat(v: View) {
        if(ContextCompat.checkSelfPermission(this, READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // permission not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
                AlertDialog.Builder(this)
                        .setTitle("Contacts Permission")
                        .setMessage("This app requires access to your contacts to initiate a conversation")
                        .setPositiveButton("Ask Me") { dialog, which -> requestContactsPermission() }
                        .setNegativeButton("No") { dialog, which -> }
                        .show()
            } else {
                requestContactsPermission()
            }
        } else {
            // permission granted
            startNewActivity()
        }
    }

    fun startNewActivity() {
        startActivityForResult(ContactsActivity.newIntent(this), REQUEST_NEW_CHAT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NEW_CHAT -> {
                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
            firebaseDB.collection(DATA_USERS)
                    .whereEqualTo(DATA_USER_PHONE, phone)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.documents.size > 0) {
                            chatsFragment.newChat(result.documents[0].id)
                        } else {
                            AlertDialog.Builder(this)
                                    .setTitle("User Not Found")
                                    .setMessage("$name does not have an account. Send them an SMS to install this app")
                                    .setPositiveButton("Ok") { dialog, which ->
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse("sms:$phone")
                                        intent.putExtra("sms body", "Hi. I am using this new cool chat app. You should install it to so we can chat there")
                                        startActivity(intent)
                                    }
                                    .setNegativeButton("cancel", null)
                                    .show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "An Error occurred. Please try again later", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
        }
    }

    fun requestContactsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startNewActivity()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_profile -> onProfile()
            R.id.action_logout -> onLogout()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    private fun onProfile() {
        startActivity(ProfileActivity.newIntent(this))
    }



    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            return when(position) {
                0 -> statusUpdateFragment
                1 -> chatsFragment
                2 -> statusFragment
                else -> statusFragment
            }
        }

        override fun getCount(): Int {

            return 3
        }
    }



    companion object {
        val PARAM_NAME = "Param name"
        val PARAM_PHONE = "Param phone"
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
