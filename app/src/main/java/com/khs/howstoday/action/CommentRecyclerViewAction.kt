package com.khs.howstoday.action

import android.view.View
import com.khs.howstoday.R

class CommentRecyclerViewAction:View.OnClickListener{
    var commentRecyclerViewActionInterface: CommentRecyclerViewAction.CommentRecyclerViewActionInterface? = null

    interface CommentRecyclerViewActionInterface{
        fun favoriteEvent()
        fun moveUserProfile()
    }

    fun addCommentRecyclerViewActionInterface(listener: CommentRecyclerViewAction.CommentRecyclerViewActionInterface){
        commentRecyclerViewActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.commentviewitem_userimage ->{ commentRecyclerViewActionInterface?.moveUserProfile()}
        }
    }
}