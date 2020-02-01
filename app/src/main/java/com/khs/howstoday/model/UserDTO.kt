package com.khs.howstoday.model

data class UserDTO(
    var uid:String?=null,
    var userEmail:String?=null,
    var userNickName:String??=null,
    var userImage : String? =null,
    var userLocation : String? = null,
    var userAge:Int?=null,
    var crush:CrushUserDTO?=null,
    var friend:FriendDTO?=null
)