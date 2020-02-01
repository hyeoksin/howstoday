package com.khs.howstoday.model

data class ContentDTO(
    var explain : String? =null,
    var contentImageUrl : String? =null,
    var userLocation : String? = null,
    var uid : String? = null,
    var userId : String? =null,
    var timestamp : Long? =null,
    var commentCount:Int? =0,
    var favoriteCount : Int =0,
    var favorites: MutableMap<String,Boolean> = HashMap()){

    data class Comment(
        var uid:String?=null,
        var userImageUrl:String?=null,
        var userId:String?=null,
        var comment:String?=null,
        var timestamp: Long?=null,
        var favoriteCount : Int =0,
        var favorites: MutableMap<String,Boolean> = HashMap()){

        data class CommentInComment(
            var uid:String?=null,
            var userImageUrl:String?=null,
            var userId:String?=null,
            var comment:String?=null,
            var timestamp: Long?=null
        )
    }
}