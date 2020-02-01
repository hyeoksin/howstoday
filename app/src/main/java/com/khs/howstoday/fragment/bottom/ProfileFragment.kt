package com.khs.howstoday.fragment.bottom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.khs.howstoday.AddContentsActivity
import com.khs.howstoday.MainActivity
import com.khs.howstoday.action.profile.ProfileContentDetail
import com.khs.howstoday.R
import com.khs.howstoday.action.profile.action.ProfileFragmentAction
import com.khs.howstoday.action.profile.action.ProfileRecyclerViewAction
import com.khs.howstoday.model.ContentDTO
import com.khs.howstoday.model.CrushUserDTO
import com.khs.howstoday.model.RequestCode
import com.khs.howstoday.model.UserDTO
import com.khs.howstoday.util.PermissionUtil
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.main_toolbar.*
import java.lang.ref.WeakReference


class ProfileFragment : Fragment() {

    var fragmentView: View? = null
    var profileFragmentAction: ProfileFragmentAction? = null
    var mainActivity: MainActivity? = null
    var profileRecyclerViewAction: ProfileRecyclerViewAction? =null
    var task:ProfileFragmentTask? =null

    var firestore: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null              // 파이어베이스 스토리지

    var destinationUid: String? = null                 // 넘어온 Uid
    var destinationUserEmail: String? = null            // 넘어온 Email
    var currentUserUid: String? = null                   // 현재 유저 Uid
    var currentUserEmail: String? = null                 // 현재 유저 email
    var auth: FirebaseAuth? = null

