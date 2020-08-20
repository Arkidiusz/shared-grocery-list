package com.example.shared_grocery_list.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val user = intent.getParcelableExtra<FirebaseUser>("user")
        if (user != null) textView.text = user.email
        else textView.text = "user null"
    }
}