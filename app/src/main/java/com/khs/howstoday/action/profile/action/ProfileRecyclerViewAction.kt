package com.khs.howstoday.action.profile.action

import android.view.View

class ProfileRecyclerViewAction:View.OnClickListener{


    var profileRecyclerViewActionInterface: ProfileRecyclerViewActionInterface?=null

    interface ProfileRecyclerViewActionInterface{
        fun contentImageDetail()
    }

    fun addProfileRecyclerViewActionInterface(listener: ProfileRecyclerViewActionInterface){
        profileRecyclerViewActionInterface = listener
    }

    override fun onClick(v: View?) {
        // imageview의 ID 값을 얻어올 수 없기때문에 아래 코드는 나중에 else로 처리
        profileRecyclerViewActionInterface?.contentImageDetail()
    }
}
