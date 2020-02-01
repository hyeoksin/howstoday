package com.khs.howstoday.model

class CrushUserDTO(
    var crushedFromCount:Int =0,
    var crushedFromUser : MutableMap<String,Boolean> = HashMap(),
    var crushToCount:Int=0,
    var crushToUser : MutableMap<String,Boolean> = HashMap()
)