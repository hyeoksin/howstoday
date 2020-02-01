package com.khs.howstoday.action.profile.action

import android.view.View
import com.khs.howstoday.R

class ProfileDetailRecyclerViewAction:View.OnClickListener{

    var profileDetailRecyclerViewActionInterface:ProfileDetailRecyclerViewActionInterface?=null

    interface ProfileDetailRecyclerViewActionInterface {
        fun favorite()
        fun moveCommentList()
    }

    fun addProfileDetailRecyclerViewActionInterface(listener:ProfileDetailRecyclerViewActionInterface){
        profileDetailRecyclerViewActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.profile_detail_favorite_imageview ->           { profileDetailRecyclerViewActionInterface?.favorite() }
            R.id.profile_detail_comment_imageview ->            { profileDetailRecyclerViewActionInterface?.moveCommentList() }
            R.id.profile_detail_commentcounter_textview ->      { profileDetailRecyclerViewActionInterface?.moveCommentList() }
        }
    }
}
