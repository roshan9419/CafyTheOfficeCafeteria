package com.programmingtech.cafy_theofficecafeteria

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import datamodels.CurrentOrderItem
import datamodels.OrderHistoryItem
import services.DatabaseHandler
import java.text.SimpleDateFormat
import java.util.*

class OrderDoneActivity : AppCompatActivity() {

    private lateinit var completeLL: LinearLayout
    private lateinit var processingLL: LinearLayout

    private lateinit var orderStatusTV: TextView
    private lateinit var orderIDTV: TextView
    private lateinit var dateAndTimeTV: TextView

    private var totalItemPrice = 0.0F
    private var totalTaxPrice = 0.0F
    private var subTotalPrice = 0.0F
    private var paymentMethod = ""
    private var takeAwayTime = ""

    private var orderID = ""
    private var orderDate = ""

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_done)

        completeLL = findViewById(R.id.order_done_complete_ll)
        processingLL = findViewById(R.id.order_done_processing_ll)

        orderStatusTV = findViewById(R.id.order_done_order_status_tv)
        orderIDTV = findViewById(R.id.order_done_order_id_tv)
        dateAndTimeTV = findViewById(R.id.order_done_date_and_time_tv)

        totalItemPrice = intent.getFloatExtra("totalItemPrice", 0.0F)
        totalTaxPrice = intent.getFloatExtra("totalTaxPrice", 0.0F)
        subTotalPrice = intent.getFloatExtra("subTotalPrice", 0.0F)

        paymentMethod = intent?.getStringExtra("paymentMethod").toString()
        takeAwayTime = intent?.getStringExtra("takeAwayTime").toString()


        findViewById<TextView>(R.id.order_done_total_amount_tv).text = "%.2f".format(subTotalPrice)
        findViewById<TextView>(R.id.order_done_payment_method_tv).text = paymentMethod
        findViewById<TextView>(R.id.order_done_take_away_time).text = takeAwayTime

        Handler().postDelayed({
            window.statusBarColor = resources.getColor(R.color.light_green)
            window.navigationBarColor = resources.getColor(R.color.light_green)
            processingLL.visibility = ViewGroup.INVISIBLE
            completeLL.visibility = ViewGroup.VISIBLE
                              }, 2000)

        generateOrderID()
        setCurrentDateAndTime()
        saveOrderRecordToDatabase()

        findViewById<ImageView>(R.id.order_done_show_qr_iv).setOnClickListener { showQRCode() }
        findViewById<ImageView>(R.id.order_done_share_iv).setOnClickListener { shareOrder() }
        findViewById<LinearLayout>(R.id.order_done_cancel_order_ll).setOnClickListener { cancelCurrentOrder() }
        findViewById<LinearLayout>(R.id.order_done_contact_us_ll).setOnClickListener { contactUs() }
    }

    private fun generateOrderID() {
        val r1: String = ('A'..'Z').random().toString() + ('A'..'Z').random().toString()
        val r2: Int = (10000..99999).random()

        orderID = "$r1$r2"
        orderIDTV.text = "Order ID: $orderID"
    }

    private fun setCurrentDateAndTime() {
        val c = Calendar.getInstance()
        val monthName = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val dayNumber = c.get(Calendar.DAY_OF_MONTH)
        val year = c.get(Calendar.YEAR)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val time = "$hour:$minute"
        val date = SimpleDateFormat("HH:mm").parse(time)
        val orderTime = SimpleDateFormat("hh:mm aa").format(date)

        orderDate = "${monthName.substring(0, 3)} %02d, $year at $orderTime".format(dayNumber)
        dateAndTimeTV.text = orderDate
    }

    private fun saveOrderRecordToDatabase() {
        val item = OrderHistoryItem(orderDate, orderID, "Order Successful", paymentMethod, "\$%.2f".format(subTotalPrice))
        val db = DatabaseHandler(this)
        db.insertOrderData(item)

        saveCurrentOrderToDatabase()
    }

    private fun saveCurrentOrderToDatabase() {
        val item = CurrentOrderItem(
            orderID,
            takeAwayTime,
            if(paymentMethod.startsWith("Pending")) "Pending" else "Done",
            getOrderItemNames(),
            getOrderItemQty(),
            totalItemPrice.toString(),
            totalTaxPrice.toString(),
            subTotalPrice.toString()
        )
        val db = DatabaseHandler(this)
        db.insertCurrentOrdersData(item)
    }


    private fun cancelCurrentOrder() {
        AlertDialog.Builder(this)
            .setTitle("Order Cancellation")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes, Cancel Order", DialogInterface.OnClickListener { _, _ ->
                val result = DatabaseHandler(this).deleteCurrentOrderRecord(orderID)
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                onBackPressed()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()
    }

    private fun showQRCode() {
        val bundle = Bundle()
        bundle.putString("orderID", orderID)
        val dialog = QRCodeFragment()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "QR Code Generator")
    }

    private fun shareOrder() {
        val intent = Intent(Intent.ACTION_SEND)
        val message = "Order Status: ${orderStatusTV.text}\n" +
                "${orderIDTV.text}\n" +
                "$paymentMethod\n" +
                "Order Take-Away Time: $takeAwayTime\n" +
                "Total Amount: $%.2f".format(subTotalPrice)

        intent.putExtra(Intent.EXTRA_TEXT, message)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Share To"))
    }

    private fun contactUs() {
        startActivity(Intent(this, ContactUsActivity::class.java))
    }

    fun openMainActivity(view: View) {onBackPressed()}

    private fun getOrderItemNames(): String {
        //stores all the item names in a single string separated by (;)
        var itemNames = ""
        for(item in DatabaseHandler(this).readCartData()) {
            itemNames += item.itemName + ";"
        }
        return itemNames.substring(0, itemNames.length-1)
    }

    private fun getOrderItemQty(): String {
        //stores all the item qty in a single string separated by (;)
        var itemQty = ""
        for(item in DatabaseHandler(this).readCartData()) {
            itemQty += item.quantity.toString() + ";"
        }
        return itemQty.substring(0, itemQty.length-1)
    }
}