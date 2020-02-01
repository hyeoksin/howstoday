package com.khs.howstoday

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.khs.howstoday.action.CommentActivityAction
import com.khs.howstoday.action.CommentRecyclerViewAction
import com.khs.howstoday.model.ContentDTO
import com.khs.howstoday.model.IntentCode
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.comment_detail.view.*
import java.lang.ref.WeakReference

class CommentActivity : AppCompatActivity() {

    var context:MainActivity?=null
    var contentUid: String? = null
    var commentRecyclerViewAction: CommentRecyclerViewAction? = null
    var commentActivityAction: CommentActivityAction? = null
    var auth: FirebaseAuth? = null
    var fireStore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        initializeVariable()
//      setCommentUserImage()  // 백그라운드로 실행
        setUpListener()
        comment_recyclerview.adapter = CommentRecyclerViewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)
    }

    private fun initializeVariable() {
        contentUid = intent.getStringExtra("contentUid")
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    private fun setUpListener() {
        commentActivityAction = CommentActivityAction().apply {
            addCommentActivityActionInterface(object :
                CommentActivityAction.CommentActivityActionInterface {
                override fun send() {
                    setComment()
                }

                override fun back() {
                    onBackPressed()
                }
            })
        }
        comment_btn_send.setOnClickListener(commentActivityAction)
        comment_setting_back.setOnClickListener(commentActivityAction)
    }

    private fun setComment() {
        var comment = ContentDTO.Comment()                          // 댓글 객체
        comment.userId = auth?.currentUser?.email                   // 작성자 이메일
        comment.uid = auth?.currentUser?.uid                        // 작성자 Uid
        comment.comment = comment_edit_message.text.toString()      // 댓글 내용
        comment.timestamp = System.currentTimeMillis()              // 작성 시각
        setCurrentUserImage(comment) // 댓글 작성자의 이미지
        comment_edit_message.setText("")
    }

    private fun setCurrentUserImage(comment: ContentDTO.Comment) {
        var docRef = fireStore
            ?.collection("users")
            ?.document(auth?.currentUser?.email.toString())
            ?.get()?.addOnSuccessListener {
                comment.userImageUrl = it.get("userImage").toString()       // 유저 이미지
                saveComment(comment) // 댓글 저장
            }?.addOnFailureListener {
                saveComment(comment) // 댓글 저장
            }
    }

    private fun saveComment(comment: ContentDTO.Comment) {

        // 게시글 댓글 개수 업데이트
        var tsDoc = fireStore?.collection("contents")?.document(contentUid!!)
        fireStore?.runTransaction { transaction ->
            var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
            contentDTO!!.commentCount = contentDTO?.commentCount!! + 1
            transaction.set(tsDoc, contentDTO)   // transaction을 서버로 돌려준다.
        }

        fireStore?.collection("contents")// 게시글 디렉토리
            ?.document(contentUid!!)                  // 게시글 uid (*사용자 uid랑 다름)
            ?.collection("comments")    //  댓글 디렉토리
            ?.document()                              // 댓글 uid
            ?.set(comment)
    }

    private fun setCommentUserImage() {
        var docRef = fireStore
            ?.collection("users")
            ?.document(auth?.currentUser?.email.toString())
            ?.get()

        docRef?.addOnSuccessListener {
            Glide.with(this)
                .load(it.get("userImage").toString())
                .error(R.drawable.icon_profile)
                .placeholder(R.drawable.icon_profile)
                .apply(RequestOptions().circleCrop())
                .into(comment_current_userimage)
        }?.addOnFailureListener {
            Glide.with(this)
                .load(R.drawable.icon_profile)
                .error(R.drawable.icon_profile)
                .placeholder(R.drawable.icon_profile)
                .apply(RequestOptions().circleCrop())
                .into(comment_current_userimage)
            Log.d(" DEBUG", "Failure")
        }
    }

    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            Log.d("SNAPSHOT", "Comments SNAP SHOT START")
            fireStore?.collection("contents")
                ?.document(contentUid!!) // 게시물 Uid
                ?.collection("comments")                        // comments 디렉토리 내에
                ?.orderBy("timestamp", Query.Direction.DESCENDING)      // 필드 중 timestamp르 정렬해서
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear() // 값이 중복으로 쌓일 수 있기 때문에 clear
                    if (querySnapshot == null) {
                        Log.d("SNAPSHOT", "Comments SNAP SHOT is null")
                        return@addSnapshotListener
                    }
                    for (snapShot in querySnapshot.documents) {
                        comments.add(snapShot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged() // 새로고침
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.comment_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bindItems(viewHolder: CustomViewHolder, comment: ContentDTO.Comment) {
                itemView.commentviewitem_comment.text = comment.comment
                itemView.commentviewitem_userid.text = comment.userId
                itemView.commentviewitem_userlocation.text = "서울특별시 중랑구 상봉동"
                var task = ConmmentActivityTask(viewHolder,comment).execute()
                setUserImage(viewHolder,comment)

                task.cancel(true)
            }

            fun setUserImage(viewHolder: CustomViewHolder, comment: ContentDTO.Comment) {
                if (comment?.userImageUrl == "null") {
                    Glide.with(itemView.context)
                        ?.load(R.drawable.icon_profile)
                        ?.apply(RequestOptions().circleCrop())
                        ?.placeholder(R.drawable.icon_profile)
                        ?.into(itemView.commentviewitem_userimage)
                } else {
                    Glide.with(itemView.context)
                        ?.load(comment.userImageUrl)
                        ?.apply(RequestOptions().circleCrop())
                        ?.placeholder(R.drawable.icon_profile)
                        ?.into(itemView.commentviewitem_userimage)
                    // 댓글 작성자 이미지가 있을 경우
                }
            }

            fun setupRecyclerViewAction(position: Int) {
                // 스냅샷을 찍고, position 값이 필요 하기 때문에 onBindViewHoler() 메소드에서 작성
                commentRecyclerViewAction = CommentRecyclerViewAction().apply {
                    addCommentRecyclerViewActionInterface(object : CommentRecyclerViewAction.CommentRecyclerViewActionInterface {
                        override fun favoriteEvent() { }
                        override fun moveUserProfile() {
                            // 구현이 완벽하지 않음, 보완이 필요함
                            startActivity(Intent(this@CommentActivity,MainActivity::class.java).apply {
                                putExtra("INTENT_CODE", IntentCode().MOVE_PROFILE_FRAGMENT_FROM_CONTENTS)
                                putExtra("destinationUid",comments!![position].uid)
                                putExtra("destinationUserEmail",comments!![position].userId)
                            })
                        }
                    })
                }
            }

            fun setupListener(viewHolder: CommentActivity.CommentRecyclerViewAdapter.CustomViewHolder) {
                viewHolder.itemView.commentviewitem_userimage.setOnClickListener(commentRecyclerViewAction)
            }

        }

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            Log.d("SNAPSHOT", "Comments SNAP SHOT Size:" + comments.size)
            // 화면을 넘길 때 마다 스냅샷이 찍힌다.
            // 스냅샷의 갯수는 화면에 들어온 콘텐츠의 갯수
            var viewHolder = (holder as CustomViewHolder)
            viewHolder.bindItems(viewHolder,comments!![position])
            viewHolder.setupRecyclerViewAction(position)
            viewHolder.setupListener(viewHolder)

        }

        inner class ConmmentActivityTask constructor(
            var viewHolder: CustomViewHolder,
            var comment: ContentDTO.Comment
        ) : AsyncTask<Any, Void, String>(){

            private var weakReference: WeakReference<CommentActivity>?=null

            init{
                this.weakReference = WeakReference(this@CommentActivity)
            }

            override fun onPreExecute() {
                super.onPreExecute()
                // 응답이 오기전에 사용자가 프래그먼트를 종료할 수도 있음
                val activity = weakReference?.get()
                if(activity!=null && !activity!!.isFinishing){
                    viewHolder.setUserImage(viewHolder,comment)
                    setCommentUserImage()
                }
            }


            override fun doInBackground(vararg params: Any?): String {
                return "COMMENT_ACTIVITIY_TASK_OK"
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
            }


        }

    }

}
