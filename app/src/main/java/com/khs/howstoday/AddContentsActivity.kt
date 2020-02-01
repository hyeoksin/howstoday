package com.khs.howstoday

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.khs.howstoday.model.RequestCode
import com.khs.howstoday.action.AddPhotoActivityAction
import com.khs.howstoday.model.ContentDTO
import com.twitter.sdk.android.core.models.TwitterCollection
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddContentsActivity : AppCompatActivity() {

    var addPhotoActivityAction:AddPhotoActivityAction?=null

    var storage : FirebaseStorage? = null   // 파이어베이스 스토리지
    var auth:FirebaseAuth?=null             // 유저정보
    var firestore:FirebaseFirestore? =null  // 데이터베이스
    var photoUri: Uri? = null               // 이미지 URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)
        InitializeVariable()
        setupListener()
        openGallery()
    }

    fun InitializeVariable() {
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun openGallery() {
        // 앨범 열기
        startActivityForResult(Intent(Intent.ACTION_PICK).apply {
            setType("image/*")
        },RequestCode().PICK_IMAGE_FROM_ALBUM)
    }

    private fun setupListener() {
        addPhotoActivityAction = AddPhotoActivityAction().apply {
            addAddPhotoActivityActionInterface(object : AddPhotoActivityAction.AddPhotoActivityActionInterface {
                override fun photoUpload() {
                    contentUpload()
                }
            })
        }
        addphoto_btn_upload.setOnClickListener(addPhotoActivityAction)
    }

    private fun contentUpload() {
       var currentUserEmail =  auth?.currentUser?.email
       var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
       var imageFileName = "IMAGE_"+timestamp+"_.png"
       uploadIntoStorage(currentUserEmail,timestamp,imageFileName)
    }


    // Promise method
    private fun uploadIntoStorage(currentUserEmail: String?, timestamp: String, imageFileName: String) {
        var storageRef = storage?.reference?.child("contents/"+auth?.currentUser?.email)?.child(imageFileName)
        storageRef?.putFile(photoUri!!)?.continueWithTask {
            return@continueWithTask storageRef.downloadUrl
            // 업로드 할 때 이미지 주소 받아오기
        }?.addOnSuccessListener {
            // 데이터베이스에 저장
            saveIntoDatabase(it) //
            Toast.makeText(this,"사진을 업로드했습니다.",Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveIntoDatabase(it: Uri?) {
        var contentDTO = ContentDTO()
        contentDTO.contentImageUrl = it.toString()
        contentDTO.userLocation ="서울특별시 중랑구 상봉동"
        contentDTO.uid = auth?.currentUser?.uid
        contentDTO.userId = auth?.currentUser?.email
        contentDTO.explain = addphoto_edit_explain.text.toString()
        contentDTO.timestamp = System.currentTimeMillis()
        firestore?.collection("contents")?.document()?.set(contentDTO)
        setResult(Activity.RESULT_OK) // 정상적으로 닫혔다라는 flag 값
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RequestCode().PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){   // 사진을 선택했을 경우
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            }else{
                finish()
            }
        }
    }
}
