package com.example.shared_grocery_list.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
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

            // RecyclerView of grocery items
            val recyclerAdapter = RecyclerAdapter()
            rv_your_grocery.adapter = recyclerAdapter
            rv_your_grocery.layoutManager = LinearLayoutManager(this)
            val dividerDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            val drawable = ContextCompat.getDrawable(this, R.drawable.line_divider_decoration)
            if (drawable != null) dividerDecoration.setDrawable(drawable)
            rv_your_grocery.addItemDecoration(dividerDecoration)

            // Listener for fetching grocery items from database
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("items")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val itemID = snapshot.key
                        if (itemID != null) {
                            for (groceryItem in groceryList) {
                                if (groceryItem.id == itemID) {
                                    groceryList.remove(groceryItem)
                                    recyclerAdapter.notifyDataSetChanged()
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

            // Remove selected exercises on button press
            btn_buy.setOnClickListener {
                for (viewHolder in recyclerAdapter.allViewHolders) {
                    val cbGroceryItem = viewHolder.cbGroceryItem
                    if (cbGroceryItem.isChecked) {
                        val groceryID = viewHolder.groceryID
                        Log.d("Removing value", groceryID)
                        FirebaseDatabase.getInstance().reference.child("users")
                            .child(user.uid).child("items").child(groceryID).removeValue()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val intent = when (item.itemId) {
            R.id.btn_add_grocery_menu -> Intent(this, AddItemActivity::class.java)
            R.id.btn_add_friend_menu -> Intent(this, AddFriendActivity::class.java)
            else -> null
        }
        if (intent != null) {
            intent.putExtra("uid", user.uid)
            startActivity(intent)
        }
        return true
    }

    inner class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>() {
        val allViewHolders = ArrayList<RecyclerHolder>()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerAdapter.RecyclerHolder {
            val inflater = LayoutInflater.from(parent.context)
            val recyclerHolder =
                RecyclerHolder(inflater.inflate(R.layout.item_grocery_item, parent, false))
            allViewHolders.add(recyclerHolder)
            return recyclerHolder
        }
        override fun getItemCount(): Int {
            return groceryList.size
        }

        override fun onBindViewHolder(holder: RecyclerAdapter.RecyclerHolder, position: Int) {
            val groceryItem = groceryList[position]
            val cbGroceryItem = holder.cbGroceryItem
            cbGroceryItem.text = groceryItem.name
            cbGroceryItem.isChecked = false
            holder.groceryID = groceryItem.id
        }

        inner class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cbGroceryItem: CheckBox = itemView.cb_grocery_item
            lateinit var groceryID: String // This is set in onBindViewHolder()
        }
    }
}