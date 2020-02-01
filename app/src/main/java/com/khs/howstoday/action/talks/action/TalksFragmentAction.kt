package com.khs.howstoday.action.talks.action

import android.view.View
import com.khs.howstoday.R

class TalksFragmentAction :View.OnClickListener{

    var talksFragmentActionInterface:TalksFragmentActionInterface?=null

    interface TalksFragmentActionInterface{
        fun addFriend()
    }

    fun addTalksFragmentActionInterface(listener:TalksFragmentActionInterface){
        talksFragmentActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.talks_btn_addfriend ->{
                talksFragmentActionInterface?.addFriend()
            }
        }
    }
}