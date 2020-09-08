package com.papa.handheld.util

import android.content.Context

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * author:RanQing
 * date:2020/9/8 0008 10:44
 * description:
 */
const val PREF_NAME = "papa_pref"

const val URL_HOST = "host"

fun Context.saveHostUrl(host: String) =
    this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
        putString(URL_HOST, host)
        commit()
    }

fun Context.getHostUrl(): String =
    this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(
        URL_HOST, "http://yanshi.hand.papa.com.cn"
    )