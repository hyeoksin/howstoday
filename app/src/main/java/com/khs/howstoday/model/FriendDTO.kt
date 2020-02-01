package com.khs.howstoday.model

class FriendDTO(
    var friendFromUser: MutableMap<String,Boolean> = HashMap(),      // 나를 친구로 등록한 유저 uid
    var friendFromCount:Int =0,                                     // 나를 친구로 등록한 수
    var friendToUser: MutableMap<String,Boolean> = HashMap(),        // 내가 친구로 등록한 유저 uid
    var friendToCount:Int=0                                         // 내가 친구로 등록한 유저 수
)