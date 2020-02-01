package com.khs.howstoday.action

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.khs.howstoday.R
import kotlinx.android.synthetic.main.dialog_account_delete.*

class SettingsDeleteDialogAction:DialogFragment(){

    interface AccountDeleteDialogInterface{
        fun delete()
        fun cancelDelete()
    }

    private var accountDeleteDialogInterface: AccountDeleteDialogInterface? = null

    fun addAccountDeleteDialogInterface(listener: AccountDeleteDialogInterface){
        accountDeleteDialogInterface = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.dialog_account_delete,container,false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpListener()
    }

    private fun setUpListener() {
        delete_no.setOnClickListener {
            accountDeleteDialogInterface?.cancelDelete()
            dismiss()
        }
        delete_yes.setOnClickListener {
            accountDeleteDialogInterface?.delete()
        }
    }
}