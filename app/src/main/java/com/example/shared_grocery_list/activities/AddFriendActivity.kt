package com.example.shared_grocery_list.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_friend.*

class AddFriendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)

        // TODO handle the case where user attempts to add himself to the list
        btn_add_friend_menu.setOnClickListener {
            val uid = intent.getStringExtra("uid")
            if (uid != null) {
                val friendEmail = et_grocery_item_name.text.toString()
                if (friendEmail.isEmpty()) {
                    Toast.makeText(
                        this, "Please specify friend's email.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    FirebaseDatabase.getInstance().reference.child("users")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (child in snapshot.children) {
                                    val snapshotEmail = child.child("email").value
                                    val snapshotID = child.key
                                    if (snapshotEmail == friendEmail && snapshotID != null) {
                                        FirebaseDatabase.getInstance().reference.child("users")
                                            .child(uid)
                                            .child("friends").child(snapshotID)
                                            .setValue(friendEmail)
                                        Toast.makeText(
                                            applicationContext,
                                            "$snapshotEmail added to friends list",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        break
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                //TODO handle this somehow
                            }
                        })
                }
            } else finish() //TODO inform user of what happened
        }
    }
}