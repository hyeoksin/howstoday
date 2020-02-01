package com.khs.howstoday.fragment.bottom.talks


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.data.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.khs.howstoday.MainActivity

import com.khs.howstoday.R
import com.khs.howstoday.action.talks.FriendAddDialog
import com.khs.howstoday.action.talks.action.FriendFragmentAction
import com.khs.howstoday.action.talks.action.FriendFragmentRecyclerViewAction
import com.khs.howstoday.fragment.bottom.PostsFragment
import com.khs.howstoday.fragment.bottom.ProfileFragment
import com.khs.howstoday.model.FriendDTO
import com.khs.howstoday.model.IntentCode
import com.khs.howstoday.model.UserDTO
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_friend.*
import kotlinx.android.synthetic.main.friend_list_detail.*
import kotlinx.android.synthetic.main.friend_list_detail.view.*

/**
 * A simple [Fragment] subclass.
 */
class FriendFragment : Fragment() {

    /*
        companion object{
            fun newInstance():FriendFragment{
                return FriendFragment()
            }
        }
    */
    var fragmentView: View? = null
    var friendFragmentAction: FriendFragmentAction? = null
    var friendAddDialog: FriendAddDialog? = null
    var task: FriendFragmentTask? = null

    var user: FirebaseUser? = null
    var auth: FirebaseAuth? = null
    var fireStore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null

    var REGISTERED_USER_EMAIL_CHECK = "false"


    init {
        auth = FirebaseAuth.getInstance()               // 유저 인증 객체
        user = auth?.currentUser                        // 인증된 유저의 정보
        fireStore = FirebaseFirestore.getInstance()     // 데이터베이스 객체
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Talks", "FriendFragment() onCreateView()")
        fragmentView = inflater.inflate(R.layout.fragment_friend, container, false)
        var recyclerview = fragmentView?.findViewById<RecyclerView>(R.id.talks_friends_recyclerview)
        recyclerview?.adapter = FriendFragmentRecyclerViewAdpater()
        recyclerview?.layoutManager = LinearLayoutManager(activity)
        return fragmentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setFriendFragmentAction()
    }

