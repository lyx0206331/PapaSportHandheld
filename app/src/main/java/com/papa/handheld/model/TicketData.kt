package com.papa.handheld.model

/**
 * date:2019/7/3 9:56
 * author:RanQing
 * description:
 */
data class TicketData(
    val ticketInfo: TicketInfo?
) {
    constructor() : this(null)
}

data class TicketInfo(
    //购票卡号
    val card_num: String?,
    val date_str: String?,
    val end_date: String?,
    val face_value: String?,
    val from: String?,
    val member_card_id: Int?,
    val member_id: Int?,
    val member_type: String?,
    val member_type_str: String?,
    //购票人名称
    val name: String?,
    val no: String?,
    //金额
    val pay_price: String?,
    //购票人手机
    val phone: String?,
    val session_name: String?,
    val sport_tag_id: Int?,
    //场管名称
    val stadium_name: String?,
    val start_date: String?,
    val status: Int?,
    //状态
    val status_str: String?,
    //门票票号
    val ticket_code: String?,
    //门票名称
    val ticket_name: String?,
    val valid_num: Int?,
    val valid_time: Int?,
    val valid_type: Int?
) {
    constructor() : this(
        "", "", "", "", "", 0, 0, "", "", "", "",
        "", "", "", 0, "", "", 0, "", "", "", 0, 0, 0
    )
}