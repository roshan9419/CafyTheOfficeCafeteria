package com.programmingtech.cafy_theofficecafeteria

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import datamodels.MenuItem

open class BottomSheetSelectedItemDialog: BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.selected_menu_bottom_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalPrice = this.arguments?.getFloat("totalPrice")
        val totalItems = this.arguments?.getInt("totalItems")

        view.findViewById<TextView>(R.id.total_ordered_items_price_tv).text = "$%.2f".format(totalPrice)
        view.findViewById<TextView>(R.id.total_ordered_items_tv).text = "Your Order ($totalItems items)"

        val placeOrderBTN: Button = view.findViewById(R.id.place_order_btn)
        placeOrderBTN.setOnClickListener {
            if(totalItems != 0) {
                val intent = Intent(context, UserMenuOrderActivity::class.java)
                intent.putExtra("totalItems", totalItems)
                intent.putExtra("totalPrice", totalPrice)

                dismiss()
                startActivity(intent)
            } else {
                Toast.makeText(context, "Please select at-least 1 item", Toast.LENGTH_SHORT).show()
            }
        }
    }

}