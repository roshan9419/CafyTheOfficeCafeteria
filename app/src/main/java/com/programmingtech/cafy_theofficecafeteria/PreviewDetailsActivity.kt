package com.programmingtech.cafy_theofficecafeteria

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri

class PreviewDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_details)

        var name = intent.getStringExtra("name")!!
        var email = intent.getStringExtra("email")!!
        var companyOrg = intent.getStringExtra("companyOrg")!!
        var empID = intent.getStringExtra("empID")!!
        var mobile = intent.getStringExtra("mobile")!!
        val imageUri = intent.getStringExtra("imageUri")!!

        if(name.isEmpty()) name = "Not filled"
        if(email.isEmpty()) email = "Not filled"
        if(companyOrg.isEmpty()) companyOrg = "Not filled"
        if(empID.isEmpty()) empID = "Not filled"
        if(mobile.isEmpty()) mobile = "Not filled"

        findViewById<TextView>(R.id.preview_details_name).text = name
        findViewById<TextView>(R.id.preview_details_email).text = email
        findViewById<TextView>(R.id.preview_details_company_organization).text = companyOrg
        findViewById<TextView>(R.id.preview_details_employee_id).text = empID
        findViewById<TextView>(R.id.preview_details_mobile_number).text = mobile

        if (imageUri.isNotEmpty()) {
            val iCard = findViewById<ImageView>(R.id.preview_details_employee_id_card_iv)
            iCard.setImageURI(imageUri.toUri())
            iCard.visibility = ViewGroup.VISIBLE
        } else {
            findViewById<TextView>(R.id.preview_details_employee_id_tv).text = "Your ID Card (Not Available)"
        }

    }

    fun goBack(view: View) {
        onBackPressed()
    }
}