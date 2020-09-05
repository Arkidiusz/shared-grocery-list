package com.example.shared_grocery_list.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.shared_grocery_list.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.item_grocery_item.view.*

/**
 * Activity for displaying user's and his friends' shopping lists
 */
class ListActivity : AppCompatActivity() {
    private lateinit var user: FirebaseUser
    private val listTitles: MutableList<String> = ArrayList()
    private val groceryLists: MutableList<MutableList<GroceryItem>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Authenticate user from intent information
        val user = intent.getParcelableExtra<FirebaseUser>("user")
        if (user == null) finish()
        else {
            this.user = user

            // Setup expandableListAdapter of grocery lists
            // Initiate it with user's list
            listTitles.add("Your list")
            val yourGroceryItems: MutableList<GroceryItem> = ArrayList()
            groceryLists.add(yourGroceryItems)
            val expandableListAdapter = ExpandableListAdapter(this, listTitles, groceryLists)
            expandable_list_view.setAdapter(expandableListAdapter)

            // Listener for fetching user's grocery items from database
            FirebaseDatabase.getInstance().reference.child("users/${user.uid}/items")
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

                    // no changes expected and movement expected
                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {
                        // Do nothing
                    }
                })

            // Listeners for fetching friends' grocery items from database
            FirebaseDatabase.getInstance().reference.child("users/${user.uid}/friends")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (friend in snapshot.children) {
                            val friendID = friend.key
                            val friendEmail = friend.value
                            if (friendID != null && friendEmail != null) {

                                // Fetch friend's nickname and add his list to all lists
                                FirebaseDatabase.getInstance().reference.child("users/${friendID}/nickname")
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {
                                            // Do nothing
                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val friendNickname = snapshot.value.toString()

                                            // Check if list is not already displayed
                                            var friendListExists = false
                                            for (title in listTitles) {
                                                if (title == "${friendNickname}'s list") friendListExists =
                                                    true
                                            }
                                            if (!friendListExists) {
                                                listTitles.add("$friendNickname's list")
                                                // Fetch friend's items
                                                val friendGroceryList = ArrayList<GroceryItem>()
                                                groceryLists.add(friendGroceryList)
                                                FirebaseDatabase.getInstance().reference.child("users/${friendID}/items")
                                                    .addChildEventListener(object :
                                                        ChildEventListener {

                                                        override fun onChildAdded(
                                                            snapshot: DataSnapshot,
                                                            previousChildName: String?
                                                        ) {
                                                            val itemID = snapshot.key
                                                            val itemName = snapshot.value.toString()
                                                            if (itemID != null) {
                                                                val groceryItem =
                                                                    GroceryItem(itemID, itemName)
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
                                                                        friendGroceryList.remove(
                                                                            groceryItem
                                                                        )
                                                                        expandableListAdapter.notifyDataSetChanged()
                                                                        break
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                            // Do nothing
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
                                                    })
                                            }
                                        }
                                    })
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Do nothing
                    }
                })

            // Remove selected shopping items on button press
            btn_buy.setOnClickListener {
                val toBeRemoved = ArrayList<String>()
                for ((id, view) in expandableListAdapter.allChildViews) {
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
            val cbGroceryItem = convertViewVar!!.findViewById<CheckBox>(R.id.cb_grocery_item)
            cbGroceryItem.text = getChild(groupPosition, childPosition).name
            cbGroceryItem.isChecked = false
            allChildViews[getChild(groupPosition, childPosition).id] = convertViewVar

            return convertViewVar
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return header.size
        }

        // Remove a childView from allChildViews and the corresponding shopping item in the database
        fun removeChildFromMap(id: String) {
            allChildViews.remove(id)
            FirebaseDatabase.getInstance().reference.child("users/${allChildParents[id].toString()}/items/${id}")
                .removeValue()
            allChildParents.remove(id)
        }
    }
}