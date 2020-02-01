package com.khs.howstoday

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.khs.howstoday.action.SettingsDeleteDialogAction
import com.khs.howstoday.action.SettingsAction
import kotlinx.android.synthetic.main.activity_account_setting.*

class SettingsActivity : AppCompatActivity() {

    var settingsAction:SettingsAction? =null
    var accountDeleteDialog: SettingsDeleteDialogAction?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)
        setupListener()
    }

    private fun setupListener() {
        settingsAction = SettingsAction().apply {
            addSettingActionInterface(object : SettingsAction.SettingsActionInterface {
                override fun back() {
                    onBackPressed()
                }

                override fun signOutAccount() {
                    signOutAction()
                }

                override fun deleteAccount() {
                    showDeleteDialog()
                }
            })
        }
        account_setting_logout.setOnClickListener(settingsAction)
        account_setting_delete.setOnClickListener(settingsAction)
        account_setting_back.setOnClickListener(settingsAction)
    }

    private fun showDeleteDialog() {
        accountDeleteDialog = SettingsDeleteDialogAction().apply {
            addAccountDeleteDialogInterface(object : SettingsDeleteDialogAction.AccountDeleteDialogInterface {
                override fun delete() {
                    deleteAccount()
                }
                override fun cancelDelete() {}
            })
        }
        accountDeleteDialog?.show(supportFragmentManager,"")
    }

    private fun deleteAccount() {
        AuthUI.getInstance()
            .delete(this@SettingsActivity)
            .addOnCompleteListener {
                MoveToLoginActivity()
                Toast.makeText(this@SettingsActivity,"그동안 이용해주셔서 감사합니다.",Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOutAction() {
        AuthUI.getInstance()
            .signOut(this@SettingsActivity)
            .addOnCompleteListener {
                MoveToLoginActivity()
                Toast.makeText(this@SettingsActivity,"로그아웃 했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun MoveToLoginActivity() {
        startActivity(Intent(this@SettingsActivity,LoginActivity::class.java))
    }
}
