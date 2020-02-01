package com.khs.howstoday.action.talks.action

import android.view.View
import com.khs.howstoday.R

class FriendFragmentAction:View.OnClickListener{
    var friendFragmentActionInterface:FriendFragmentActionInterface?=null

    interface FriendFragmentActionInterface{
        fun search()
    }

    fun addFriendFragmentActionInterface(listener:FriendFragmentActionInterface){
        friendFragmentActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.talks_friend_searchbtn->{ friendFragmentActionInterface?.search() }
        }
    }
}