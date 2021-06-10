package services

import com.google.firebase.database.*
import datamodels.MenuItem
import interfaces.MenuApi

class FirebaseDBService {
    private var databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun readAllMenu(menuApi: MenuApi) {
        val menuList = ArrayList<MenuItem>()

        val menuDbRef = databaseRef.child("food_menu")
        menuDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val item = MenuItem(
                        snap.child("item_image_url").value.toString(),
                        snap.child("item_name").value.toString(),
                        snap.child("item_price").value.toString().toFloat(),
                        snap.child("item_desc").value.toString(),
                        snap.child("item_category").value.toString(),
                        snap.child("stars").value.toString().toFloat()
                    )
                    menuList.add(item)
                }
                menuList.shuffle() //so that every time user can see different items on opening app
                menuApi.onFetchSuccessListener(menuList)
            }

            override fun onCancelled(error: DatabaseError) {
                // HANDLE ERROR
            }
        })
    }
}