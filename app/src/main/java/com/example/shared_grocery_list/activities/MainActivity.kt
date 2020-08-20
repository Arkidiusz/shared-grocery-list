package com.example.shared_grocery_list.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Activity for registering new users with email and password
 */
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var registering = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_register_or_login.setOnClickListener {
            if (validateRegistrationForm()) {
                val email = et_email.text.toString()
                val password = et_password.text.toString()
                if (registering) createAccount(email, password)
                else signIn(email, password)
            }
        }

        tv_register_or_login.setOnClickListener {
            if (registering) {
                // switch to logging in
                btn_register_or_login.text = getString(R.string.main_login)
                tv_register_or_login.setText(R.string.main_no_account)
                registering = false
            } else {
                // switch to registering
                btn_register_or_login.text = getString(R.string.main_register)
                tv_register_or_login.setText(R.string.main_already_registered)
                registering = true
            }

        }

        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        val user = auth.currentUser
//        if(user != null) startListActivity(auth.currentUser)
    }

    private fun validateRegistrationForm(): Boolean {
        val email = et_email.text.toString()
        val password = et_password.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                baseContext,
                "You need to fill the registration form.",
                Toast.LENGTH_LONG
            )
                .show()
            return false
        }
        return true
    }

    private fun startListActivity(user: FirebaseUser?) {
        // TODO possible issue with null user
        intent = Intent(this, ListActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    startListActivity(user)
                } else {
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT)
                    .show()
                startListActivity(auth.currentUser)
            } else {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}