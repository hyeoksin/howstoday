package com.khs.howstoday.action

import android.view.View
import com.khs.howstoday.R

class SettingsAction():View.OnClickListener{

    interface SettingsActionInterface{
        fun back()
        fun signOutAccount()
        fun deleteAccount()
    }

    var settingsActionInteface:SettingsActionInterface? = null

    fun addSettingActionInterface(listener:SettingsActionInterface){
        settingsActionInteface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.account_setting_back ->      { settingsActionInteface?.back() }
            R.id.account_setting_logout ->    { settingsActionInteface?.signOutAccount() }
            R.id.account_setting_delete ->    { settingsActionInteface?.deleteAccount() }
        }
    }
}