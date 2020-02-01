package com.khs.howstoday.action.profile.action

import android.view.View
import com.khs.howstoday.R

class ProfileFragmentAction:View.OnClickListener{

    var profileFragmentActionInterface: ProfileFragmentActionInterface?=null

    interface ProfileFragmentActionInterface{
        fun profileDetail()
        fun uploadImage()
        fun editUserImage()
        fun crush()
    }

    fun addProfileFragmentActionInterface(listener: ProfileFragmentActionInterface){
        profileFragmentActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.profile_btn_detail ->  { profileFragmentActionInterface?.profileDetail() }
            R.id.user_btn_upload    ->  { profileFragmentActionInterface?.uploadImage() }
            R.id.user_image         ->  { profileFragmentActionInterface?.editUserImage()}
            R.id.profile_btn_crush ->  { profileFragmentActionInterface?.crush()}
        }
    }

}