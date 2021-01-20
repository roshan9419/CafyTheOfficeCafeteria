package com.programmingtech.cafy_theofficecafeteria

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPassword : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.popup_forgot_password, container, false)

        view.findViewById<Button>(R.id.forgot_send_link_btn).setOnClickListener {
            val email = view.findViewById<EditText>(R.id.forgot_email_edit_text).text.toString()

            if (email.isEmpty()) {
                Toast.makeText(context, "Please provide your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Attention")
                        builder.setMessage("A password reset link has been sent. Please check your Inbox.")
                        builder.setPositiveButton("OK", DialogInterface.OnClickListener {dialogInterface, _ ->
                            dialogInterface.dismiss()
                        })
                        builder.create().show()
                        dismiss()
                    } else {
                        Toast.makeText(context, "This email is not registered", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Unable to reset your password\n" +
                            "${it.message}", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
        }

        return view
    }

}