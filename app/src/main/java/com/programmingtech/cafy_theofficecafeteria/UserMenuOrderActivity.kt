package com.programmingtech.cafy_theofficecafeteria

import adapters.RecyclerOrderItemAdapter
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import datamodels.CartItem
import datamodels.MenuItem
import services.DatabaseHandler
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserMenuOrderActivity : AppCompatActivity(), RecyclerOrderItemAdapter.OnItemClickListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerOrderItemAdapter

    private lateinit var totalItemsTV: TextView
    private lateinit var totalPriceTV: TextView
    private lateinit var totalTaxTV: TextView
    private lateinit var subTotalTV: TextView
    private lateinit var proceedToPayBtn: Button
    private lateinit var orderTakeAwayTV: TextView

    private var totalPrice: Float = 0F
    private var totalItems: Int = 0
    private var totalTax: Float = 0F

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_alert)
            .setTitle("Alert!")
            .setMessage("Do you want to cancel your order?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener {_, _ ->
                super.onBackPressed()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener {dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_menu_order)

        totalItemsTV = findViewById(R.id.order_total_items_tv)
        totalPriceTV = findViewById(R.id.order_total_price_tv)
        totalTaxTV = findViewById(R.id.order_total_tax_tv)
        subTotalTV = findViewById(R.id.order_sub_total_tv)
        proceedToPayBtn = findViewById(R.id.proceed_to_pay_btn)
        orderTakeAwayTV = findViewById(R.id.order_take_away_time_tv)

        totalPrice = intent.getFloatExtra("totalPrice", 0F)
        totalItems = intent.getIntExtra("totalItems", 0)
        totalTax = totalPrice * 0.12F

        loadOrderDetails()
        loadRecyclerAdapter()

        val c = Calendar.getInstance()
        c.add(Calendar.MINUTE, 5)
        onTimeSet(TimePicker(this), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))

        findViewById<ImageView>(R.id.user_menu_order_help_iv).setOnClickListener {
            startActivity(Intent(this, ContactUsActivity::class.java))
        }
    }

    private fun loadOrderDetails() {
        totalItemsTV.text = "$totalItems items"
        totalPriceTV.text = "\$%.2f".format(totalPrice)
        totalTaxTV.text = "\$%.2f".format(totalTax)
        subTotalTV.text = "\$%.2f".format(totalPrice+totalTax)
        proceedToPayBtn.text = "Proceed to Pay \$%.2f".format(totalPrice+totalTax)
    }

    private fun loadRecyclerAdapter() {
        val sharedPref: SharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val orderedItems: ArrayList<CartItem> = ArrayList()

        for (item in DatabaseHandler(this).readCartData()) {
            orderedItems.add(item)
        }

        itemRecyclerView = findViewById(R.id.order_recycler_view)
        recyclerAdapter = RecyclerOrderItemAdapter(
            applicationContext,
            orderedItems,
            totalItemsTV,
            totalItems,
            totalPriceTV,
            totalPrice,
            totalTaxTV,
            totalTax,
            subTotalTV,
            proceedToPayBtn,
            sharedPref.getInt("loadItemImages", 0),
            this
        )

        itemRecyclerView.adapter = recyclerAdapter
        itemRecyclerView.layoutManager = LinearLayoutManager(this)

        recyclerAdapter.notifyItemRangeInserted(0, orderedItems.size)
    }

    fun changeOrderTakeAwayTime(view: View) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, this, hour, minute, true)
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, hourOfDay: Int, minute: Int) {
        val time = "$hourOfDay:$minute"
        val f24Hours = SimpleDateFormat("HH:mm")
        try {
            val date = f24Hours.parse(time)
            val f12Hours = SimpleDateFormat("hh:mm aa")
            orderTakeAwayTV.text = f12Hours.format(date)
        } catch (e: Exception) {}
    }

    fun goBack(view: View) {onBackPressed()}

    override fun emptyOrder() {
        AlertDialog.Builder(this)
                .setMessage("Your order is now empty. Add some items from the food menu and place the order.")
                .setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
                    onBackPressed()
                })
                .setCancelable(false)
                .create().show()
    }

    fun openPaymentActivity(view: View) {

        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("totalItemPrice", recyclerAdapter.getTotalItemPrice())
        intent.putExtra("totalTaxPrice", recyclerAdapter.getTotalTax())
        intent.putExtra("subTotalPrice", recyclerAdapter.getSubTotalPrice())
        intent.putExtra("takeAwayTime", orderTakeAwayTV.text.toString())
        startActivity(intent)

    }
}