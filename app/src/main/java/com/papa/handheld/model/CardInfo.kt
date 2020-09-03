package com.papa.handheld.model

import com.adrian.basemodule.orZero
import com.sunmi.pay.hardware.aidl.AidlConstants

/**
 * author:RanQing
 * date:2019/6/29 0029 13:29
 * description:
 **/
class CardInfo(val cardType: Int, val value: String?) {

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