package com.example.shared_grocery_list.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Activity for registering new users with email and password
 */
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_register.setOnClickListener {
            if (validateRegistrationForm())
                createAccount(et_email.text.toString(), et_password.text.toString())
        }

        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        // TODO Check if user is signed in (non-null) and update UI accordingly.
        // val currentUser = auth.currentUser
    }

    private fun validateRegistrationForm(): Boolean {
        val email = et_email.text.toString()
        val password = et_password.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "Please fill the registration form", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // TODO Sign in success, update UI with the signed-in user's information
                // val user = auth.currentUser
                Toast.makeText(baseContext, "Registration successful", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}