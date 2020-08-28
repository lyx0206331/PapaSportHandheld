package com.papa.handheld

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.adrian.basemodule.LogUtils.logE
import com.adrian.basemodule.PermissionUtil
import com.adrian.basemodule.PhoneUtils
import com.adrian.basemodule.ToastUtils.showToastShort
import com.adrian.basemodule.orFalse
import com.alibaba.fastjson.JSON
import com.just.agentweb.*
import com.papa.handheld.model.DeviceInfo
import com.papa.handheld.model.PrintInfo
import com.papa.handheld.model.ScanInfo
import com.papa.handheld.view.SmartRefreshWebLayout
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.android.synthetic.main.activity_base_web.*

class MainActivity : BaseWebActivity() {

    companion object {
        const val TAG = "MainActivity"

        const val REQUEST_CODE_SCAN = 0
    }

    private var curUrl: String? = null
    private var pageTag: String = "memberSearch"

    private var permissionUtil = PermissionUtil(this)
    private val permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.title_bg_color)
        }
        super.onCreate(savedInstanceState)

        /*setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setBackgroundResource(R.color.title_bg_color)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.mipmap.back)

        btnQuit.setOnClickListener {
            agentWeb.jsAccessEntrace.quickCallJs(
                "signOut"
            )
        }
        ibHome.setOnClickListener {
            agentWeb.jsAccessEntrace.quickCallJs("linkToHome")
        }
*/
        permissionUtil.requestPermission(permissions, object : PermissionUtil.IPermissionCallback {
            override fun allowedPermissions() {
                logE("req permission", "permissions1")
            }

            override fun deniedPermissions() {
                permissionUtil.showTips()
            }
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_SCAN -> {
                    data?.apply {
                        val bundle = extras
                        val result =
                            bundle.getSerializable("data") as ArrayList<HashMap<String, String>>
                        val iterator = result.iterator()
                        while (iterator.hasNext()) {
                            val hashMap = iterator.next()
                            val scanType = hashMap["TYPE"] ?: "未知扫码类型"
                            val scanValue = hashMap["VALUE"] ?: "无扫码结果"
                            val scanInfo = ScanInfo(scanValue)
                            logE("SCAN", scanInfo.toJson())
                            agentWeb.jsAccessEntrace.quickCallJs(
                                "androidCallH5",
                                scanInfo.toJson()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun addBGChild(frameLayout: FrameLayout) {
        frameLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_base_web
    }

    override fun getAndroidInterface(): AndroidInterface {
        return AndroidInterface(this, agentWeb, object : AndroidInterface.IJsListener {

            override fun printJsContent(msg: String) {
                logE("PAPA", "printMsg")
                try {
//                    val test = "{\"payInfo\":{\"consumeAddr\":\"啪啪运动第一运动公园\",\"consumeType\":\"门票\",\"fieldName\":\"啪啪运动第一运动公园\",\"printTime\":\"\",\"total\":\"0.03\",\"offer\":\"0.03\",\"payType\":\"现金\",\"payTime\":\"2019-07-06 16:39:43\",\"ticketList\":[{\"count\":\"1\",\"name\":\"游泳日票\",\"price\":\"0.01\"},{\"count\":\"1\",\"name\":\"大熊测试\",\"price\":\"0.02\"}],\"remark\":\"\"},\"ticketInfo\":[]}"
                    val printInfo = JSON.parseObject(msg, PrintInfo::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToastShort("数据解析异常")
                }
            }

            override fun startScan() {
                logE("PAPA", "startScan")
                try {
                    bootScanner()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    showToastShort("扫码功能启动异常,请检查设备!")
                }
            }

            override fun turnOnNFC() {
                logE("PAPA", "turnOnNFC")
//                bootScanner()
            }

            override fun turnOffNFC() {
                logE("PAPA", "turnOffNFC")
            }

            override fun turnOnRFID() {
                logE("PAPA", "turnOnRFID")
            }

            override fun turnOffRFID() {
            }
        })
    }

    private fun bootScanner() {
        val intent = Intent("com.sunmi.scan")
        intent.setPackage("com.sunmi.sunmiqrcodescanner")
        startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    override fun getAgentWebSettings(): AbsAgentWebSettings {
        return AgentWebSettingsImpl.getInstance()
    }

    override fun getMiddleWareWebClient(): MiddlewareWebClientBase {
        return object : MiddlewareWebClientBase() {}
    }

    override fun getMiddleWareWebChrome(): MiddlewareWebChromeBase {
        return object : MiddlewareWebChromeBase() {}
    }

    override fun getErrorLayoutEntity(): ErrorLayoutEntity {
        return ErrorLayoutEntity()
    }

    override fun getWebChromeClient(): WebChromeClient? {
        return object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
//                tvTitle?.text = title
            }
        }
    }

    override fun getWebViewClient(): WebViewClient? {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                logE(TAG, "shouldOverrideUrlLoading")
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                logE(TAG, "onPageStarted. url: $url")
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                logE(TAG, "shouldOverrideUrlLoading. url: $url")
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                logE(TAG, "onPageFinished. url: $url")
                when {
                    url?.endsWith("login").orFalse() -> {
                        val deviceInfoJson =
                            JSON.toJSONString(DeviceInfo(PhoneUtils.getDeviceId(this@MainActivity)))
                        logE(TAG, deviceInfoJson)
                        agentWeb.jsAccessEntrace.quickCallJs(
                            "getImei",
                            deviceInfoJson
                        )
//                        toolbar.visibility = View.GONE
//                        ibHome.visibility = View.GONE
//                        btnQuit.visibility = View.GONE
                    }
                    url?.endsWith("index").orFalse() -> {
//                        toolbar.visibility = View.VISIBLE
//                        btnQuit.visibility = View.VISIBLE
//                        ibHome.visibility = View.GONE
                    }
                    else -> {
//                        toolbar.visibility = View.VISIBLE
//                        btnQuit.visibility = View.GONE
//                        ibHome.visibility = View.VISIBLE
                    }
                }
                curUrl = url
                if (!isDiscernUserPage()) {
                }
                super.onPageFinished(view, url)
            }
        }
    }

    /**
     * 判断识别用户界面。身份证，扫码，会员卡
     */
    private fun isDiscernUserPage(): Boolean {
        return curUrl?.endsWith(pageTag).orFalse()
    }

    override fun getWebView(): WebView? {
        val wv = WebView(this)
        wv.settings.javaScriptEnabled = true
        wv.settings.useWideViewPort = true
        wv.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        wv.settings.loadWithOverviewMode = true
        wv.settings.setSupportZoom(true)
        return wv
//        return null
    }

    override fun getWebLayout(): IWebLayout<*, *>? {
        return if (BuildConfig.DEBUG) {
            val smartRefreshWebLayout = SmartRefreshWebLayout(this)
            val smartRefreshLayout = smartRefreshWebLayout.layout as SmartRefreshLayout
            smartRefreshLayout.setOnRefreshListener {
                agentWeb.urlLoader.reload()
                smartRefreshLayout.postDelayed({
                    smartRefreshLayout.finishRefresh()
                }, 2000)
            }
            smartRefreshWebLayout
        } else {
            null
        }
    }

    override fun getPermissionInterceptor(): PermissionInterceptor? {
        return PermissionInterceptor { url, permissions, action ->
            logE(TAG, "url:$url permission:$permissions action:$action")
            false
        }
    }

    override fun getAgentWebUIController(): AgentWebUIControllerImplBase? {
        return null
    }

    override fun getOpenOtherAppWay(): DefaultWebClient.OpenOtherPageWays? {
        return DefaultWebClient.OpenOtherPageWays.ASK
    }

    override fun getAgentWebParent(): ViewGroup {
        return container
    }

    override fun getUrl(): String {
        //papa 123456
        return if (BuildConfig.DEBUG) {
            "http://demo.handtest.papa.com.cn:8280"
        } else {
            "http://papa.hand.ppdev.fun:8180"
        }
//        return "https://pda.papa.com.cn"
//        return "http://192.168.1.12:8039"
    }

}