    init {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkGalleryPermission()
        initializeVariable()
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false)
        fragmentView?.account_recycler_view?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recycler_view?.layoutManager = GridLayoutManager(activity!!, 3)
        Log.d("ProfileFragment", "onCreateView() - destinationUid: " + destinationUid)
        return fragmentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
/*        task = ProfileFragmentTask(destinationUserEmail)
        task?.execute()*/
        setFragmentAction()
        setVisibility()
    }

    private fun checkGalleryPermission(): Boolean {
        if (PermissionUtil().requestPermission(
                activity!!,
                RequestCode().PICK_IMAGE_FROM_ALBUM,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) return true
        return false
    }

    private fun initializeVariable() {
        currentUserEmail = auth?.currentUser?.email                                 // 현재 이메일
        currentUserUid = auth?.currentUser?.uid                                     // 현재 Uid
        destinationUid = arguments?.getString("destinationUid")                // 넘어온 Uid
        destinationUserEmail = arguments?.getString("destinationUserEmail")    // 넘어온 email
        mainActivity = (activity as MainActivity)
    }


    private fun setVisibility() {
        if (destinationUid == currentUserUid) {                        // 내 프로필
            fragmentView?.user_btn_upload?.show()// 사진 업로드 버튼
            fragmentView?.profile_btn_detail?.text = "프로필수정"       // 프로필 수정 버튼
            fragmentView?.profile_btn_crush?.visibility = View.GONE
            destinationUserEmail=currentUserEmail
        } else {                                                       // 상대방 프로필
            fragmentView?.user_btn_upload?.hide()    // 사진 업로드 버튼
            fragmentView?.profile_btn_detail?.text = "프로필보기"       // 프로필 보기 버튼
            setMainToolbar()
        }
        getCrushCount(destinationUserEmail)
        getProfileImage(destinationUserEmail)
    }

    private fun setMainToolbar() {
        mainActivity?.main_toolbar_useremail?.text = destinationUserEmail       // 메인툴바 이메일 텍스트
        mainActivity?.logo_main?.visibility = View.GONE                         // 메인툴바 로고 삭제
        mainActivity?.main_toolbar_useremail?.visibility = View.VISIBLE         // 메인툴바 이메일 보이기
        mainActivity?.main_toolbar_back?.visibility = View.VISIBLE              // 메인툴바 뒤로가기 보이기
    }

    private fun setFragmentAction() {
        profileFragmentAction = ProfileFragmentAction().apply {
            addProfileFragmentActionInterface(object :
                ProfileFragmentAction.ProfileFragmentActionInterface {
                override fun crush() {
                    requestCrush()
                }
                override fun profileDetail() {}

                override fun uploadImage() {
                    if (ContextCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) startActivity(Intent(activity, AddContentsActivity::class.java))
                    else {
                        checkGalleryPermission()
                        Toast.makeText(context!!, "권한 사용에 동의하셔야 이용이 가능합니다.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun editUserImage() {
                    // 액티비티 작성 없이 바로 업로드
                    // Request는 MainActivity로 전달
                    if (destinationUid == currentUserUid
                        && ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) openGallery()
                    else {
                        checkGalleryPermission()
                        if(destinationUid!=currentUserUid) Toast.makeText(context!!, "프로필수정은 본인만 가능합니다.", Toast.LENGTH_LONG).show()
                        else Toast.makeText(context!!, "권한 사용에 동의하셔야 이용이 가능합니다.", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
        setupListener()
    }

    fun getCrushCount(userEmail:String?){
        Log.d("Profile","getCrushCount() - "+userEmail)
        firestore?.collection("users")?.document(userEmail!!)?.addSnapshotListener{
            documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot==null) return@addSnapshotListener
            var userDTO = documentSnapshot.toObject(UserDTO::class.java)
            if(userDTO == null) // 데이터베이스에 회원으로 등록되어있지 않은 경우
                return@addSnapshotListener
            var crushDTO = (userDTO.crush as CrushUserDTO)
            crushDTO?.let {
                fragmentView?.account_tv_crush_count?.text = crushDTO?.crushToCount?.toString()
            }
            crushDTO?.crushedFromCount.let{
                fragmentView?.account_tv_crushed_count?.text = crushDTO?.crushedFromCount?.toString()
                if(crushDTO?.crushedFromUser?.containsKey(currentUserEmail)!!){
                    Log.d("ProfileFragment", "- getCrushCount: contains " + userEmail)
                    fragmentView?.profile_btn_crush?.text = "호감취소"
                    fragmentView?.profile_btn_crush?.background?.setColorFilter(ContextCompat.getColor(activity!!,R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                }else{
                    fragmentView?.profile_btn_crush?.text = "호감있음"
                    Log.d("ProfileFragment", "- getCrushCount: notcontains " + userEmail)
                    fragmentView?.profile_btn_crush?.background?.colorFilter = null
                }
            }
        }
    }

    fun requestCrush(){
        // 현재 유저 계정 크러쉬 디렉토리
        var tsDocCrush = firestore?.collection("users")?.document(currentUserEmail!!)
        // 내 계정에 정보를 저장
        firestore?.runTransaction { transaction ->
            var userDTO = transaction.get(tsDocCrush!!).toObject(UserDTO::class.java)
            var crushDTO = (userDTO?.crush as CrushUserDTO)
            if(crushDTO.crushToCount == 0){
                // 내가 크러쉬하는 사람이 없다면
                crushDTO!!.crushToCount = 1
                crushDTO!!.crushToUser[destinationUserEmail!!] = true
                userDTO.crush = crushDTO
                transaction.set(tsDocCrush,userDTO)
                return@runTransaction
            }

            if(crushDTO.crushToUser.containsKey(destinationUserEmail)){
                crushDTO.crushToCount = crushDTO.crushToCount - 1
                crushDTO.crushToUser?.remove(destinationUserEmail)
            }else{
                crushDTO.crushToCount = crushDTO.crushToCount + 1
                crushDTO.crushToUser[destinationUserEmail!!] = true
            }
            userDTO.crush = crushDTO
            transaction.set(tsDocCrush,userDTO)
            return@runTransaction
        }

        // 상대방 정보에 데이터를 저장
        var tsDocCrushFrom = firestore?.collection("users")?.document(destinationUserEmail!!)
        firestore?.runTransaction {transaction ->
            var userDTO = transaction.get(tsDocCrushFrom!!).toObject(UserDTO::class.java)
            var crushedDTO = (userDTO?.crush as CrushUserDTO)
            if(crushedDTO.crushedFromCount==0){
                crushedDTO!!.crushedFromCount = 1
                crushedDTO!!.crushedFromUser[currentUserEmail!!] = true
                userDTO.crush = crushedDTO
                transaction.set(tsDocCrushFrom,userDTO)
                return@runTransaction
            }
            if(crushedDTO.crushedFromUser.containsKey(currentUserEmail)){
                crushedDTO.crushedFromCount = crushedDTO.crushedFromCount - 1
                crushedDTO.crushedFromUser?.remove(currentUserEmail)
            }else{
                crushedDTO.crushedFromCount = crushedDTO.crushedFromCount + 1
                crushedDTO.crushedFromUser[currentUserEmail!!] = true
            }
            userDTO.crush = crushedDTO
            transaction.set(tsDocCrushFrom,userDTO)
            return@runTransaction
        }
    }

    private fun setupListener() {
        user_btn_upload.setOnClickListener(profileFragmentAction)
        user_image.setOnClickListener(profileFragmentAction)
        profile_btn_crush.setOnClickListener(profileFragmentAction)
    }

    private fun openGallery() {
        // 앨범 열기
        // 프래그먼트에서 ActivityResult를 실행하려면 앞에 activity?를 써야함.
        activity?.startActivityForResult(Intent(Intent.ACTION_PICK).apply { setType("image/*") }, RequestCode().USER_PICK_IMAGE_FROM_ALBUM)
    }

    private fun getProfileImage(userEmail: String?) {
        Log.d("ProfileFragment", "getProfileImage() - userEmail: "+userEmail)
        var doct =firestore?.collection("users")?.document(userEmail!!)?.get()
        doct?.addOnCompleteListener {
            var snapshot = it.getResult()
            snapshot?.let{
                Glide.with(activity!!).load(it.getString("userImage"))
                    .apply(RequestOptions().circleCrop())
                    .into(fragmentView!!.user_image)
                return@addOnCompleteListener
            }
            Glide.with(activity!!).load(R.drawable.icon_profile)
                .apply(RequestOptions().circleCrop())
                .into(fragmentView!!.user_image)
        }
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidLIst:ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("contents")?.whereEqualTo("uid", destinationUid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidLIst.clear()
                    if (querySnapshot == null) {
                        return@addSnapshotListener
                    }
                    for (snapShot in querySnapshot.documents) {
                        if(!snapShot.metadata.isFromCache) {
                            contentDTOs.add(snapShot.toObject(ContentDTO::class.java)!!)
                            contentUidLIst.add(snapShot.id)
                        }
                    }
                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                    Log.d("DEBUG", "UserFragmentRecyclerViewAdapter() - Contents Size: " + contentDTOs.size)
                    notifyDataSetChanged() // 새로고침
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3             // 화면폭의 1/3의 값
            var imageView = ImageView(parent.context)                            //  view의 context를 가져온다
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width) // layout 셋팅
            return CustomViewHolder(imageView)

        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {

            fun binditems(viewHolder: CustomViewHolder, position: Int) {
                var imageView = viewHolder.imageView
                Glide.with(imageView.context)
                    .load(contentDTOs[position].contentImageUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(imageView)
            }

            fun setRecyclerViewAction(viewHolder: CustomViewHolder, position: Int) {
                profileRecyclerViewAction = ProfileRecyclerViewAction()
                    .apply {
                    addProfileRecyclerViewActionInterface(object : ProfileRecyclerViewAction.ProfileRecyclerViewActionInterface {
                        override fun contentImageDetail() {
                            startActivity(Intent(activity, ProfileContentDetail::class.java).apply{
                                Log.d("Profile","ImageView id: ")
                                putExtra("destinationUid",destinationUid)
                                putExtra("destinationUserEmail",destinationUserEmail)
                                putExtra("contentId",contentUidLIst[position])
                            })
                        }
                    })
                }
                setUpListener(viewHolder)
            }

            fun setUpListener(viewHolder: CustomViewHolder) {
                var imageView = viewHolder.imageView
                imageView.setOnClickListener(profileRecyclerViewAction)
            }

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder)//inner클래스의 image view를 쓸려면 var imageView로 작성해야한다.
            viewHolder.binditems(viewHolder,position)
            viewHolder.setRecyclerViewAction(viewHolder,position)

        }
    }

    inner class ProfileFragmentTask constructor(
        val userEmail:String?=null
    ) : AsyncTask<Any, Void, String>(){

        private var weakReference: WeakReference<ProfileFragment>?=null

        init{
            this.weakReference = WeakReference(this@ProfileFragment)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val fragment = weakReference?.get()
            if(fragment!=null && !fragment!!.isRemoving){
                setVisibility()
            }
        }

        override fun doInBackground(vararg params: Any?): String {
            return "PROFILE_FRAGMENT_TASK_RETURN"
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onPause() {
        super.onPause()
        task?.cancel(true)
    }




}