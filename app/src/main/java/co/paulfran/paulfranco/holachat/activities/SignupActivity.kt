package co.paulfran.paulfranco.holachat.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import co.paulfran.paulfranco.holachat.R
import co.paulfran.paulfranco.holachat.util.DATA_USERS
import co.paulfran.paulfranco.holachat.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            startActivity(MainActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_signup)


        setTextChangeListener(nameET, nameTIL)
        setTextChangeListener(phoneET, phoneTIL)
        setTextChangeListener(emailET, emailTIL)
        setTextChangeListener(passwordET, passwordTIL)
        // when progress layout is visible intercept all clicks
        progressLayout.setOnTouchListener { v, event -> true }

    }

    private fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til.isErrorEnabled = false
            }

        })
    }



    fun onSignup(v: View) {

        var proceed = true
        if (nameET.text.isNullOrEmpty()) {
            nameTIL.error = "Name is required"
            nameTIL.isErrorEnabled = true
            proceed = false
        }

        if (phoneET.text.isNullOrEmpty()) {
            phoneTIL.error = "Phone Number is required"
            phoneTIL.isErrorEnabled = true
            proceed = false
        }

        if (emailET.text.isNullOrEmpty()) {
            emailTIL.error = "Email is Required!"
            emailTIL.isErrorEnabled = true
            proceed = false
        }

        if (passwordET.text.isNullOrEmpty()) {
            passwordTIL.error = "Password is required!"
            passwordTIL.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            progressLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString())
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            progressLayout.visibility = View.GONE
                            Toast.makeText(this@SignupActivity, "Signup error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        } else if (firebaseAuth.uid != null) {
                            val email = emailET.text.toString()
                            val phone = phoneET.text.toString()
                            val name = nameET.text.toString()
                            // user object from util/Model
                            val user = User(email, phone, name, "", "Hello I am new Here", "", "")
                            //!! means we know its not null because we tested in the if-else statement
                            firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                        }
                        progressLayout.visibility = View.GONE
                    }
                    .addOnFailureListener { e ->
                        progressLayout.visibility = View.GONE
                        e.printStackTrace()
                    }
        }
    }

    override fun onStart() {
        super.onStart()
        // when activity starts add listener
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        // when activity stops remove listener
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    fun onLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }

}
