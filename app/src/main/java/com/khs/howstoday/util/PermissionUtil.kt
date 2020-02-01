package com.khs.howstoday.util

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil {
    fun requestPermission(
        activity: Activity,
        requestCode: Int,
        vararg requestPermissions: String
    ): Boolean {
        var granted = true
        var permissionNeeded = ArrayList<String>()
        requestPermissions.forEach {
            var permissionCheck = ContextCompat.checkSelfPermission(activity, it)
            var hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED
            granted = granted and hasPermission
            if (!hasPermission) {
                permissionNeeded.add(it)
            }
        }
        if(granted) return true
        else{
            Log.d("PERMISSION","REQUEST")
            ActivityCompat.requestPermissions(
                activity,
                permissionNeeded.toTypedArray(),
                requestCode
            )
            return false
        }
    }

    fun requestGranted(
        requestCode:Int,
        permissionCode:Int,
        grantResults:IntArray
    ):Boolean{
        return requestCode == permissionCode
                && grantResults.size>0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}