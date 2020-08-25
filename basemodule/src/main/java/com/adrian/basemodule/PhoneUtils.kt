package com.adrian.basemodule

import android.content.Context
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
}