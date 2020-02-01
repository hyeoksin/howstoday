package com.khs.howstoday.action.talks.action

import android.view.View
import com.khs.howstoday.R
import kotlinx.android.synthetic.main.dialog_talks_friend_add.view.*

class FriendAddDialogAction :View.OnClickListener{

    private var friendAddDialogActionInterface: FriendAddDialogActionInterface? = null

    interface FriendAddDialogActionInterface{
        fun add()
        fun cancel()
    }

    fun addFriendAddDialogActionInterface(listener: FriendAddDialogActionInterface){
        friendAddDialogActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.talks_friend_btn_add_yes -> { friendAddDialogActionInterface?.add()}
            R.id.talks_friend_btn_add_no ->  { friendAddDialogActionInterface?.cancel()}
        }
    }
}