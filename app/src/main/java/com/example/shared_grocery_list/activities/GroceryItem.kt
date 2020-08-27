package com.example.shared_grocery_list.activities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class GroceryItem(val id: String, val name: String) : Parcelable