package com.khs.howstoday.fragment.bottom.talks

import androidx.fragment.app.Fragment

class TalksTabInfo(
    iconResId:Int?,
    title:String?,
    fragment: Fragment?
) {
    var iconResId:Int? =null
    var title:String? =null
    var fragment:Fragment? =null

    init{
        this.iconResId =iconResId
        this.title = title
        this.fragment =fragment
    }
}