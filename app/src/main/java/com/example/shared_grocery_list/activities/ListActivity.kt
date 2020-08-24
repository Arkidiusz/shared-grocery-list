package com.example.shared_grocery_list.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.item_grocery_item.view.*

class ListActivity : AppCompatActivity() {
    private lateinit var user: FirebaseUser
    val groceryList = ArrayList<GroceryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val user = intent.getParcelableExtra<FirebaseUser>("user")
        if (user == null)
            finish()
        else {
            this.user = user

            val recyclerAdapter = RecyclerAdapter()
            rv_your_grocery.adapter = recyclerAdapter
            rv_your_grocery.layoutManager = LinearLayoutManager(this)

            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("items")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val itemID = snapshot.key
                        val itemName = snapshot.value.toString()
                        if (itemID != null) {
                            for (groceryItem in groceryList) {
                                if (groceryItem.id == itemID) {
                                    groceryList.remove(groceryItem)
                                    break
                                }
                            }
                            Log.d("Child Removed", "${snapshot.key} | ${snapshot.value}")
                        }
                    }

                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val itemID = snapshot.key
                        val itemName = snapshot.value.toString()
                        if (itemID != null) {
                            val groceryItem = GroceryItem(itemID, itemName)
                            groceryList.add(groceryItem)
                            recyclerAdapter.notifyDataSetChanged()
                            Log.d("Child Added", "${snapshot.key} | ${snapshot.value}")
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Child canceled", error.message)
                    }
                })

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

    inner class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerAdapter.RecyclerHolder {
            val inflater = LayoutInflater.from(parent.context)
            return RecyclerHolder(inflater.inflate(R.layout.item_grocery_item, parent, false))
        }

        override fun getItemCount(): Int {
            return groceryList.size
        }

        override fun onBindViewHolder(holder: RecyclerAdapter.RecyclerHolder, position: Int) {
            holder.cbGroceryItem.text = groceryList[position].name
        }

        inner class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cbGroceryItem: CheckBox = itemView.cb_grocery_item
        }
    }
}