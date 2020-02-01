package com.khs.howstoday.action

import android.view.View
import com.khs.howstoday.R

class PostsRecyclerViewAction():View.OnClickListener{

    var postsViewRecyclerViewActionInterface: PostsViewRecyclerViewActionInterface? = null

    interface PostsViewRecyclerViewActionInterface{
        fun favoriteEvent()
        fun moveUserProfile()
        fun moveCommentList()
    }

    fun addPostsViewRecyclerViewActionInterface(listener: PostsViewRecyclerViewActionInterface){
        postsViewRecyclerViewActionInterface = listener
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.postsviewitem_favorite_imageview -> { postsViewRecyclerViewActionInterface?.favoriteEvent() }
            R.id.postsviewitem_user_image ->         {
                postsViewRecyclerViewActionInterface?.moveUserProfile()
                // activity.bottom_navigation.selectedItemId = R.id.action_profile
                // 위 코드를 쓰게되면 2번 로딩 된다.
            }
            R.id.postsviewitem_comment_imageview        -> { postsViewRecyclerViewActionInterface?.moveCommentList()}
            R.id.postsviewitem_commentcounter_textview  -> { postsViewRecyclerViewActionInterface?.moveCommentList()}
        }
    }
}