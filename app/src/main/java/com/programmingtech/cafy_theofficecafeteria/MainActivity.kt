package com.programmingtech.cafy_theofficecafeteria

import adapters.RecyclerFoodItemAdapter
import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import datamodels.CartItem
import datamodels.MenuItem
import de.hdodenhof.circleimageview.CircleImageView
import interfaces.MenuApi
import interfaces.RequestType
import services.DatabaseHandler
import services.FirebaseDBService

class MainActivity : AppCompatActivity(), RecyclerFoodItemAdapter.OnItemClickListener, MenuApi {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private val db = DatabaseHandler(this)

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private var allItems = ArrayList<MenuItem>()
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var recyclerFoodAdapter: RecyclerFoodItemAdapter

    private lateinit var userIcon: CircleImageView
    private lateinit var showAllSwitch: SwitchCompat

    private lateinit var topHeaderLL: LinearLayout
    private lateinit var topSearchLL: LinearLayout
    private lateinit var searchBox: SearchView
    private lateinit var foodCategoryCV: CardView
    private lateinit var showAllLL: LinearLayout

    private var empName = ""
    private var empEmail = ""
    private var empGender = "male"

    private var searchIsActive = false
    private var doubleBackToExit = false

    private lateinit var progressDialog: ProgressDialog

    override fun onBackPressed() {
        if (searchIsActive) {
            //un-hiding all the views which are above the items
            recyclerFoodAdapter.filter.filter("")
            topHeaderLL.visibility = ViewGroup.VISIBLE
            foodCategoryCV.visibility = ViewGroup.VISIBLE
            showAllLL.visibility = ViewGroup.VISIBLE
            topSearchLL.visibility = ViewGroup.GONE
            searchIsActive = false
            return
        }
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        if (doubleBackToExit) {
            super.onBackPressed()
            return
        }
        doubleBackToExit = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExit = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference

        db.clearCartTable()
        loadProfile()
        loadNavigationDrawer()
        loadMenu()
        loadSearchTask()

        userIcon = findViewById(R.id.menu_user_icon)
        userIcon.setOnClickListener {
            openUserProfileActivity()
        }
    }

    private fun loadProfile() {
        val user = auth.currentUser!!
        this.empName = user.displayName!!
        this.empEmail = user.email!!
        findViewById<TextView>(R.id.top_wish_name_tv).text = "Hi ${this.empName.split(" ")[0]}"
        Handler().postDelayed({
            findViewById<TextView>(R.id.nav_header_user_name).text = this.empName
        }, 1000)

        databaseRef.child("employees")
            .child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    empGender = snapshot.child("gender").value.toString()
                    //by default male icon is attached
                    if (empGender == "female") {
                        userIcon.setImageDrawable(resources.getDrawable(R.drawable.user_female))
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadMenu() {
        val sharedPref: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)

        itemRecyclerView = findViewById(R.id.items_recycler_view)
        recyclerFoodAdapter = RecyclerFoodItemAdapter(
            applicationContext,
            allItems,
            sharedPref.getInt("loadItemImages", 0),
            this
        )
        itemRecyclerView.adapter = recyclerFoodAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        showAllSwitch = findViewById(R.id.show_all_items_switch)
        showAllSwitch.setOnClickListener {
            if (showAllSwitch.isChecked) {
                recyclerFoodAdapter.filterList(allItems) //display complete list
                val container: LinearLayout = findViewById(R.id.food_categories_container_ll)
                for (ll in container.children) {
                    ll.alpha =
                        1.0f //change alpha value of all the category items, so it will indicate that they are not pressed
                }
            }
        }

        when (sharedPref.getInt("menuMode", 0)) {
            0 -> loadOnlineMenu()
            1 -> { // Offline
                val data = db.readOfflineMenuData()

                if (data.size == 0) { //means, offline database is not available
                    AlertDialog.Builder(this)
                        .setMessage("Offline Menu is now not available. Do you want download the menu for Offline?")
                        .setPositiveButton(
                            "Yes, Download it",
                            DialogInterface.OnClickListener { dialogInterface, _ ->
                                updateOfflineFoodMenu(true)
                                dialogInterface.dismiss()
                            })
                        .setNegativeButton(
                            "No, Continue to Online Mode",
                            DialogInterface.OnClickListener { dialogInterface, _ ->
                                loadOnlineMenu()
                                dialogInterface.dismiss()
                            })
                        .setCancelable(false)
                        .create().show()
                    return
                } else {
                    loadOfflineMenu()
                }
            }
        }
    }

    private fun loadOfflineMenu() {
        val data = db.readOfflineMenuData()

        for (i in 0 until data.size) {
            val item = MenuItem()
            item.itemID = data[i].itemID
            item.imageUrl = data[i].imageUrl
            item.itemName = data[i].itemName
            item.itemPrice = data[i].itemPrice
            item.itemShortDesc = data[i].itemShortDesc
            item.itemTag = data[i].itemTag
            item.itemStars = data[i].itemStars
            allItems.add(item)
        }
        recyclerFoodAdapter.notifyItemRangeInserted(0, allItems.size)
    }

    private fun loadOnlineMenu() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Loading Menu...")
        progressDialog.setMessage("For fast and smooth experience, you can download Menu for Offline.")
        progressDialog.create()
        progressDialog.show()

        FirebaseDBService().readAllMenu(this, RequestType.READ)
    }

