package com.papa.handheld

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.papa.handheld.model.*
import com.papa.handheld.printerUtil.SunmiPrintHelper
import com.papa.handheld.util.Utility
import com.papa.handheld.view.SmartRefreshWebLayout
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2
import com.sunmi.peripheral.printer.InnerResultCallbcak
import kotlinx.android.synthetic.main.activity_base_web.*
import sunmi.paylib.SunmiPayKernel
import java.util.*

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

    var basicOptV2: BasicOptV2? = null
    var readCardOptV2: ReadCardOptV2? = null
    val smPayKernel = SunmiPayKernel.getInstance()

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.title_bg_color)
        }
        super.onCreate(savedInstanceState)
        SunmiPrintHelper.getInstance().initPrinter()

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
            }

            override fun deniedPermissions() {
                permissionUtil.showTips()
            }
        })

        smPayKernel.initPaySDK(this, object : SunmiPayKernel.ConnectCallback {
            override fun onConnectPaySDK() {
                basicOptV2 = smPayKernel.mBasicOptV2
                readCardOptV2 = smPayKernel.mReadCardOptV2
                logE(TAG, "read card connected")
            }

            override fun onDisconnectPaySDK() {
                logE(TAG, "read card disconnect")
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
        smPayKernel.destroyPaySDK()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
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
                            val scanInfo = ScanInfo(scanType, scanValue)
                            logE("SCAN", scanInfo.toJson())
                            callWebInterface(jsonInfo = scanInfo.toJson())
                        }
                    }
                }
            }
        }
    }

    /**
     * 调用前端接口
     */
    private fun callWebInterface(interfaceName: String = "androidCallH5", jsonInfo: String) =
        agentWeb.jsAccessEntrace.quickCallJs(interfaceName, jsonInfo)

    override fun addBGChild(frameLayout: FrameLayout) {
        frameLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_base_web
    }

    override fun getAndroidInterface(): AndroidInterface {
        return AndroidInterface(this, agentWeb, object : AndroidInterface.IJsListener {

            override fun printJsContent(msg: String) {
                logE("PAPA", "printMsg: $msg")
                try {
//                    val test = "{\"ticket_code\":\"T00001yI492\",\"face_value\":\"50.00\",\"pay_price\":\"50.00\",\"member_id\":0,\"member_card_id\":0,\"date_str\":\"2020-08-27\",\"start_date\":\"2020-08-27\",\"end_date\":\"2020-12-04\",\"status\":3,\"status_str\":\"已检票\",\"ticket_name\":\"向上票\",\"valid_time\":100,\"valid_num\":1000,\"valid_type\":2,\"sport_tag_id\":1,\"member_type\":\"1,4,14\",\"session_name\":\"羽毛球（午夜）\",\"no\":null,\"from\":\"现场购票\",\"name\":\"--\",\"phone\":\"--\",\"member_type_str\":\"普通会员,教职工,testA\",\"stadium_name\":\"深圳大运中心\"}"
                    val ticketInfo = JSON.parseObject(msg, TicketData::class.java)
                    ticketInfo?.ticketInfo?.let {
                        startPrint(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToastShort("数据解析异常")
                }
            }

            override fun startScan() {
                logE("PAPA", "startScan")
                try {
                    this@MainActivity.startScan()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    showToastShort("扫码功能启动异常,请检查设备!")
                }
            }

            override fun turnOnNFC() {
//                testPrint()
                checkCard()
            }

            override fun turnOffNFC() {
                logE("PAPA", "turnOffNFC")
                cancleCheckCard()
            }

            override fun turnOnRFID() {
                logE("PAPA", "turnOnRFID")
            }

            override fun turnOffRFID() {
            }
        })
    }

    private fun testPrint() {
        try {
            val test =
                "{\"ticketInfo\":{\"ticket_code\":\"T00yX001520\",\"face_value\":\"0.01\",\"pay_price\":\"1.00\",\"member_id\":171,\"member_card_id\":711,\"date_str\":\"2020-08-29\",\"start_date\":\"2020-08-29\",\"end_date\":\"2020-12-06\",\"status\":3,\"status_str\":\"已检票\",\"ticket_name\":\"111111111111\",\"valid_time\":100,\"valid_num\":1,\"valid_type\":2,\"sport_tag_id\":2,\"member_type\":\"1,46,4,14,23,24,28,35,36,42\",\"session_name\":\"篮球周末场次\",\"no\":null,\"from\":\"现场购票\",\"name\":\"囧囧\",\"phone\":\"18566766922\",\"card_num\":\"963084089683\",\"member_type_str\":\"普通会员,测试类型aa,教职工,testA,学生A2,职工家属,客户类型11,12,hi,儿童3\",\"stadium_name\":\"深圳大运中心\"}}"
            val ticketInfo = JSON.parseObject(test, TicketData::class.java)
            ticketInfo?.ticketInfo?.let {
                startPrint(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToastShort("数据解析异常")
        }
    }

    /**
     * 开始打印
     */
    private fun startPrint(ticketInfo: TicketInfo) {
        SunmiPrintHelper.getInstance().printTrans(this, ticketInfo, object : InnerResultCallbcak() {
            override fun onRunResult(p0: Boolean) {
            }

            override fun onReturnString(p0: String?) {
            }

            override fun onRaiseException(p0: Int, p1: String?) {
            }

            override fun onPrintResult(p0: Int, p1: String?) {
                logE(
                    TAG,
                    if (p0 == 0) "Transaction print successful!" else "Transaction print failed!"
                )
            }
        })
//        SunmiPrintHelper.getInstance().feedPaper()
    }

    /**
     * 启动扫码
     */
    private fun startScan() {
        val intent = Intent("com.sunmi.scan")
        intent.setPackage("com.sunmi.sunmiqrcodescanner")
        startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    private fun checkCard() {
        readCardOptV2?.checkCard(
            AidlConstants.CardType.MAGNETIC.value.or(AidlConstants.CardType.NFC.value)
                .or(
                    AidlConstants.CardType.IC.value
                ), object : CheckCardCallbackV2.Stub() {
                override fun findMagCard(info: Bundle?) {
                    logE(TAG, "findMagCard, bundle:${Utility.bundle2String(info)}")
                    handleResult(info)
                }

                override fun findICCard(atr: String?) {
                    logE(TAG, "findICCard.atr:$atr")
                    showToastShort("atr:$atr")
                    callWebInterface(
                        jsonInfo = JSON.toJSONString(
                            CardInfo(
                                CardInfo.getCardType(
                                    CardInfo.TYPE_IC_CARD
                                ), atr
                            )
                        )
                    )
                }

                override fun findRFCard(uuid: String?) {
                    logE(TAG, "findRFCard. uuid:$uuid")
                    showToastShort("uuid:$uuid")
                    callWebInterface(
                        jsonInfo = JSON.toJSONString(
                            CardInfo(
                                CardInfo.getCardType(
                                    CardInfo.TYPE_NFC_CARD
                                ), uuid
                            )
                        )
                    )
                }

                override fun onError(code: Int, message: String?) {
                    logE(TAG, "onError.code:$code  || msg:$message")
                    handleResult(null)
                    callWebInterface(
                        jsonInfo = JSON.toJSONString(
                            CardInfo(
                                CardInfo.getCardType(
                                    CardInfo.TYPE_INVALID_CARD
                                ), "$code -- $message"
                            )
                        )
                    )
                }

                override fun findICCardEx(info: Bundle?) {
                    logE(TAG, "findICCardEx, info: ${Utility.bundle2String(info)}")
                }

                override fun findRFCardEx(info: Bundle?) {
                    logE(TAG, "findRFCardEx, info: ${Utility.bundle2String(info)}")
                }

                override fun onErrorEx(info: Bundle?) {
                    logE(TAG, "onErrorEx, info: ${Utility.bundle2String(info)}")
                }

            }, 60
        )
    }

    private fun cancleCheckCard() {
        readCardOptV2?.cancelCheckCard()
    }

    private fun handleResult(bundle: Bundle?) {
        if (isFinishing) return
        handler.post {
            if (bundle == null) {
                showToastShort("无效卡")
            } else {
                val track1 = Utility.null2String(bundle.getString("TRACK1"))
                val track2 = Utility.null2String(bundle.getString("TRACK2"))
                val track3 = Utility.null2String(bundle.getString("TRACK3"))

                //磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
                val code1 = bundle.getInt("track1ErrorCode")
                val code2 = bundle.getInt("track2ErrorCode")
                val code3 = bundle.getInt("track3ErrorCode")

                val logMsg = String.format(
                    Locale.getDefault(),
                    "track1ErrorCode:%d,track1:%s\ntrack2ErrorCode:%d,track2:%s\ntrack3ErrorCode:%d,track3:%s",
                    code1, track1, code2, track2, code3, track3
                )
                logE(TAG, logMsg)
                showToastShort(logMsg)

//                if (!isFinishing) {
//                    handler.postDelayed(this::checkCard, 500)
//                }
            }
        }
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
                        callWebInterface("getImei", deviceInfoJson)
                    }
                    url?.endsWith("index").orFalse() -> {
                    }
                    else -> {
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
        /*return if (BuildConfig.DEBUG) {
            "http://demo.handtest.papa.com.cn:8280"
        } else {
            "http://papa.hand.ppdev.fun:8180"
        }*/
        return "http://papa.hand.ppdev.fun:8180"
    }

}