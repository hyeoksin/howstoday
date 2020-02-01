package com.khs.howstoday.action

import android.view.View
import com.khs.howstoday.R

class AddPhotoActivityAction():View.OnClickListener{

    var photoActivityActionInterface:AddPhotoActivityActionInterface?=null

    interface AddPhotoActivityActionInterface{
        fun photoUpload()
    }

    fun addAddPhotoActivityActionInterface(listener:AddPhotoActivityActionInterface){
        photoActivityActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.addphoto_btn_upload ->{
                photoActivityActionInterface?.photoUpload()
            }
        }
    }
}