    private fun loadSearchTask() {
        topHeaderLL = findViewById(R.id.main_activity_top_header_ll)
        topSearchLL = findViewById(R.id.main_activity_top_search_ll)
        searchBox = findViewById(R.id.search_menu_items)
        foodCategoryCV = findViewById(R.id.main_activity_food_categories_cv)
        showAllLL = findViewById(R.id.main_activity_show_all_ll)

        findViewById<ImageView>(R.id.main_activity_search_iv).setOnClickListener {
            //hiding all the views which are above the items
            topHeaderLL.visibility = ViewGroup.GONE
            foodCategoryCV.visibility = ViewGroup.GONE
            showAllLL.visibility = ViewGroup.GONE
            topSearchLL.visibility = ViewGroup.VISIBLE
            recyclerFoodAdapter.filterList(allItems)
            searchIsActive = true
        }

        searchBox.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                recyclerFoodAdapter.filter.filter(p0)
                return false
            }
        })
    }

    fun showTagItems(view: View) {
        //displays the items which are of same category
        val container: LinearLayout = findViewById(R.id.food_categories_container_ll)
        for (ll in container.children) {
            ll.alpha = 1.0f
        }
        (view as LinearLayout).alpha = 0.5f
        val tag = ((view as LinearLayout).getChildAt(1) as TextView).text.toString()
        val filterList = ArrayList<MenuItem>()
        for (item in allItems) {
            if (item.itemTag == tag) filterList.add(item)
        }
        recyclerFoodAdapter.filterList(filterList)
        showAllSwitch.isChecked = false
    }

    private fun loadNavigationDrawer() {
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val drawerDelay: Long = 150 //delay of the drawer to close
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_food_menu -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    Handler().postDelayed({ openUserProfileActivity() }, drawerDelay)
                }
                R.id.nav_my_orders -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    Handler().postDelayed({
                        startActivity(
                            Intent(
                                this,
                                MyCurrentOrdersActivity::class.java
                            )
                        )
                    }, drawerDelay)
                }
                R.id.nav_orders_history -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    Handler().postDelayed({
                        startActivity(
                            Intent(
                                this,
                                OrdersHistoryActivity::class.java
                            )
                        )
                    }, drawerDelay)
                }
                R.id.nav_share_app -> {
                    shareApp()
                }
                R.id.nav_report_bug -> {
                    Toast.makeText(this, "Not Available", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_contact_us -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    Handler().postDelayed({
                        startActivity(
                            Intent(
                                this,
                                ContactUsActivity::class.java
                            )
                        )
                    }, drawerDelay)
                }
                R.id.nav_update_menu -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    updateOfflineFoodMenu()
                }
                R.id.nav_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    Handler().postDelayed({
                        startActivity(
                            Intent(
                                this,
                                SettingsActivity::class.java
                            )
                        )
                    }, drawerDelay)
                }
                R.id.nav_log_out -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    logOutUser()
                }
            }
            true
        }

        findViewById<ImageView>(R.id.nav_drawer_opener_iv).setOnClickListener {
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun logOutUser() {
        // Remove all the settings
        // Remove all the order history, my orders (warn him/her to Backup the data)

        AlertDialog.Builder(this)
            .setTitle("Attention")
            .setMessage("Are you sure you want to Log Out ? You will lose all your Orders, as it is a demo App")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                Firebase.auth.signOut()

                getSharedPreferences("settings", MODE_PRIVATE).edit().clear()
                    .apply() //deleting settings from offline
                getSharedPreferences("user_profile_details", MODE_PRIVATE).edit().clear()
                    .apply() //deleting user details from offline

                //removing tables
                db.dropCurrentOrdersTable()
                db.dropOrderHistoryTable()
                db.clearSavedCards()

                startActivity(Intent(this, LoginUserActivity::class.java))
                finish()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            )
            .create().show()
    }

    private fun shareApp() {
        val message =
            "Try out this awesome App on Google Play!\nhttps://play.google.com/store/apps/details?id=$packageName"
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Share To"))
    }

    fun showBottomDialog(view: View) {
        val bottomDialog = BottomSheetSelectedItemDialog()
        val bundle = Bundle()

        var totalPrice = 0.0f
        var totalItems = 0

        for (item in db.readCartData()) {
            totalPrice += item.itemPrice
            totalItems += item.quantity
        }

        bundle.putFloat("totalPrice", totalPrice)
        bundle.putInt("totalItems", totalItems)
        // bundle.putParcelableArrayList("orderedList", recyclerFoodAdapter.getOrderedList() as ArrayList<out Parcelable?>?)

        bottomDialog.arguments = bundle
        bottomDialog.show(supportFragmentManager, "BottomSheetDialog")
    }

    private fun openUserProfileActivity() {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("gender", this.empGender)

        val options =
            ActivityOptions.makeSceneTransitionAnimation(this, userIcon, "userIconTransition")
        startActivity(intent, options.toBundle())
    }

    private fun updateOfflineFoodMenu(offlineMenuToVisible: Boolean = false) {
        db.clearTheOfflineMenuTable() // clear the older records first

        progressDialog.setTitle("Updating...")
        progressDialog.setMessage("Offline Menu is preparing for you...")
        progressDialog.show()

        FirebaseDBService().readAllMenu(this, RequestType.OFFLINE_UPDATE)
    }

    override fun onFetchSuccessListener(list: ArrayList<MenuItem>, requestType: RequestType) {

        if (requestType == RequestType.READ) {
            for (item in list) {
                allItems.add(item)
            }
            recyclerFoodAdapter.notifyItemRangeInserted(0, allItems.size)
        }

        if (requestType == RequestType.OFFLINE_UPDATE) {
            for (item in list) {
                db.insertOfflineMenuData(item)
            }
            Toast.makeText(applicationContext, "Offline Menu Updated", Toast.LENGTH_LONG).show()
            loadOfflineMenu()
        }

        progressDialog.dismiss()
    }

    override fun onItemClick(item: MenuItem) {
        //Do some stuff, when we click on an item, like Show More details of that item
    }

    override fun onPlusBtnClick(item: MenuItem) {
        item.quantity += 1
        db.insertCartItem(
            CartItem(
                itemID = item.itemID,
                itemName = item.itemName,
                imageUrl = item.imageUrl,
                itemPrice = item.itemPrice,
                quantity = item.quantity,
                itemStars = item.itemStars,
                itemShortDesc = item.itemShortDesc
            )
        )
    }

    override fun onMinusBtnClick(item: MenuItem) {
        if (item.quantity == 0) return
        item.quantity -= 1

        val cartItem = CartItem(
            itemID = item.itemID,
            itemName = item.itemName,
            imageUrl = item.imageUrl,
            itemPrice = item.itemPrice,
            quantity = item.quantity,
            itemStars = item.itemStars,
            itemShortDesc = item.itemShortDesc
        )

        if (item.quantity == 0) {
            db.deleteCartItem(cartItem)
        } else {
            db.insertCartItem(cartItem) // Update
        }
    }

}
