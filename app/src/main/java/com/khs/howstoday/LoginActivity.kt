package com.khs.howstoday

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.khs.howstoday.model.CrushUserDTO
import com.khs.howstoday.model.FriendDTO
import com.khs.howstoday.model.RequestCode
import com.khs.howstoday.model.UserDTO
import com.khs.howstoday.util.PermissionUtil
import kotlinx.android.synthetic.main.main_toolbar.*
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Permission

class LoginActivity : AppCompatActivity() {

    private var backPressHolder: OnBackPressHolder = OnBackPressHolder() // 뒤로가기

    val hashKey:String = "rpIeIjIMg+7LajI2KG+U1gZ5SW4="
    var fireStore:FirebaseFirestore?=null
    var user:FirebaseUser?=null                         // 접속에 성공한 유저

    init{
        fireStore = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setUpLoginActivity()                                            // 로그인 액티비티 초기화
    }

    /* 로그인 화면 초기화 */
    private fun setUpLoginActivity() {
        checkPreviousLogin()
    }
    /* 로그인 화면 초기화 끝 */

    /* 자동 로그인 체크*/
    private fun checkPreviousLogin() {
        if(FirebaseAuth.getInstance().currentUser==null) showLoginWindow()
        else {
            moveToOpenMainActivity()
        }
    }
    /* 자동 로그인 체크 끝*/

    private fun moveToOpenMainActivity() {
        startActivity(Intent(this,MainActivity::class.java))
    }

    private fun showLoginWindow() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
//          AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.GreenTheme)
                .setLogo(R.drawable.logo_main)
                .build(),
            RequestCode().RC_SIGN_IN)
    }

    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                var hashkey = String(Base64.encode(md.digest(),0))
                Log.i("TAG","printHashKey() Hash key: $hashkey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG","printHashkey()",e);
        } catch (e: Exception) {
            Log.e("TAG","printHashkey()",e);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RequestCode().RC_SIGN_IN){
            if(resultCode == Activity.RESULT_OK){
                // 로그인에 성공 했을 경우
                Log.d("LoginActivity","Login()")
                user = FirebaseAuth.getInstance().currentUser
                RegisterUserTask(this,user!!).execute()
                moveToOpenMainActivity()
            }else{
                Toast.makeText(this,"이용해주셔서 감사합니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class OnBackPressHolder(){
        private var backPressHolder:Long=0

        fun onBackPressed(){
            if(System.currentTimeMillis()> backPressHolder +2000){
                backPressHolder = System.currentTimeMillis()
                showBackToast()
                return
            }

            if(System.currentTimeMillis() <=backPressHolder +2000){
                finishAffinity()
            }
        }

        fun showBackToast(){
            Toast.makeText(this@LoginActivity,"한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        backPressHolder?.onBackPressed()
    }

    inner class RegisterUserTask constructor(
        activity: LoginActivity,
        user:FirebaseUser
    ) : AsyncTask<Any,Void,String>(){

        private var weakReference:WeakReference<LoginActivity>?=null
        private var currentUser:FirebaseUser?=null
        private var REGISTER_SUCCESS:String = "false"

        init{
            this.weakReference = WeakReference(activity)
            this.currentUser = user
        }

        override fun doInBackground(vararg params: Any?): String {
            // 1) 데이터베이스에 등록된 이메일인지 확인
            var tsDocCrush = fireStore?.collection("users")?.document(currentUser?.email!!)
            fireStore?.runTransaction { transaction ->
                var userDTO = transaction.get(tsDocCrush!!).toObject(UserDTO::class.java)
                if(userDTO == null){
                    // 회원가입된 유저가 아니라면
                    Log.d("LoginActivity","New Member")
                    userDTO = UserDTO()
                    userDTO!!.crush = CrushUserDTO()
                    userDTO!!.friend = FriendDTO()
                    userDTO!!.uid = currentUser?.uid
                    userDTO!!.userNickName = currentUser?.displayName
                    userDTO!!.userEmail = currentUser?.email
                    transaction.set(tsDocCrush,userDTO)
                    return@runTransaction
                }
                Log.d("LoginActivity","Exsit Member")
                REGISTER_SUCCESS = "true"
            }
            return REGISTER_SUCCESS
        }
    }


}
