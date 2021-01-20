package com.programmingtech.cafy_theofficecafeteria

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ContactUsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private var canteenMail = ""
    private var canteenNum = ""

    override fun onStart() {
        super.onStart()
        window.statusBarColor = resources.getColor(R.color.purple_theme_color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("canteen_info")

        loadFields()

        findViewById<CardView>(R.id.contact_us_call_cv).setOnClickListener {call()}
        findViewById<CardView>(R.id.contact_us_mail_cv).setOnClickListener {mail()}
        findViewById<FloatingActionButton>(R.id.contact_us_send_btn).setOnClickListener { sendMessage() }

        findViewById<ImageView>(R.id.contact_us_go_back_iv).setOnClickListener { onBackPressed() }
    }

    private fun sendMessage() {
        val messageTIL: TextInputLayout = findViewById(R.id.contact_us_message_til)
        val message = messageTIL.editText!!.text.toString()

        if(message.isEmpty()) {
            messageTIL.error = "Tell us something"
            return
        }

        AlertDialog.Builder(this)
            .setMessage("Your query has been sent. We will reach you soon !!")
            .setPositiveButton("OK", DialogInterface.OnClickListener {_, _ ->
                onBackPressed()
            })
            .setCancelable(false)
            .create().show()
    }

    private fun call() {
        val number = Uri.parse("tel:$canteenNum")
        startActivity(Intent(Intent.ACTION_DIAL, number))
    }

    private fun mail() {
        try{
            val intent = Intent(Intent.ACTION_SEND)
            val recipient = arrayOf(canteenMail) //mail address of canteen
            intent.putExtra(Intent.EXTRA_EMAIL, recipient)
            intent.putExtra(Intent.EXTRA_SUBJECT,"Contact: Cafy")
            intent.putExtra(Intent.EXTRA_TEXT,"What do you want to tell us?")
            intent.putExtra(Intent.EXTRA_CC,"mailcc@gmail.com")
            intent.type = "text/html"
            intent.setPackage("com.google.android.gm")
            startActivity(Intent.createChooser(intent, "Send mail"))

        }catch(e: Exception){
            Toast.makeText(this, "Unable to send mail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFields() {
        val user = auth.currentUser!!
        findViewById<TextInputEditText>(R.id.contact_us_name_et).setText(user.displayName)
        findViewById<TextInputEditText>(R.id.contact_us_email_et).setText(user.email)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //storing canteen mail and number from online database
                canteenMail = snapshot.child("canteen_email").value.toString()
                canteenNum = snapshot.child("canteen_number").value.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}