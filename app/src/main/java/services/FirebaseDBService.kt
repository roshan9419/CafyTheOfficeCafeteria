package services

import com.google.firebase.database.*
import datamodels.MenuItem
import interfaces.MenuApi
import interfaces.RequestType

class FirebaseDBService {
    private var databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val foodMenu = "food_menu"

    fun readAllMenu(menuApi: MenuApi, requestType: RequestType) {
        val menuList = ArrayList<MenuItem>()

        val menuDbRef = databaseRef.child(foodMenu)
        menuDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val item = MenuItem(
                        itemID = (1..1000).random().toString(), // TODO NEED TO UPDATE
                        imageUrl = snap.child("item_image_url").value.toString(),
                        itemName = snap.child("item_name").value.toString(),
                        itemPrice = snap.child("item_price").value.toString().toFloat(),
                        itemShortDesc = snap.child("item_desc").value.toString(),
                        itemTag = snap.child("item_category").value.toString(),
                        itemStars = snap.child("stars").value.toString().toFloat()
                    )
                    menuList.add(item)
                }
                menuList.shuffle() //so that every time user can see different items on opening app
                menuApi.onFetchSuccessListener(menuList, requestType)
            }

            override fun onCancelled(error: DatabaseError) {
                // HANDLE ERROR
            }
        })
    }

    fun insertMenuItem(item: MenuItem) {
        val menuRef = databaseRef.child(foodMenu)

        menuRef.setValue(item)
    }
}