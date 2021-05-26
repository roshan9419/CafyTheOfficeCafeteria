package com.programmingtech.cafy_theofficecafeteria

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import datamodels.IdCardModel
import java.util.*


class RegisterUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    private lateinit var employeeIDCardIV: ImageView

    private lateinit var fullNameTIL: TextInputLayout
    private lateinit var emailTIL: TextInputLayout
    private lateinit var organizationTIL: TextInputLayout
    private lateinit var employeeIDTIL: TextInputLayout
    private lateinit var mobileNumberTIL: TextInputLayout
    private lateinit var createPasswordTIL: TextInputLayout
    private lateinit var confirmPasswordTIL: TextInputLayout

    private lateinit var employeeIDCardUri: Uri
    var idUploaded = false

    private lateinit var agreeCheckBox: CheckBox
    private lateinit var registerBtn: Button

    private lateinit var registerProgressDialog: ProgressDialog

    private var doubleBackToExit = false
    override fun onBackPressed() {
        if(doubleBackToExit) {
            super.onBackPressed()
            return
        }
        doubleBackToExit = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExit = false }, 2000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("employees")
        storageRef = FirebaseStorage.getInstance().reference.child("employeeIdCards")

        fullNameTIL = findViewById(R.id.register_full_name_til)
        emailTIL = findViewById(R.id.register_email_til)
        organizationTIL = findViewById(R.id.register_organization_til)
        employeeIDTIL = findViewById(R.id.register_employee_id_til)
        mobileNumberTIL = findViewById(R.id.register_mobile_num_til)
        createPasswordTIL = findViewById(R.id.register_create_password_til)
        confirmPasswordTIL = findViewById(R.id.register_confirm_password_til)
        employeeIDCardIV = findViewById(R.id.register_employee_id_card_iv)

        agreeCheckBox = findViewById(R.id.register_agree_check_box)
        registerBtn = findViewById(R.id.register_emp_btn)

        agreeCheckBox.setOnClickListener {
            registerBtn.isEnabled = agreeCheckBox.isChecked
        }

        registerProgressDialog = ProgressDialog(this)
        registerBtn.setOnClickListener { registerEmployee() }
    }

    fun chooseImageFromGallery(view: View) {
        CropImage
            .activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val uri = result.uri
                employeeIDCardIV.visibility = ViewGroup.VISIBLE
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                employeeIDCardIV.setImageBitmap(bitmap)
                employeeIDCardUri = uri
                idUploaded = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun validateName(): Boolean {
        val fullName = fullNameTIL.editText!!.text.toString().trim()
        if(fullName.isEmpty()) {
            fullNameTIL.error = getString(R.string.field_empty)
            return false
        }
        fullNameTIL.error = null
        return true
    }

    private fun validateEmail(): Boolean {
        val email = emailTIL.editText!!.text.toString().trim()
        if(email.isEmpty()) {
            emailTIL.error = getString(R.string.field_empty)
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTIL.error = getString(R.string.invalid_email)
            return false
        }
        emailTIL.error = null
        return true
    }

    private fun validateOrganization(): Boolean {
        val organization = organizationTIL.editText!!.text.toString().trim()
        if(organization.isEmpty()) {
            organizationTIL.error = getString(R.string.field_empty)
            return false
        }
        organizationTIL.error = null
        return true
    }

    private fun validateEmployeeID(): Boolean {
        val employeeId = employeeIDTIL.editText!!.text.toString().trim()
        if(employeeId.isEmpty()) {
            employeeIDTIL.error = getString(R.string.field_empty)
            return false
        }
        employeeIDTIL.error = null
        return true
    }

    private fun validateMobileNum(): Boolean {
        val mobileNum = mobileNumberTIL.editText!!.text.toString().trim()
        if(mobileNum.isEmpty()) {
            mobileNumberTIL.error = getString(R.string.field_empty)
            return false
        }
        if(mobileNum.length < 10) {
            mobileNumberTIL.error = getString(R.string.invalid_mobile_no)
            return false
        }
        mobileNumberTIL.error = null
        return true
    }

    private fun validatePassword(): Boolean {
        createPasswordTIL.error = null
        confirmPasswordTIL.error = null

        val createPass = createPasswordTIL.editText!!.text.toString().trim()
        val confirmPass = confirmPasswordTIL.editText!!.text.toString().trim()

        if(createPass.isEmpty()) { createPasswordTIL.error = getString(R.string.field_empty) }
        if(confirmPass.isEmpty()) { confirmPasswordTIL.error = getString(R.string.field_empty) }
        if(createPass.isEmpty() || confirmPass.isEmpty()) return false

        if(createPass.length < 6) {
            createPasswordTIL.error = "Password is too short (Min. 6 Characters)"
            return false
        }
        if(createPass != confirmPass) {
            confirmPasswordTIL.error = "Password don't match"
            return false
        }
        createPasswordTIL.error = null
        confirmPasswordTIL.error = null
        return true
    }

    private fun registerEmployee() {
        if(!validateName() or !validateEmail() or !validateOrganization() or !validateEmployeeID() or !validateMobileNum() or !validatePassword())
            return

        if(!idUploaded) {
            Toast.makeText(this, "Please Upload your ID Card", Toast.LENGTH_SHORT).show()
            return
        }

        val email = emailTIL.editText!!.text.toString()
        val password = confirmPasswordTIL.editText!!.text.toString()
        val name = fullNameTIL.editText!!.text.toString()

        registerProgressDialog.setTitle("Registering...")
        registerProgressDialog.setMessage("We are creating your account")
        registerProgressDialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    val currentUser = auth.currentUser
                    val profileUpdates = userProfileChangeRequest { displayName = name }

                    currentUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { it ->
                            if(it.isSuccessful) {
                                //Name Updated
                                uploadIDCardToFirebaseStorage(currentUser)
                            }
                        }
                } else {
                    showDialog("Registration Failed", task.exception.toString())
                }
            }
    }

    private fun uploadIDCardToFirebaseStorage(user: FirebaseUser) {

        val reference = storageRef.child("${user.uid}.jpeg")

        registerProgressDialog.setTitle("Uploading ID Card...")

        reference.putFile(employeeIDCardUri)
            .addOnSuccessListener { _ ->
                // Image uploaded successfully
                // Dismiss dialog
                registerProgressDialog.dismiss()
                Toast.makeText(this, "ID Card Uploaded!!", Toast.LENGTH_SHORT).show()
                reference.downloadUrl.addOnSuccessListener { uri ->
                    val model = IdCardModel(uri.toString())
                    addEmployeeDetailsToDatabase(user, model)
                }
            }
            .addOnFailureListener { e -> // Error, Image not uploaded
                registerProgressDialog.dismiss()
                Toast.makeText(this, "Failed to upload ID Card" + e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0
                        * taskSnapshot.bytesTransferred
                        / taskSnapshot.totalByteCount)
                registerProgressDialog.setMessage(
                    "Uploaded " + progress.toInt() + "%"
                )
            }
    }

    private fun addEmployeeDetailsToDatabase(user: FirebaseUser, idCardDownloadUri: IdCardModel) {
        registerProgressDialog.setMessage("Uploading details to database")

        val orgName = organizationTIL.editText!!.text.toString()
        val empIDNo = employeeIDTIL.editText!!.text.toString()
        val mobileNo = mobileNumberTIL.editText!!.text.toString()

        val employee = databaseRef.child(user.uid)
        employee.child("organization").setValue(orgName)
        employee.child("emp_id").setValue(empIDNo)
        employee.child("mobile_no").setValue(mobileNo)
        employee.child("gender").setValue("none")
        employee.child("reg_id").setValue(getRegID())
        employee.child("reg_date").setValue(getRegDate())
        employee.child("emp_id_card_uri").setValue(idCardDownloadUri)

        sendEmailVerification(user)
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { it ->
                if(it.isSuccessful) {
                    AlertDialog.Builder(this)
                        .setTitle("Verify e-mail address")
                        .setMessage("Registered Successfully !\nA verification link has been sent to your Email address")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                            startActivity(Intent(this, LoginUserActivity::class.java))
                            finish()
                        })
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
            .addOnFailureListener { t ->
                showDialog("Verification Link", t.message.toString())
            }
    }

    private fun getRegDate(): String {
        val c = Calendar.getInstance()
        val monthName = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val dayNumber = c.get(Calendar.DAY_OF_MONTH)
        val year = c.get(Calendar.YEAR)
        return "%02d-${monthName.substring(0, 3)}-$year".format(dayNumber)
    }

    private fun getRegID(): String {
        val empName = fullNameTIL.editText!!.text.toString().split(" ")
        val r1 = if(empName.size == 2) {
            "${empName[0][0]}${empName[1][0]}".toUpperCase(Locale.ROOT) //first character of first name and last name
        } else {
            "${empName[0][0]}${empName[0][empName[0].length - 1]}".toUpperCase(Locale.ROOT) //first and last character of first name
        }
        val r2: Int = (10000..99999).random()
        return "$r1$r2"
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.dismiss()
            })
            .setCancelable(false)
            .create()
            .show()
        registerProgressDialog.dismiss()
    }

    fun openPreviewActivity(view: View) {
        val intent = Intent(this, PreviewDetailsActivity::class.java)

        intent.putExtra("name", fullNameTIL.editText!!.text.toString())
        intent.putExtra("email", emailTIL.editText!!.text.toString())
        intent.putExtra("companyOrg", organizationTIL.editText!!.text.toString())
        intent.putExtra("empID", employeeIDTIL.editText!!.text.toString())
        intent.putExtra("mobile", mobileNumberTIL.editText!!.text.toString())

        if(idUploaded) intent.putExtra("imageUri", employeeIDCardUri.toString())
        else intent.putExtra("imageUri", "")
        startActivity(intent)
    }

    fun openLoginActivity(view: View) {
        startActivity(Intent(this, LoginUserActivity::class.java))
        finish()
    }
}