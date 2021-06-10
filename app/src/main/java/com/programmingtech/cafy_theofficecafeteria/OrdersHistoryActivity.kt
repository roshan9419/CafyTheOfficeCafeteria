package com.programmingtech.cafy_theofficecafeteria

import adapters.RecyclerOrderHistoryAdapter
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import datamodels.OrderHistoryItem
import services.DatabaseHandler

class OrdersHistoryActivity : AppCompatActivity() {

    private var orderHistoryList = ArrayList<OrderHistoryItem>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerOrderHistoryAdapter

    private lateinit var deleteRecordsIV : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_history)

        recyclerView = findViewById(R.id.order_history_recycler_view)
        recyclerAdapter = RecyclerOrderHistoryAdapter(this, orderHistoryList)
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        deleteRecordsIV = findViewById(R.id.order_history_delete_records_iv)
        deleteRecordsIV.setOnClickListener { deleteOrderHistoryRecords() }

        loadOrderHistoryListFromDatabase()
    }

    private fun loadOrderHistoryListFromDatabase() {
        val db = DatabaseHandler(this)
        val data = db.readOrderData()

        if(data.size == 0) {
            deleteRecordsIV.visibility = ViewGroup.INVISIBLE
            return
        }

        findViewById<LinearLayout>(R.id.order_history_empty_indicator_ll).visibility = ViewGroup.GONE
        for(i in 0 until data.size) {
            val item = OrderHistoryItem()
            item.date = data[i].date
            item.orderId = data[i].orderId
            item.orderStatus = data[i].orderStatus
            item.orderPayment = data[i].orderPayment
            item.price = data[i].price
            orderHistoryList.add(item)
            orderHistoryList.reverse()
            recyclerAdapter.notifyItemRangeInserted(0, data.size)
        }
    }

    private fun deleteOrderHistoryRecords() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want delete all your order history?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener {dialogInterface, _ ->
                val db = DatabaseHandler(this)
                db.dropOrderHistoryTable()
                deleteRecordsIV.visibility = ViewGroup.INVISIBLE
                findViewById<LinearLayout>(R.id.order_history_empty_indicator_ll).visibility = ViewGroup.VISIBLE


                val size = orderHistoryList.size
                orderHistoryList.clear()
                recyclerAdapter.notifyItemRangeRemoved(0, size)

                dialogInterface.dismiss()
            } )
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .create().show()
    }

    fun goBack(view: View) {onBackPressed()}
}