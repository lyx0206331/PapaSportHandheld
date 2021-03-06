package com.adrian.basemodule

import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission

/**
 * date:2019/7/4 16:15
 * author:RanQing
 * description:
 */
object PhoneUtils {
    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    fun getImeiNum(): String {
        val tm =
            BaseApplication.instance.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.deviceId.orEmpty()
    }

    fun getDeviceId(context: Context): String {
        return Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getVersionName(context: Context) =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName

    fun getVersionCode(context: Context) =
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode

    fun getPackageName(context: Context) =
        context.packageManager.getPackageInfo(context.packageName, 0).packageName

    fun getAppName(context: Context) = context.getString(
        context.packageManager.getPackageInfo(
            context.packageName,
            0
        ).applicationInfo.labelRes
    )
}