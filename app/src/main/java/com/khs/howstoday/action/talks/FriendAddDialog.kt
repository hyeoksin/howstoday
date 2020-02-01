package com.khs.howstoday.action.talks

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.khs.howstoday.CommentActivity
import com.khs.howstoday.R
import com.khs.howstoday.action.talks.action.FriendAddDialogAction
import com.khs.howstoday.model.ContentDTO
import com.khs.howstoday.model.CrushUserDTO
import com.khs.howstoday.model.FriendDTO
import com.khs.howstoday.model.UserDTO
import kotlinx.android.synthetic.main.dialog_talks_friend_add.*
import java.lang.ref.WeakReference

class FriendAddDialog :DialogFragment(){

    private var dialogView:View?=null
    private var friendAddDialogAction: FriendAddDialogAction? = null
    var task:FriendAddDialogTask?=null

    var fireStore:FirebaseFirestore?=null
    var friendUserEmail:String? = null
    var friendUserUid:String? = null
    var auth:FirebaseAuth?=null

    init {
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogView =  inflater.inflate(R.layout.dialog_talks_friend_add,container,false)
        setBackgroundUI()
        InitializeVariable()
        return dialogView
    }

    private fun InitializeVariable() {
        friendUserEmail = arguments?.getString("friendUserEmail")
        friendUserUid = arguments?.getString("friendUserUid")
    }

    private fun setBackgroundUI() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        task = FriendAddDialogTask()
        task?.execute()
        setTalksFriendAddDialogAction()
        setDialogText()
    }

    private fun setDialogText() {
        talks_friend_add_useremail.text = friendUserEmail
    }

    private fun setTalksFriendAddDialogAction() {
        friendAddDialogAction = FriendAddDialogAction().apply {
            addFriendAddDialogActionInterface(object : FriendAddDialogAction.FriendAddDialogActionInterface {
                override fun add() {
                    saveFriendInfo()
                }
                override fun cancel() {
                    dismiss()
                }
            })
        }
        setupListener()
    }

    private fun saveFriendInfo() {
        // 내 계정에 저장
        var tsDocFriendTo = fireStore?.collection("users")?.document(auth?.currentUser?.email!!)
        fireStore?.runTransaction { transaction ->
            var userDTO = transaction.get(tsDocFriendTo!!).toObject(UserDTO::class.java)
            var friendDTO = (userDTO?.friend as FriendDTO)
            if(friendDTO.friendToCount == 0){
                // 내가 친구로 추가한 사람이 없다면
                friendDTO.friendToCount = 1
                friendDTO.friendToUser[friendUserEmail!!] = true
                userDTO.friend = friendDTO
                transaction.set(tsDocFriendTo,userDTO)
                return@runTransaction
            }
            // 이미 등록한 친구라면
            if(friendDTO.friendToUser.containsKey(friendUserEmail)){
//              Toast.makeText(context,"이미 등록한 친구입니다.",Toast.LENGTH_SHORT).show()
                Snackbar.make(dialogView!!,"이미 등록한 친구입니다.",Snackbar.LENGTH_SHORT).show()
                return@runTransaction
            }else{
                // 새로운 친구 등록
                friendDTO.friendToCount = friendDTO.friendToCount + 1
                friendDTO.friendToUser[friendUserEmail!!] = true
//              Toast.makeText(context,"새로운 친구를 등록했습니다.",Toast.LENGTH_SHORT).show()
                Snackbar.make(dialogView!!,"새로운 친구를 등록했습니다",Snackbar.LENGTH_SHORT).show()
            }
            userDTO.friend = friendDTO
            transaction.set(tsDocFriendTo,userDTO)
            return@runTransaction
        }

        // 상대 계정에 정보를 저장
        var tsDocCrushFrom = fireStore?.collection("users")?.document(friendUserEmail!!)
        fireStore?.runTransaction { transaction ->
            var userDTO = transaction.get(tsDocCrushFrom!!).toObject(UserDTO::class.java)
            var friendDTO = (userDTO?.friend as FriendDTO)
            if(friendDTO.friendFromCount == 0){
                // 나를 친구로 추가한 사람이 없다면
                friendDTO = FriendDTO()
                friendDTO!!.friendFromCount = 1
                friendDTO!!.friendFromUser[auth?.currentUser?.email!!] = true
                userDTO.friend = friendDTO
                transaction.set(tsDocCrushFrom,userDTO)
                return@runTransaction
            }
            // 이미 등록한 친구라면
            if(friendDTO?.friendFromUser!!.containsKey(auth?.currentUser?.email))
                return@runTransaction
            else{
                // 새로운 친구 등록
                friendDTO?.friendFromCount = friendDTO?.friendFromCount!! + 1
                friendDTO.friendFromUser[auth?.currentUser?.email!!] = true
            }
            userDTO.friend = friendDTO
            transaction.set(tsDocCrushFrom,userDTO!!)
        }
        dismiss()
    }

    private fun setupListener() {
        talks_friend_btn_add_yes.setOnClickListener(friendAddDialogAction)
        talks_friend_btn_add_no.setOnClickListener(friendAddDialogAction)
    }

    inner class FriendAddDialogTask constructor() :  AsyncTask<Any, Void, String>(){

        private var weakReference: WeakReference<FriendAddDialog>?=null

        init{
            this.weakReference = WeakReference(this@FriendAddDialog)
        }

        fun loadImage(){
            var docRef = fireStore?.collection("users")?.document(friendUserEmail!!)?.get()
            docRef?.addOnCompleteListener{ task ->
                var snapshot = task.getResult()
                snapshot?.getString("userImage")?.let {
                    Log.d("Talks","it "+it)
                    Glide.with(this@FriendAddDialog)
                        .load(it)
                        .apply(RequestOptions().circleCrop())
                        .override(250,250)
                        .into(talks_friend_add_userimage)
                }
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val dialog = weakReference?.get()
            if(dialog!=null && !dialog!!.isRemoving)
                loadImage()

        }

        override fun doInBackground(vararg params: Any?): String {
            return "COMMENT_ACTIVITIY_TASK_OK"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel(true)
    }
}