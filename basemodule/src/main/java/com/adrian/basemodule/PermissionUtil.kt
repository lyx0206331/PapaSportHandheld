package com.adrian.basemodule

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.jetbrains.annotations.NotNull

//  ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//   ┃　　　┃   神兽保佑
//   ┃　　　┃   代码无BUG！
//   ┃　　　┗━━━┓
//   ┃　　　　　　　┣┓
//   ┃　　　　　　　┏┛
//   ┗┓┓┏━┳┓┏┛
//     ┃┫┫　┃┫┫
//     ┗┻┛　┗┻┛
/**
 * Author:RanQing
 * Create time:20-8-4 上午11:12
 * Description:权限请求设置工具类
 **/
class PermissionUtil constructor(private val activity: Activity) {

    private lateinit var permissions: Array<String>

    /**
     * 缺少权限列表
     */
    private val lackPermissons: ArrayList<String> = arrayListOf()
        get() {
            field.clear()
            permissions.forEach {
                if (ContextCompat.checkSelfPermission(
                        activity,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    field.add(it)
                }
            }
            LogUtils.logE("lackPermission", field.toString())
            return field
        }

    private var callback: IPermissionCallback? = null

    companion object {

        const val PERMISSION_REQ_CODE = 1

//        @Volatile
//        private var instance: PermissionUtil? = null
//        fun getInstance(activity: Activity) = instance ?: synchronized(this) {
//            instance ?: PermissionUtil(activity).also {
//                instance = it
//            }
//        }
    }

    /**
     * 请求权限
     */
    fun requestPermission(
        @NotNull permissions: Array<String>,
        @Nullable callback: IPermissionCallback? = null
    ): PermissionUtil {
        this.permissions = permissions
        this.callback = callback
        if (!permissions.isNullOrEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lackPermissons.size > 0) {
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQ_CODE)
        } else {
            callback?.allowedPermissions()
        }
        return this
    }

    /**
     * 此方法在Activity中重写[override]的onRequestPermissionsResult(...)方法中调用
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQ_CODE -> {
                grantResults.forEachIndexed { index, i ->
                    val permission = permissions[index]
                    if (i != PackageManager.PERMISSION_GRANTED) {
//                        ToastUtils.showToastShort("请求${permission}授权被拒绝")
//                            goToSetting()
                        LogUtils.logE("req permission", "----------------")
                        return@forEachIndexed
                    } else {
//                        ToastUtils.showToastShort("${permission}已授权")
                    }
                }
                if (lackPermissons.size > 0) {
                    callback?.deniedPermissions()
                } else {
                    callback?.allowedPermissions()
                }
            }
        }
    }

    fun showTips(deniedTips: String = "缺少必要权限，请手动设置，否则可能导致部分功能不可用！是否进入设置界面?"): AlertDialog =
        AlertDialog.Builder(activity).setMessage(deniedTips)
            .setPositiveButton("是") { dialog, _ ->
                dialog.dismiss()
                goToSetting()
            }.setNegativeButton("否") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(false).show()

    /**
     * 跳转到权限详情设置页
     */
    private fun goToSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

    interface IPermissionCallback {
        fun allowedPermissions()
        fun deniedPermissions()
    }
}