    private fun setFriendFragmentAction() {
        friendFragmentAction = FriendFragmentAction().apply {
            addFriendFragmentActionInterface(object :
                FriendFragmentAction.FriendFragmentActionInterface {
                override fun search() {
                    if (checkEmail() != false) {
                        // 존재하는 계정인지 확인
                        Snackbar.make(fragmentView!!, "계정을 검색하고 있습니다.", Snackbar.LENGTH_SHORT)
                            .show()
                        var friendUserEmail = talks_friend_searchedit.text.toString()
                        var tsDocCrush = fireStore?.collection("users")?.document(friendUserEmail)
                        tsDocCrush?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                            if (firebaseFirestoreException != null || querySnapshot == null) return@addSnapshotListener
                            var userDTO = querySnapshot?.toObject(UserDTO::class.java)
                            if (userDTO == null) {
                                Snackbar.make(
                                    fragmentView!!,
                                    "계정이 존재하지 않습니다.",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                return@addSnapshotListener
                            }
                            showFriendAddDialog(friendUserEmail)
                        }
                    }
                }

                private fun showFriendAddDialog(friendUserEmail: kotlin.String) {
                    friendAddDialog = FriendAddDialog()
                    var args = Bundle()
                    fireStore?.collection("users")?.document(friendUserEmail)?.get()
                        ?.addOnCompleteListener {
                            var snapshot = it.getResult()
                            args.putString("friendUserUid", snapshot?.getString("uid"))
                            args.putString("friendUserEmail", snapshot?.getString("userEmail"))
                            friendAddDialog?.arguments = args
                            friendAddDialog?.show(fragmentManager!!, "")
                        }
                }

                private fun checkEmail(): Boolean {
                    var inputEmail = talks_friend_searchedit.text.toString()
                    // 2) 이메일이 입력되지 않았다면 예외처리
                    if (inputEmail.isEmpty()) {
                        Snackbar.make(fragmentView!!, "이메일을 입력해주세요.", Snackbar.LENGTH_SHORT).show()
                        return false
                    }
                    // 3) 자기자신을 친구로 등록할 수 없기때문에 FirebaseUser의 email이 입력한 이메일과 같다면, 자기자신은 등록 불가 메시지를 띄운다.
                    if (inputEmail == user?.email) {
                        Snackbar.make(
                            fragmentView!!,
                            "자기자신은 친구로 등록할 수 없습니다.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        return false
                    }
                    return true
                }
            })
        }
        setupListener()
    }

    private fun setupListener() {
        talks_friend_searchbtn.setOnClickListener(friendFragmentAction)
    }

    fun toggleSearchBar() {
        if (talks_friend_search_area.visibility == View.VISIBLE) {
            talks_friend_search_area.visibility == View.GONE
        } else talks_friend_search_area.visibility = View.VISIBLE
    }


    inner class FriendFragmentRecyclerViewAdpater : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var friendList: ArrayList<UserDTO> = arrayListOf()
        var friendFragmentRecyclerViewAction: FriendFragmentRecyclerViewAction? = null

        init {
            fireStore?.collection("users")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    friendList.clear()
                    Log.d("FriendFragment","querySnapsht: "+querySnapshot?.documents?.size)
                    if (querySnapshot == null) return@addSnapshotListener
                    for(snapshot in querySnapshot.documents){
                        if(!snapshot.metadata.isFromCache) {
                            var userDTO = (snapshot.toObject(UserDTO::class.java)!!)
                            if (userDTO.friend!!.friendFromUser[auth?.currentUser?.email] == true)
                                friendList.add(userDTO)
                        }
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.friend_list_detail, parent, false)
            Log.d("FriendFragment", "onCreateViewHolder")
            return CustomViewHolder(view)
        }

        override fun getItemCount(): Int {
            return friendList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder)
            Log.d("FriendFragment", "Current User Email: " + auth?.currentUser?.email)
            Log.d("FriendFragment", "friendList.size: " + friendList.size)
            viewHolder.bindItems(viewHolder, position)
            viewHolder.setRecyclerAction(viewHolder, position)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            fun bindItems(
                viewHolder: FriendFragment.FriendFragmentRecyclerViewAdpater.CustomViewHolder,
                position: Int
            ) {
                var itemView = viewHolder.itemView
                itemView.friendlist_detail_userId.text = friendList!![position].userEmail
                task = FriendFragmentTask(viewHolder, position)
                task?.execute()
            }

            fun setUserImage(itemView: View, position: Int) {
                Glide.with(itemView)
                    .load(friendList[position].userImage)
                    .apply(RequestOptions().circleCrop())
                    .into(itemView.friendlist_detail_userimage)
            }

            fun setRecyclerAction(
                viewHolder: FriendFragment.FriendFragmentRecyclerViewAdpater.CustomViewHolder,
                position: Int
            ) {
                friendFragmentRecyclerViewAction = FriendFragmentRecyclerViewAction().apply {
                    addFriendFragmentRecyclerViewActionInterface(object :
                        FriendFragmentRecyclerViewAction.FriendFragmentRecyclerViewActionInterface {
                        override fun moveProfile() {
                            var fragment = ProfileFragment()
                            var bundle = Bundle()
                            bundle.putString("destinationUid", friendList!![position].uid)
                            bundle.putString("destinationUserEmail", friendList!![position].userEmail)
                            fragment?.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.main_content, fragment)?.commit()
                        }

                        override fun openTalk() {

                        }
                    })
                }
                setupRecyclerViewListener(viewHolder)
            }

            private fun setupRecyclerViewListener(viewHolder: FriendFragment.FriendFragmentRecyclerViewAdpater.CustomViewHolder) {
                var itemView = viewHolder.itemView
                itemView.friendlist_detail_layout.setOnClickListener(friendFragmentRecyclerViewAction)

            }
        }

    }

    inner class FriendFragmentTask constructor(
        var viewHolder: FriendFragmentRecyclerViewAdpater.CustomViewHolder,
        var position: Int
    ) : AsyncTask<Any, Void, String>() {

        private var weakReference: WeakReference<FriendFragment>? = null

        init {
            this.weakReference = WeakReference(this@FriendFragment)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val fragment = weakReference?.get()
            if (fragment != null && !fragment!!.isRemoving) {
                viewHolder.setUserImage(viewHolder.itemView,position)
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
