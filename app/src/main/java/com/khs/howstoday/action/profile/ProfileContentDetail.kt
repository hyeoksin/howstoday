package com.khs.howstoday.action.profile

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.khs.howstoday.CommentActivity
import com.khs.howstoday.LoginActivity
import com.khs.howstoday.R
import com.khs.howstoday.model.ContentDTO
import com.khs.howstoday.action.profile.action.ProfileDetailRecyclerViewAction
import com.khs.howstoday.model.UserDTO
import kotlinx.android.synthetic.main.activity_profile_content_detail.*
import kotlinx.android.synthetic.main.profile_content_detail.view.*
import java.lang.ref.WeakReference

class ProfileContentDetail : AppCompatActivity() {

    var uid:String?=null
    var userEmail:String?=null
    var contentId:String?=null

    var auth:FirebaseAuth? =null
    var fireStore:FirebaseFirestore? = null
    var profileDetailRecyclerViewAction:ProfileDetailRecyclerViewAction?=null
    var task:ProfileDetailContentTask?=null

    init {
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_content_detail)
        initializeVariable()
        profile_content_detail_recyclerview.adapter = ProfileContentDetailRecyclerView()
        profile_content_detail_recyclerview.layoutManager = LinearLayoutManager(this)

    }

    private fun initializeVariable() {
        uid = intent.getStringExtra("destinationUid")
        userEmail = intent.getStringExtra("destinationUserEmail")
        contentId = intent.getStringExtra("contentId")
        setToolbar()
    }

    private fun setToolbar() {
        profile_content_toolbar_useremail.text = userEmail
        profile_content_detail_setting_back.setOnClickListener{onBackPressed()}
    }

    inner class ProfileContentDetailRecyclerView:RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        // 게시물 1개의 상세정보를 보여 줄 페이지
        var contentDetail:ContentDTO? = null

        init{
            loadData()
        }

        private fun loadData() {
            Log.d("ProfileContent","contentId: "+contentId)
            fireStore?.collection("contents")?.document(contentId!!)
                ?.addSnapshotListener{ documentSnapshot, firebaseFirestoreException ->
                    contentDetail = null
                    if(documentSnapshot==null) return@addSnapshotListener
                    contentDetail = documentSnapshot.toObject(ContentDTO::class.java)
                    notifyDataSetChanged() // 새로고침
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.profile_content_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            fun binditems(viewHolder: CustomViewHolder, position: Int) {
                var itemView = viewHolder.itemView
                // 게시물 이미지
                Glide.with(itemView.context).load(contentDetail?.contentImageUrl).into(itemView.profile_detail_imageview_content)
                // 프로필 이미지
//              setContentUserImage(viewHolder = viewHolder,position = position)
//              setFavoriteImage(viewHolder,position)
                Glide.with(itemView.context).load(R.drawable.icon_chat).override(120,120).into(itemView.profile_detail_talk_imageview)
                Glide.with(itemView.context).load(R.drawable.icon_comment).override(120,120).into(itemView.profile_detail_comment_imageview)
                task = ProfileDetailContentTask(viewHolder,position)
                task?.execute()

                itemView.profile_detail_user_location.text = "경상북도 안동시 용상동"
                itemView.profile_detail_user_email.text = userEmail
                itemView.profile_detail_favoritecounter_textview.text = "좋아요 "+contentDetail?.favoriteCount
                itemView.profile_detail_commentcounter_textview.text ="댓글 "+contentDetail?.commentCount
                itemView.profile_detail_explain_textview.text = contentDetail?.explain


            }

            fun setFavoriteImage(viewHolder: ProfileContentDetail.ProfileContentDetailRecyclerView.CustomViewHolder, position: Int) {
                var itemView = viewHolder.itemView
                if(contentDetail?.favorites!!.containsKey(uid)){ // uid는 내 uid
                    Glide.with(itemView.context).load(R.drawable.icon_heart_full).override(160,160).into(itemView.profile_detail_favorite_imageview)
//                    itemView.profile_detail_favorite_imageview.setImageResource(R.drawable.icon_heart_full)
                }else{
                    Glide.with(itemView.context).load(R.drawable.icon_heart_empty_bold).override(160,160).into(itemView.profile_detail_favorite_imageview)
//                    itemView.profile_detail_favorite_imageview.setImageResource(R.drawable.icon_heart_empty_bold)
                }
            }

            fun setContentUserImage(viewHolder: CustomViewHolder, position: Int) {
                var itemView = viewHolder.itemView
                // 데이터베이스에서 uri를 가져오기
                var docRef = fireStore
                    ?.collection("users")
                    ?.document(contentDetail?.userId.toString())
                    ?.get()

                docRef?.addOnSuccessListener {
                    Glide.with(itemView.context)
                        .load(it.get("userImage").toString())
                        .error(R.drawable.icon_profile)
                        .placeholder(R.drawable.icon_profile)
                        .apply(RequestOptions().circleCrop())
                        .into(itemView.profile_detail_user_image)
                }?.addOnFailureListener {
                    Glide.with(itemView.context)
                        .load(R.drawable.icon_profile)
                        .error(R.drawable.icon_profile)
                        .placeholder(R.drawable.icon_profile)
                        .apply(RequestOptions().circleCrop())
                        .into(itemView.profile_detail_user_image)
                    Log.d(" DEBUG","Failure")
                }
            }

            fun setRecyclerViewAction(viewHolder: CustomViewHolder, position: Int) {
                profileDetailRecyclerViewAction = ProfileDetailRecyclerViewAction().apply{
                    addProfileDetailRecyclerViewActionInterface(object : ProfileDetailRecyclerViewAction.ProfileDetailRecyclerViewActionInterface {
                        override fun favorite() {
                            favoriteEvent(position)
                        }

                        override fun moveCommentList() {
                            Log.d("DEBUG","moveCommentList()")
                            var intent = Intent(this@ProfileContentDetail, CommentActivity::class.java)
                            intent.putExtra("contentUid",contentId)
                            startActivity(intent)
                        }
                    })
                }
                setupListener(viewHolder,position)
            }

            fun favoriteEvent(position: Int){
                // 내가 선택한 이미지의 정보 값
                var tsDoc = fireStore?.collection("contents")?.document(contentId!!)
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

            fun setupListener(viewHolder: ProfileContentDetail.ProfileContentDetailRecyclerView.CustomViewHolder, position: Int) {
                viewHolder.itemView.profile_detail_favorite_imageview.setOnClickListener(profileDetailRecyclerViewAction)
                viewHolder.itemView.profile_detail_commentcounter_textview.setOnClickListener(profileDetailRecyclerViewAction)
                viewHolder.itemView.profile_detail_comment_imageview.setOnClickListener(profileDetailRecyclerViewAction)
            }
        }

        override fun getItemCount(): Int {
            return 1
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder)
            viewHolder.binditems(viewHolder,position)
            viewHolder.setRecyclerViewAction(viewHolder,position)
        }
    }

    inner class ProfileDetailContentTask constructor(
        var viewHolder: ProfileContentDetailRecyclerView.CustomViewHolder,
        var position: Int
    ) : AsyncTask<Any, Void, String>(){

        private var weakReference: WeakReference<ProfileContentDetail>?=null

        init{
            this.weakReference = WeakReference(this@ProfileContentDetail)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // 응답이 오기전에 사용자가 프래그먼트를 종료할 수도 있음
            val activity = weakReference?.get()
            if(activity!=null && !activity!!.isFinishing){
                viewHolder.setContentUserImage(viewHolder,position)
            }
        }

        override fun onPostExecute(result: String?) {
            viewHolder.setFavoriteImage(viewHolder,position) // 일단 여기로..
            super.onPostExecute(result)
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
