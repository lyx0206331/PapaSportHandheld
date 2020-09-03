package com.papa.handheld.model

import com.adrian.basemodule.orZero
import com.sunmi.pay.hardware.aidl.AidlConstants

/**
 * author:RanQing
 * date:2019/6/29 0029 13:29
 * description:
 **/
class CardInfo(
    val cardType: Int,
    val cardValue: CardValue? = null,
    val errorInfo: ErrorInfo? = null
) {

    companion object {
        //无效卡
        const val TYPE_INVALID_CARD = "TYPE_INVALID_CARD"

        //磁卡
        const val TYPE_MAG_CARD = "TYPE_MAG_CARD"

        //NFC卡
        const val TYPE_NFC_CARD = "TYPE_NFC_CARD"

        //IC卡
        const val TYPE_IC_CARD = "TYPE_IC_CARD"

        private val cardTypes = hashMapOf(
            Pair(TYPE_INVALID_CARD, 0),
            Pair(TYPE_MAG_CARD, AidlConstants.CardType.MAGNETIC.value),
            Pair(TYPE_NFC_CARD, AidlConstants.CardType.NFC.value),
            Pair(TYPE_IC_CARD, AidlConstants.CardType.IC.value)
        )

        fun getCardType(name: String): Int = cardTypes[name].orZero()
    }
}

/**
 * code1/code2/code3::磁道错误码：0-无错误，-1-磁道无数据，-2-奇偶校验错，-3-LRC校验错
 */
class CardValue(
    val code1: Int = 0,
    val track1: String? = null,
    val code2: Int = 0,
    val track2: String? = null,
    val code3: Int = 0,
    val track3: String? = null,
    val atr: String? = null,
    val uuid: String? = null
)

class ErrorInfo(val code: Int, val msg: String? = null)