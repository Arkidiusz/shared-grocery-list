package com.example.shared_grocery_list.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.item_grocery_item.view.*

class ListActivity : AppCompatActivity() {
    private lateinit var user: FirebaseUser
    private val listTitles: MutableList<String> = ArrayList()
    private val groceryLists: MutableList<MutableList<GroceryItem>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val user = intent.getParcelableExtra<FirebaseUser>("user")
        if (user == null) finish()
        else {
            this.user = user

            // Setup expandableListAdapter of grocery lists
            listTitles.add("Your grocery items")
            val yourGroceryItems: MutableList<GroceryItem> = ArrayList()
            groceryLists.add(yourGroceryItems)
            val expandableListAdapter = ExpandableListAdapter(this, listTitles, groceryLists)
            expandable_list_view.setAdapter(expandableListAdapter)

            // Listener for fetching user's grocery items from database
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("items")
                .addChildEventListener(object : ChildEventListener {

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val itemID = snapshot.key
                        if (itemID != null) {
                            for (groceryItem in yourGroceryItems) {
                                if (groceryItem.id == itemID) {
                                    yourGroceryItems.remove(groceryItem)
                                    expandableListAdapter.notifyDataSetChanged()
                                    break
                                }
                            }
                        }
                    }

                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val itemID = snapshot.key
                        val itemName = snapshot.value.toString()
                        if (itemID != null) {
                            val groceryItem = GroceryItem(itemID, itemName)
                            yourGroceryItems.add(groceryItem)
                            expandableListAdapter.allChildParents[itemID] = user.uid
                            expandableListAdapter.notifyDataSetChanged()
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

            // Listeners for fetching friends' grocery items from database
            FirebaseDatabase.getInstance().reference.child("users").child(user.uid).child("friends")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("onCancelled", error.message)
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (friend in snapshot.children) {
                            Log.d("friend", "${friend.key} --- ${friend.value}")
                            val friendID = friend.key
                            val friendEmail = friend.value
                            if (friendID != null && friendEmail != null) {
                                listTitles.add("$friendEmail's grocery list")
                                val friendGroceryList = ArrayList<GroceryItem>()
                                groceryLists.add(friendGroceryList)
                                FirebaseDatabase.getInstance().reference.child("users")
                                    .child(friendID).child("items")
                                    .addChildEventListener(object : ChildEventListener {
                                        override fun onCancelled(error: DatabaseError) {
                                            Log.d("onCancelled", error.message)
                                        }

                                        override fun onChildMoved(
                                            snapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {
                                        }

                                        override fun onChildChanged(
                                            snapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {
                                        }

                                        override fun onChildAdded(
                                            snapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {
                                            val itemID = snapshot.key
                                            val itemName = snapshot.value.toString()
                                            if (itemID != null) {
                                                val groceryItem = GroceryItem(itemID, itemName)
                                                friendGroceryList.add(groceryItem)
                                                expandableListAdapter.allChildParents[itemID] =
                                                    friendID
                                                expandableListAdapter.notifyDataSetChanged()
                                            }
                                        }

                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                            val itemID = snapshot.key
                                            if (itemID != null) {
                                                for (groceryItem in friendGroceryList) {
                                                    if (groceryItem.id == itemID) {
                                                        friendGroceryList.remove(groceryItem)
                                                        expandableListAdapter.notifyDataSetChanged()
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    })
                            }
                        }
                    }
                })

            // Remove selected exercises on button press
            btn_buy.setOnClickListener {
                val toBeRemoved = ArrayList<String>()
                Log.d("---------", "------------")
                for ((id, view) in expandableListAdapter.allChildViews) {
                    Log.d("childView", "${id} --- ${view.cb_grocery_item.text}")
                    if (view.cb_grocery_item.isChecked) {
                        toBeRemoved.add(id)
                    }
                }
                for (position in toBeRemoved) expandableListAdapter.removeChildFromMap(position)
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

    inner class ExpandableListAdapter(
        var context: Context,
        var header: MutableList<String>,
        var body: MutableList<MutableList<GroceryItem>>
    ) : BaseExpandableListAdapter() {
        val allChildViews = HashMap<String, View>()
        val allChildParents = HashMap<String, String>()

        override fun getGroup(groupPosition: Int): String {
            return header[groupPosition]
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            var convertViewVar = convertView
            if (convertViewVar == null) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertViewVar = inflater.inflate(R.layout.item_list_friend, null)
            }
            val title = convertViewVar!!.findViewById<TextView>(R.id.textView)
            title.text = getGroup(groupPosition)

            return convertViewVar
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return body[groupPosition].size
        }

        override fun getChild(groupPosition: Int, childPosition: Int): GroceryItem {
            return body[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            var convertViewVar = convertView
            if (convertViewVar == null) {
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertViewVar = inflater.inflate(R.layout.item_grocery_item, null)
            }
            val cbGroceryItem = convertViewVar!!.findViewById<TextView>(R.id.cb_grocery_item)
            cbGroceryItem.text = getChild(groupPosition, childPosition).name
            allChildViews[getChild(groupPosition, childPosition).id] = convertViewVar

            return convertViewVar
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return header.size
        }

        // Remove a childView from allChildViews and database
        fun removeChildFromMap(id: String) {
            allChildViews.remove(id)
            FirebaseDatabase.getInstance().reference.child("users")
                .child(allChildParents[id].toString()).child("items").child(id).removeValue()
            allChildParents.remove(id)
            // TODO danger allChildParents[id] can return null
        }
    }
}