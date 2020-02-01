package com.khs.howstoday.action

import android.view.View
import com.khs.howstoday.R
import kotlinx.android.synthetic.main.activity_comment.view.*

class CommentActivityAction:View.OnClickListener {

    var commentActivityActionInterface:CommentActivityActionInterface? =null

    interface CommentActivityActionInterface {
        fun send()
        fun back()
    }

    fun addCommentActivityActionInterface(listener:CommentActivityActionInterface){
        commentActivityActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.comment_btn_send            -> { commentActivityActionInterface?.send() }
            R.id.comment_setting_back        -> { commentActivityActionInterface?.back()}
        }
    }
}

