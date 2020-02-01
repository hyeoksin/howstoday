package com.khs.howstoday

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.khs.howstoday.action.BottomNavigation
import com.khs.howstoday.fragment.bottom.*
import com.khs.howstoday.model.CrushUserDTO
import com.khs.howstoday.model.IntentCode
import com.khs.howstoday.model.RequestCode
import com.khs.howstoday.model.UserDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_navigation.*
import kotlinx.android.synthetic.main.main_toolbar.*


class MainActivity : AppCompatActivity() {

    private var bottomNavigation: BottomNavigation? = null   // 하단내비게이션

    private var postsFragment: PostsFragment? = null         // 게시글
    private var roomsFragment: RoomsFragment? = null         // 방목록
    private var talksFragment: TalksFragment? = null         // 대화중인 방목록
    private var friendsFragment: LogsFragment? = null     // 친구목록
    private var profileFragment: ProfileFragment? = null     // 내 프로필

    private var backPressHolder: OnBackPressHolder = OnBackPressHolder() // 뒤로가기

    var auth: FirebaseAuth? = null
    var storage: FirebaseStorage? = null
    var fireStore: FirebaseFirestore? = null

    var destinationUid: String? = null
    var destinationUserEmail: String? = null

    init {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        fireStore = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigationListener()                     // 하단 네비게이션 리스너 셋업
        setupSettingListener()
        checkIntentCode()
    }

    private fun checkIntentCode() {
        intent?.let {
            Log.d("MainActivity", "INTENT_CODE: " + it.getStringExtra("INTENT_CODE"))
            when (it.getStringExtra("INTENT_CODE")) {
                IntentCode().MOVE_PROFILE_FRAGMENT_FROM_CONTENTS -> {
                    destinationUid = intent.getStringExtra("destinationUid")
                    destinationUserEmail = intent.getStringExtra("destinationUserEmail")
                    bottomNavigation?.navigationinterface?.profile()
                }
                else -> {
                    Log.d("MainActivity", "checkIntentCode() error")
                }
            }
        }
    }

    /* 환경설정 리스너*/
    private fun setupSettingListener() {
        settings.setOnClickListener {
            startActivity((Intent(this, SettingsActivity::class.java)))
        }
    }
    /* 환경설정 리스너 끝 */

    /* 툴바 초기화 */
    private fun setToolbarDefault() {
        main_toolbar_useremail.visibility = View.GONE
        main_toolbar_back.visibility = View.GONE
        logo_main.visibility = View.VISIBLE
        main_test_image.visibility = View.GONE
    }
    /* 툴바 초기화 */

    /* 하단 네비게이션 리스너 초기화 */
    private fun setupBottomNavigationListener() {
        var ft = supportFragmentManager.beginTransaction()
        bottomNavigation = BottomNavigation().apply {
            addActionBottomNavigationInterface(object : BottomNavigation.ActionBottomNavigation {
                override fun posts() {
                    setToolbarDefault()
                    postsFragment = PostsFragment()
                    var ft = supportFragmentManager.beginTransaction()
                    ft.replace(R.id.main_content, postsFragment!!).commit()
                }

                override fun rooms() {
                    setToolbarDefault()
                    roomsFragment = RoomsFragment()
                    var ft = supportFragmentManager.beginTransaction()
                    ft.replace(R.id.main_content, roomsFragment!!).commit()
                }

                override fun talks() {
                    setToolbarDefault()
                    talksFragment = TalksFragment()
                    var ft = supportFragmentManager.beginTransaction() // 늘 새롭게 초기화 해주어야 함
                    ft.replace(R.id.main_content, talksFragment!!).commit()
                }

                override fun friends() {
                    setToolbarDefault()
                    friendsFragment = LogsFragment()
                    var ft = supportFragmentManager.beginTransaction()
                    ft.replace(R.id.main_content, friendsFragment!!).commit()
                }

                override fun profile() {
                    setToolbarDefault()
                    var ft = supportFragmentManager.beginTransaction()
                    setupProfileFragment(ft)
                }
            })
        }
        bottom_navigation.setOnNavigationItemSelectedListener(bottomNavigation)
        main_toolbar_back.setOnClickListener {
            bottom_navigation.selectedItemId = R.id.action_posts
        }
        // 메인페이지 로딩시 바로 프로필 페이지로 이동
        // bottom_navigation.selectedItemId = R.id.action_profile
    }
    /* 하단 네비게이션 리스너 초기화 끝 */

    fun setupProfileFragment(ft: FragmentTransaction) {
        Log.d("MainActivity", "destinationUid: " + destinationUid)
        Log.d("MainActivity", "destinationUserEmail: " + destinationUserEmail)
        profileFragment = ProfileFragment()
        var bundle = Bundle()
        var uid = FirebaseAuth.getInstance().currentUser?.uid

        if (destinationUid == null) bundle.putString("destinationUid", uid)
        else {
            bundle.putString("destinationUid", destinationUid)
            if (intent != null && intent.getStringExtra("INTENT_CODE") == "0")
                bundle.putString("destinationUserEmail", destinationUserEmail)
        }

        profileFragment?.arguments = bundle
        ft.replace(R.id.main_content, profileFragment!!).commit()
        setDestination()               // 이동 후 초기화
    }

    private fun setDestination() {
        destinationUid = null
        destinationUserEmail = null
    }

    inner class OnBackPressHolder() {
        private var backPressHolder: Long = 0

        fun onBackPressed() {
            if (System.currentTimeMillis() > backPressHolder + 2000) {
                backPressHolder = System.currentTimeMillis()
                showBackToast()
                return
            }
            if (System.currentTimeMillis() <= backPressHolder + 2000) {
                finishAffinity()
            }
        }

        fun showBackToast() {
            Toast.makeText(this@MainActivity, "한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult() " + requestCode)
        if (requestCode == RequestCode().USER_PICK_IMAGE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            // 유저 프로필 이미지 업로드 시
            var imageUrl = data?.data
            var userEmail = auth?.currentUser?.email
            var storageRef = storage?.reference
                ?.child("userProfileImages/")
                ?.child(userEmail!!)

            // 이미지 다운로드 주소를 받아주기 위함
            storageRef?.putFile(imageUrl!!)?.continueWithTask {
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener {
                // 결과 값이 여기로 넘어옴
              //  var map = HashMap<String, Any>()
              //  map["userImage"] = it.toString()
                var tsDocUser = fireStore?.collection("users")?.document(userEmail!!)
                fireStore?.runTransaction { transaction ->
                    var userDTO = transaction.get(tsDocUser!!).toObject(UserDTO::class.java)
                        userDTO?.userImage = it.toString()
                        transaction.set(tsDocUser,userDTO!!)
                        return@runTransaction
                    }
                } // 프로필 이미지 저장은 하나씩
        } else {
            Log.d("MainActivity", "onActivityResult() Request is not valid")
            Log.d("MainActivity", "- RequestCode: " + requestCode)
            Log.d("MainActivity", "- ResultCode: " + resultCode)
            Log.d("MainActivity", "- (resultCode == Activity.RESULT_OK) => " + (resultCode == Activity.RESULT_OK)
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}


