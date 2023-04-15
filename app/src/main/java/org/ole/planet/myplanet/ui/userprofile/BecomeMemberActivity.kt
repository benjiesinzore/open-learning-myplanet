package org.ole.planet.myplanet.ui.userprofile

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.Realm
import org.ole.planet.myplanet.MainApplication
import org.ole.planet.myplanet.R
import org.ole.planet.myplanet.base.BaseActivity
import org.ole.planet.myplanet.databinding.ActivityBecomeMemberBinding
import org.ole.planet.myplanet.datamanager.DatabaseService
import org.ole.planet.myplanet.datamanager.Service
import org.ole.planet.myplanet.model.RealmUserModel
import org.ole.planet.myplanet.service.SyncManager
import org.ole.planet.myplanet.service.UserProfileDbHandler
import org.ole.planet.myplanet.ui.sync.SyncActivity
import org.ole.planet.myplanet.utilities.NetworkUtils
import org.ole.planet.myplanet.utilities.Utilities
import org.ole.planet.myplanet.utilities.VersionUtils
import java.util.*

class BecomeMemberActivity : BaseActivity() {

    private var bindingBecomeMemberBinding : ActivityBecomeMemberBinding = ActivityBecomeMemberBinding.inflate(layoutInflater)

    var dob: String = "";
    lateinit var settings: SharedPreferences
    private fun showDatePickerDialog() {
        val now = Calendar.getInstance()
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, i, i1, i2 ->
            dob = String.format(Locale.US, "%04d-%02d-%02d", i, i1 + 1, i2)
            bindingBecomeMemberBinding.txtDob.text = dob
        }, now[Calendar.YEAR],
                now[Calendar.MONTH],
                now[Calendar.DAY_OF_MONTH])
        dpd.datePicker.maxDate = now.timeInMillis
        dpd.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_become_member)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        var mRealm: Realm = DatabaseService(this).realmInstance;
        var user = UserProfileDbHandler(this).userModel;
        val languages = resources.getStringArray(R.array.language)
        val languageSpinner = findViewById(R.id.spn_lang) as Spinner
        val adapter = ArrayAdapter<String>(this, R.layout.become_a_member_spinner_layout, languages)
        languageSpinner.adapter = adapter
        bindingBecomeMemberBinding.txtDob.setOnClickListener {
            showDatePickerDialog()
        }

        settings = getSharedPreferences(SyncActivity.PREFS_NAME, Context.MODE_PRIVATE)
        textChangedListener(mRealm)

        bindingBecomeMemberBinding.btnCancel.setOnClickListener {
            finish()
        }
        bindingBecomeMemberBinding.btnSubmit.setOnClickListener {
            var username: String? = bindingBecomeMemberBinding.etUsername.text.toString()
            var password: String? = bindingBecomeMemberBinding.etPassword.text.toString()
            var repassword: String? = bindingBecomeMemberBinding.etRePassword.text.toString()
            var fname: String? = bindingBecomeMemberBinding.etFname.text.toString()
            var lname: String? = bindingBecomeMemberBinding.etLname.text.toString()
            var mname: String? = bindingBecomeMemberBinding.etMname.text.toString()
            var email: String? = bindingBecomeMemberBinding.etEmail.text.toString()
            var language: String? = bindingBecomeMemberBinding.spnLang.selectedItem.toString()
            var phoneNumber: String? = bindingBecomeMemberBinding.etPhone.text.toString()
            var birthDate: String? = dob
            var level: String? = bindingBecomeMemberBinding.spnLevel.selectedItem.toString()

            var rb: RadioButton? = findViewById<View>(bindingBecomeMemberBinding.rbGender.checkedRadioButtonId) as RadioButton?
            var gender: String? = ""
            if (rb != null)
                gender = rb.text.toString()
            else {
                Utilities.toast(this, "Please select gender")
            }
            if (username!!.isEmpty()) {
                bindingBecomeMemberBinding.etUsername.error = "Please enter a username"
            }
            else if (username.contains(" ")){
                bindingBecomeMemberBinding.etUsername.error = "Invalid username"
            }
            if (password!!.isEmpty()) {
                bindingBecomeMemberBinding.etPassword.error = "Please enter a password"
            }
            else if (password != repassword) {
                bindingBecomeMemberBinding.etRePassword.error = "Password doesn't match"
            }
            if (email!!.isNotEmpty() && !Utilities.isValidEmail(email)) {
                bindingBecomeMemberBinding.etEmail.error = "Invalid email."
            }
            if (level == null) {
                Utilities.toast(this, "Level is required")
            }
            if (password!!.isEmpty() && phoneNumber!!.isNotEmpty()) {
                bindingBecomeMemberBinding.etRePassword.setText(phoneNumber)
                password = phoneNumber
                ///Add dialog that using phone as password , Agree / disagree
            }

            checkMandatoryFieldsAndAddMember(username, password, repassword, fname, lname, mname, email, language, level, phoneNumber, birthDate, gender, mRealm)

        }
    }

    private fun checkMandatoryFieldsAndAddMember(username: String, password: String, repassword: String?, fname: String?, lname: String?, mname: String?, email: String?, language: String?, level: String?, phoneNumber: String?, birthDate: String?, gender: String?, mRealm: Realm) {
        /**
         * Creates and adds a new member if the username and password
         * are not empty and password matches repassword.
         */
        if (username.isNotEmpty() && password.isNotEmpty() && repassword == password) {
            var obj = JsonObject()
            obj.addProperty("name", username)
            obj.addProperty("firstName", fname)
            obj.addProperty("lastName", lname)
            obj.addProperty("middleName", mname)
            obj.addProperty("password", password)
    //            obj.addProperty("repeatPassword", repassword )
            obj.addProperty("isUserAdmin", false)
            obj.addProperty("joinDate", Calendar.getInstance().timeInMillis)
            obj.addProperty("email", email)
            obj.addProperty("planetCode", settings.getString("planetCode", ""))
            obj.addProperty("parentCode", settings.getString("parentCode", ""))
            obj.addProperty("language", language)
            obj.addProperty("level", level)
            obj.addProperty("phoneNumber", phoneNumber)
            obj.addProperty("birthDate", birthDate)
            obj.addProperty("gender", gender)
            obj.addProperty("type", "user")
            obj.addProperty("betaEnabled", false)
            obj.addProperty("macAddress", NetworkUtils.getMacAddr())
            obj.addProperty("androidId", NetworkUtils.getMacAddr())
            obj.addProperty("uniqueAndroidId", VersionUtils.getAndroidId(MainApplication.context))
            obj.addProperty("customDeviceName", NetworkUtils.getCustomDeviceName(MainApplication.context))
            var roles = JsonArray()
            roles.add("learner")
            obj.add("roles", roles)
            bindingBecomeMemberBinding.pbar.visibility = View.VISIBLE
            Service(this).becomeMember(mRealm, obj) { res ->
                runOnUiThread {
                    bindingBecomeMemberBinding.pbar.visibility = View.GONE
                    Utilities.toast(this, res)
                }
                finish()
            }
        }
    }

    private fun textChangedListener(mRealm: Realm) {
        bindingBecomeMemberBinding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (RealmUserModel.isUserExists(mRealm, bindingBecomeMemberBinding.etUsername.text.toString())) {
                    bindingBecomeMemberBinding.etUsername.error = "username taken"
                    return
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        bindingBecomeMemberBinding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                bindingBecomeMemberBinding.etRePassword.isEnabled = bindingBecomeMemberBinding.etPassword.text.toString().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })
    }

}
