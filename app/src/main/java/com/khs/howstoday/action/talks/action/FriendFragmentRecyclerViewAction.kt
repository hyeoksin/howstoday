package com.khs.howstoday.action.talks.action

import android.view.View
import com.khs.howstoday.R

class FriendFragmentRecyclerViewAction:View.OnClickListener {
    var friendFragmentRecyclerViewActionInterface:FriendFragmentRecyclerViewActionInterface?=null

    interface FriendFragmentRecyclerViewActionInterface{
        fun moveProfile()
        fun openTalk()
    }

    fun addFriendFragmentRecyclerViewActionInterface(listener:FriendFragmentRecyclerViewActionInterface){
        friendFragmentRecyclerViewActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.friendlist_detail_layout ->{
                friendFragmentRecyclerViewActionInterface?.moveProfile()
            }
            R.id.friendlist_detail_talk->{
                friendFragmentRecyclerViewActionInterface?.openTalk()
            }
        }
    }
}