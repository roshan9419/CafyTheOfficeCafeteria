package com.programmingtech.cafy_theofficecafeteria

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var loadItemImageLL: LinearLayout
    private lateinit var loadItemImageTV: TextView

    private lateinit var menuModeLL: LinearLayout
    private lateinit var menuModeTV: TextView

    private lateinit var updateMenuLL: LinearLayout
    private lateinit var deleteMenuLL: LinearLayout

    private lateinit var deleteSavedCardsLL: LinearLayout
    private lateinit var deleteOrdersHistoryLL: LinearLayout

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        loadItemImageLL = findViewById(R.id.settings_load_item_images_ll)
        loadItemImageTV = findViewById(R.id.settings_load_item_images_tv)
        loadItemImageLL.setOnClickListener { updateLoadItemImage() }

        menuModeLL = findViewById(R.id.settings_menu_mode_ll)
        menuModeTV = findViewById(R.id.settings_menu_mode_tv)
        menuModeLL.setOnClickListener { updateMenuMode() }

        updateMenuLL = findViewById(R.id.settings_update_menu_ll)
        updateMenuLL.setOnClickListener { updateMenuForOffline() }

        deleteMenuLL = findViewById(R.id.settings_delete_menu_ll)
        deleteMenuLL.setOnClickListener { deleteOfflineMenu() }

        deleteSavedCardsLL = findViewById(R.id.settings_saved_cards_ll)
        deleteSavedCardsLL.setOnClickListener { deleteAllTheSavedCards() }

        deleteOrdersHistoryLL = findViewById(R.id.settings_delete_order_history_ll)
        deleteOrdersHistoryLL.setOnClickListener { deleteAllTheOrdersHistoryDetails() }

        sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

        loadUserSettings()
        findViewById<ImageView>(R.id.settings_go_back_iv).setOnClickListener { onBackPressed() }
    }

    private fun deleteAllTheOrdersHistoryDetails() {
        val db = DatabaseHandler(this)
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete all the previous order details?")
            .setPositiveButton("Yes, Delete All", DialogInterface.OnClickListener {dialogInterface, _ ->
                db.dropOrderHistoryTable()
                db.close()
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun deleteAllTheSavedCards() {
        val db = DatabaseHandler(this)
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete all the saved cards?")
            .setPositiveButton("Yes, Delete All", DialogInterface.OnClickListener {dialogInterface, _ ->
                db.clearSavedCards()
                db.close()
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun updateLoadItemImage() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Load Item Images")

        val options = arrayOf("On", "Off")
        var checkedItem = sharedPref.getInt("loadItemImages", 0)
        dialog.setSingleChoiceItems(options, checkedItem, DialogInterface.OnClickListener {_, i ->
            checkedItem = i
        })
        dialog.setPositiveButton("Save", DialogInterface.OnClickListener {dialogInterface, _ ->
            when(checkedItem) {
                0 -> loadItemImageTV.text = "On"
                1 -> loadItemImageTV.text = "Off"
            }
            val editor = sharedPref.edit()
            editor.putInt("loadItemImages", checkedItem)
            editor.apply()
            dialogInterface.dismiss()
        })
        dialog.setCancelable(false)
        dialog.create()
        dialog.show()
    }

    private fun updateMenuMode() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Menu Mode")

        val options = arrayOf("Online", "Offline")
        var checkedItem = sharedPref.getInt("menuMode", 0)
        dialog.setSingleChoiceItems(options, checkedItem, DialogInterface.OnClickListener {_, i ->
            checkedItem = i
        })
        dialog.setPositiveButton("Save", DialogInterface.OnClickListener {dialogInterface, _ ->
            when(checkedItem) {
                0 -> menuModeTV.text = "Online"
                1 -> menuModeTV.text = "Offline"
            }
            val editor = sharedPref.edit()
            editor.putInt("menuMode", checkedItem)
            editor.apply()
            dialogInterface.dismiss()
        })
        dialog.setCancelable(false)
        dialog.create()
        dialog.show()
    }

    private fun updateMenuForOffline() {
        val db = DatabaseHandler(this)
        db.clearTheOfflineMenuTable()

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Updating...")
        progressDialog.setMessage("Offline Menu is preparing for you...")
        progressDialog.show()

        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val menuDbRef = databaseRef.child("food_menu")
        menuDbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children) {
                    val item = MenuItem(
                        snap.child("item_image_url").value.toString(),
                        snap.child("item_name").value.toString(),
                        snap.child("item_price").value.toString().toFloat(),
                        snap.child("item_desc").value.toString(),
                        snap.child("item_category").value.toString(),
                        snap.child("stars").value.toString().toFloat()
                    )
                    db.insertOfflineMenuData(item)
                }
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Offline Menu Updated", Toast.LENGTH_LONG).show()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Something Happened\n$error", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteOfflineMenu() {
        val db = DatabaseHandler(this)
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete the offline menu?")
            .setPositiveButton("Yes, Delete it", DialogInterface.OnClickListener {dialogInterface, _ ->
                db.clearTheOfflineMenuTable()
                db.close()
                Toast.makeText(this, "Offline Menu has been removed", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun loadUserSettings() {
        when(sharedPref.getInt("loadItemImages", 0)) {
            0 -> loadItemImageTV.text = "On"
            1 -> loadItemImageTV.text = "Off"
        }
        when(sharedPref.getInt("menuMode", 0)) {
            0 -> menuModeTV.text = "Online"
            1 -> menuModeTV.text = "Offline"
        }
    }
}