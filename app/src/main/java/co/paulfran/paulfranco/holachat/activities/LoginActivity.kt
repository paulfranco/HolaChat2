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

import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_login)

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

    fun onLogin(v: View) {
        var proceed = true
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
            // show spinner
            progressLayout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(emailET.text.toString(), passwordET.text.toString())
                    .addOnCompleteListener { task ->
                        if(!task.isSuccessful) {
                            progressLayout.visibility = View.GONE
                            Toast.makeText(this@LoginActivity, "Login error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
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

    fun onSignup(v: View) {
        startActivity(SignupActivity.newIntent(this))
        finish()
    }


    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}
