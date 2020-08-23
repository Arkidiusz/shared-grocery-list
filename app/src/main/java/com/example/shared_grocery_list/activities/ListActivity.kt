package com.example.shared_grocery_list.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseUser

class ListActivity : AppCompatActivity() {
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val user = intent.getParcelableExtra<FirebaseUser>("user")
        if (user == null)
            finish()
        else {
            this.user = user
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        intent = Intent(this, AddItemActivity::class.java)
        intent.putExtra("uid", user.uid)
        startActivity(intent)
        return true
    }
}