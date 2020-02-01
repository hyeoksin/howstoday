package com.khs.howstoday.fragment.bottom

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.khs.howstoday.CommentActivity
import com.khs.howstoday.R
import com.khs.howstoday.action.PostsRecyclerViewAction
import com.khs.howstoday.action.profile.ProfileContentDetail
import com.khs.howstoday.model.ContentDTO
import kotlinx.android.synthetic.main.cotent_detail.view.*
import kotlinx.android.synthetic.main.fragment_posts.view.*
import java.lang.ref.WeakReference

class PostsFragment:Fragment(){

    var fragmentView:View? =null
    var fireStore:FirebaseFirestore? = null
    var storage:FirebaseStorage? =null
    var uid : String? = null
    var task:PostFragmentTask?=null

    init {
        initializeVariable()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_posts,container,false)
        fragmentView?.postsviewfragment_recyclerview?.adapter = PostsViewRecyclerViewAdapter()
        fragmentView?.postsviewfragment_recyclerview?.layoutManager = LinearLayoutManager(activity)
        return fragmentView
    }

    private fun initializeVariable() {
        storage = FirebaseStorage.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
    }

    inner class PostsViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var postsRecyclerViewAction:PostsRecyclerViewAction?=null
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init{
            loadData()
        }

        fun loadData(){
            fireStore?.collection("contents")?.orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                // 안전성을 위해서 null 경우 종료
                if (querySnapshot == null) {
                    return@addSnapshotListener
                }
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)                         // 게시물 리스트
                    contentUidList.add(snapshot.id)                 // Uid 리스트
                }
                notifyDataSetChanged()                              // 새로고침
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.cotent_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view){

            fun bindItems(viewHolder: CustomViewHolder, position: Int) {
                var itemView = viewHolder.itemView
                // 유저 ID
                itemView.postsviewitem_user_email.text = contentDTOs!![position].userId
                // 유저 위치
                itemView.postsviewitem_user_location.text = contentDTOs!![position].userLocation
                // 사진 설명
                itemView.postsviewitem_explain_textview.text =  contentDTOs!![position].explain
                // 좋아요 갯수
                itemView.postsviewitem_favoritecounter_textview.text = "좋아요 "+contentDTOs!![position].favoriteCount
                // 댓글 개수
                itemView.postsviewitem_commentcounter_textview.text = "댓글 "+contentDTOs!![position].commentCount

                task = PostFragmentTask(viewHolder,position)
                task?.execute()
            }

            fun setIconImage(viewHolder: PostsFragment.PostsViewRecyclerViewAdapter.CustomViewHolder, position: Int) {
                Glide.with(viewHolder.itemView.context).load(contentDTOs!![position].contentImageUrl).into(itemView.postsviewitem_imageview_content)
                Glide.with(viewHolder.itemView.context).load(R.drawable.icon_chat).override(120,120).into(itemView.postsviewitem_talk_imageview)
                Glide.with(viewHolder.itemView.context).load(R.drawable.icon_comment).override(120,120).into(itemView.postsviewitem_comment_imageview)
            }


            fun setFavoriteImage(position: Int) {
                if(contentDTOs!![position].favorites.containsKey(uid)){ // uid는 내 uid
                    Glide.with(itemView.context).load(R.drawable.icon_heart_full).override(160,160).into(itemView.postsviewitem_favorite_imageview)
//                    itemView.postsviewitem_favorite_imageview.setImageResource(R.drawable.icon_heart_full)
                }else{
                    Glide.with(itemView.context).load(R.drawable.icon_heart_empty_bold).override(160,160).into(itemView.postsviewitem_favorite_imageview)
//                    itemView.postsviewitem_favorite_imageview.setImageResource(R.drawable.icon_heart_empty_bold)
                }
            }

            fun setContentUserImage(viewHolder: CustomViewHolder, position: Int) {
                var itemView = viewHolder.itemView
                // 데이터베이스에서 uri를 가져오기
                var docRef = fireStore
                    ?.collection("users")
                    ?.document(contentDTOs!![position].userId.toString())
                    ?.get()

                docRef?.addOnSuccessListener {
                    Glide.with(itemView.context)
                        .load(it.get("userImage").toString())
                        .error(R.drawable.icon_profile)
                        .placeholder(R.drawable.icon_profile)
                        .apply(RequestOptions().circleCrop())
                        .into(itemView.postsviewitem_user_image)
                }?.addOnFailureListener {
                    Glide.with(itemView.context)
                        .load(R.drawable.icon_profile)
                        .error(R.drawable.icon_profile)
                        .placeholder(R.drawable.icon_profile)
                        .apply(RequestOptions().circleCrop())
                        .into(itemView.postsviewitem_user_image)
                    Log.d(" DEBUG","Failure")
                }
            }

            fun setRecyclerViewAction(viewHolder: CustomViewHolder, position: Int) {
                postsRecyclerViewAction = PostsRecyclerViewAction().apply {
                    addPostsViewRecyclerViewActionInterface(object : PostsRecyclerViewAction.PostsViewRecyclerViewActionInterface {
                        override fun favoriteEvent() {
                            favoriteEvent(position)
                        }

                        override fun moveUserProfile() {
                            var fragment = ProfileFragment()
                            var bundle = Bundle()
                            bundle.putString("destinationUid",contentDTOs!![position].uid)
                            bundle.putString("destinationUserEmail",contentDTOs!![position].userId)
                            fragment?.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
                            // main_content를 fragment로 바꾸는 부분
                            // bundle로 데이터 처리
                        }

                        override fun moveCommentList() {
                            Log.d("DEBUG","moveCommentList()")
                            var intent = Intent(view?.context,CommentActivity::class.java)
                            intent.putExtra("contentUid",contentUidList!![position])
                            startActivity(intent)
                        }
                    })
                }
                setUpListener(viewHolder)
            }

            fun setUpListener(viewHolder: CustomViewHolder) {
                var itemView = viewHolder.itemView
                itemView.postsviewitem_favorite_imageview.setOnClickListener(postsRecyclerViewAction)
                itemView.postsviewitem_user_image.setOnClickListener(postsRecyclerViewAction)
                itemView.postsviewitem_comment_imageview.setOnClickListener(postsRecyclerViewAction)
                itemView.postsviewitem_commentcounter_textview.setOnClickListener(postsRecyclerViewAction)
            }

            fun favoriteEvent(position: Int){
                // 내가 선택한 이미지의 정보 값
                var tsDoc = fireStore?.collection("contents")?.document(contentUidList[position])
                fireStore?.runTransaction{ transaction ->
                    var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                    if(contentDTO!!.favorites.containsKey(uid)){
                        // 좋아요 버튼이 눌렸을 경우
                        // 누르면 취소
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1   // 좋아요 count -1
                        contentDTO?.favorites.remove(uid)                           // uid 키 제거
                    }else{
                        // 좋아요 버튼이 눌리지 않았을 경우
                        // 누르면 활성
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount +1    // 좋아요 count+1
                        contentDTO?.favorites[uid!!] = true                         // uid 값 추가
                    }
                    transaction.set(tsDoc,contentDTO)   // transaction을 서버로 돌려준다.
                }
            }

        }
        // RecyclerView를 사용할 때 메모리를 적게 사용하기 위함

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 서버에서 넘어온 데이터들을 맵핑 시켜주는 부분
            Log.d("SNAPSHOT","Contents SNAP SHOT Size:"+contentDTOs.size)
            var viewHolder = (holder as CustomViewHolder)

            viewHolder.bindItems(viewHolder,position)
            viewHolder.setRecyclerViewAction(viewHolder,position)
        }

    }

    inner class PostFragmentTask constructor(
        var viewHolder: PostsFragment.PostsViewRecyclerViewAdapter.CustomViewHolder,
        var position: Int
    ) : AsyncTask<Any, Void, String>(){

        private var weakReference: WeakReference<PostsFragment>?=null

        init{
            this.weakReference = WeakReference(this@PostsFragment)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val fragment = weakReference?.get()
            if(fragment!=null && !fragment!!.isHidden){
                viewHolder.setIconImage(viewHolder,position)
                viewHolder.setContentUserImage(viewHolder,position)
                viewHolder.setFavoriteImage(position)
            }
        }

        override fun doInBackground(vararg params: Any?): String {
            return "PROFILE_DETAIL_CONTENT_BIND_SUCCESS"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel(true)
    }

}