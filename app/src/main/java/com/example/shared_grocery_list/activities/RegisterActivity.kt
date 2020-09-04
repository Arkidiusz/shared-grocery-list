package com.example.shared_grocery_list.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener {
            if (validateRegistrationForm()) {
                val email = et_register_email.text.toString()
                val password = et_register_password.text.toString()
                createAccount(email, password)
            }
        }

        tv_register.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        auth = Firebase.auth
    }

    private fun validateRegistrationForm(): Boolean {
        val nickname = et_nickname.text.toString()
        val email = et_register_email.text.toString()
        val password = et_register_password.text.toString()
        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(
                baseContext,
                "You need to fill the registration form.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                var user = auth.currentUser
                while (user == null) {
                    user = auth.currentUser
                }
                FirebaseDatabase.getInstance().reference.child("users/${user.uid}/email/")
                    .setValue(email)
                FirebaseDatabase.getInstance().reference.child("users/${user.uid}/nickname/")
                    .setValue(et_nickname.text.toString())
                Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT)
                    .show()
                startListActivity(user)
            } else {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun startListActivity(user: FirebaseUser?) {
        // TODO possible issue with null user
        intent = Intent(this, ListActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }
}