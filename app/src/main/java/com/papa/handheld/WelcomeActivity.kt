package com.papa.handheld

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.adrian.basemodule.BaseActivity
import com.adrian.basemodule.ToastUtils
import com.adrian.basemodule.orZero
import com.papa.handheld.util.getHostUrl
import com.papa.handheld.util.saveHostUrl
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.layout_host_view.view.*
import java.lang.ref.WeakReference

class WelcomeActivity : BaseActivity() {

    companion object {
        class MyHandler(ref: WeakReference<WelcomeActivity>) : Handler() {
            private val act: WelcomeActivity? = ref.get()

            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                when (msg?.what.orZero()) {
                    0 -> {
                        val intent = Intent(act, MainActivity::class.java)
                        act?.startActivity(intent)
                        act?.finish()
                    }
                }
            }
        }
    }

    private val handler by lazy { MyHandler(WeakReference(this)) }

    override fun getLayoutResId(): Int {
        return R.layout.activity_welcome
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)

        tvSettings.setOnClickListener {
            handler.removeMessages(0)
            val layout = LayoutInflater.from(this).inflate(R.layout.layout_host_view, null, false)
            layout.etUrl.text = Editable.Factory.getInstance().newEditable(getHostUrl())
            val dialog = AlertDialog.Builder(this).setTitle(R.string.env_set).setView(layout)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
                .setOnDismissListener { handler.sendEmptyMessageDelayed(0, 1000) }.create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val url = layout.etUrl.text
                if (url.isNullOrEmpty()) {
                    ToastUtils.showToastShort(getString(R.string.url_empty_tips))
                } else {
                    saveHostUrl(url.toString())
                    dialog.dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        handler.sendEmptyMessageDelayed(0, 1000)
    }
}
