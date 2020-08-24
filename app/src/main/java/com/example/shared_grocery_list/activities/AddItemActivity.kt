package com.example.shared_grocery_list.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_item.*

class AddItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // Add grocery item to the database
        btn_add_friend_menu.setOnClickListener {
            val uid = intent.getStringExtra("uid")
            if (uid != null) {
                val itemName = et_grocery_item_name.text.toString()
                if (itemName.isEmpty()) {
                    Toast.makeText(
                        this, "Please specify grocery item name.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val dbRootRef = FirebaseDatabase.getInstance().reference
                    dbRootRef.child("users").child(uid).child("items").push()
                        .setValue(itemName)
                    et_grocery_item_name.text.clear()
                }
            } else finish() //TODO inform user of what happened
        }
    }
}