package com.papa.handheld.application

import android.content.Context
import android.content.MutableContextWrapper
import android.webkit.WebView
import androidx.multidex.MultiDex
import com.adrian.basemodule.BaseApplication
import com.papa.handheld.printerUtil.SunmiPrintHelper

/**
 * author:RanQing
 * date:2019/6/18 0018 0:36
 * description:
 **/
class MyApplication : BaseApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        val webView = WebView(MutableContextWrapper(this))
        SunmiPrintHelper.getInstance().initSunmiPrinterService(this)
    }

}