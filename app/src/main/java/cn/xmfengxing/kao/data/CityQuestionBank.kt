package cn.xmfengxing.kao.data

import android.content.Context

data class CityQuestionBank(
    val id: Int,
    val name: String,
    val initial: String,
    val isHot: Boolean = false
)

object CityQuestionBanks {
    val National = CityQuestionBank(0, "全国", "Q", isHot = true)

    val All: List<CityQuestionBank> = listOf(
        National,
        CityQuestionBank(2, "天津", "T"),
        CityQuestionBank(1, "北京", "B", isHot = true),
        CityQuestionBank(3, "上海", "S", isHot = true),
        CityQuestionBank(4, "重庆", "C"),
        CityQuestionBank(5, "黑龙江", "H"),
        CityQuestionBank(16, "四川", "S"),
        CityQuestionBank(21, "湖南", "H"),
        CityQuestionBank(24, "杭州", "H", isHot = true),
        CityQuestionBank(25, "南京", "N", isHot = true),
        CityQuestionBank(29, "山西", "S"),
        CityQuestionBank(31, "安徽", "A"),
        CityQuestionBank(110, "呼和浩特", "H"),
        CityQuestionBank(111, "包头", "B"),
        CityQuestionBank(112, "乌海", "W"),
        CityQuestionBank(115, "赤峰", "C"),
        CityQuestionBank(114, "通辽", "T"),
        CityQuestionBank(2679, "鄂尔多斯", "E"),
        CityQuestionBank(2653, "呼伦贝尔", "H"),
        CityQuestionBank(2689, "巴彦淖尔", "B"),
        CityQuestionBank(2667, "乌兰察布", "W"),
        CityQuestionBank(471, "兴安盟", "X"),
        CityQuestionBank(484, "锡林郭勒盟", "X"),
        CityQuestionBank(3179, "阿拉善盟", "A"),
        CityQuestionBank(143, "广州", "G", isHot = true),
        CityQuestionBank(144, "韶关", "S"),
        CityQuestionBank(146, "深圳", "S", isHot = true),
        CityQuestionBank(148, "珠海", "Z"),
        CityQuestionBank(290, "汕头", "S"),
        CityQuestionBank(291, "佛山", "F"),
        CityQuestionBank(301, "江门", "J"),
        CityQuestionBank(293, "湛江", "Z"),
        CityQuestionBank(475, "茂名", "M"),
        CityQuestionBank(292, "肇庆", "Z"),
        CityQuestionBank(145, "惠州", "H"),
        CityQuestionBank(289, "梅州", "M"),
        CityQuestionBank(2784, "汕尾", "S"),
        CityQuestionBank(472, "河源", "H"),
        CityQuestionBank(3097, "阳江", "Y"),
        CityQuestionBank(2712, "清远", "Q"),
        CityQuestionBank(149, "东莞", "D"),
        CityQuestionBank(147, "中山", "Z"),
        CityQuestionBank(300, "潮州", "C"),
        CityQuestionBank(294, "揭阳", "J"),
        CityQuestionBank(469, "云浮", "Y"),
        CityQuestionBank(150, "厦门", "X", isHot = true),
        CityQuestionBank(167, "南昌", "N"),
        CityQuestionBank(176, "武汉", "W", isHot = true),
        CityQuestionBank(194, "郑州", "Z"),
        CityQuestionBank(195, "开封", "K"),
        CityQuestionBank(196, "洛阳", "L"),
        CityQuestionBank(323, "平顶山", "P"),
        CityQuestionBank(321, "安阳", "A"),
        CityQuestionBank(326, "鹤壁", "H"),
        CityQuestionBank(322, "新乡", "X"),
        CityQuestionBank(325, "焦作", "J"),
        CityQuestionBank(197, "许昌", "X"),
        CityQuestionBank(456, "长葛", "C"),
        CityQuestionBank(440, "漯河", "L"),
        CityQuestionBank(442, "三门峡", "S"),
        CityQuestionBank(324, "商丘", "S"),
        CityQuestionBank(441, "驻马店", "Z"),
        CityQuestionBank(204, "济南", "J"),
        CityQuestionBank(205, "青岛", "Q"),
        CityQuestionBank(208, "淄博", "Z"),
        CityQuestionBank(446, "枣庄", "Z"),
        CityQuestionBank(443, "东营", "D"),
        CityQuestionBank(206, "烟台", "Y"),
        CityQuestionBank(261, "潍坊", "W"),
        CityQuestionBank(262, "济宁", "J"),
        CityQuestionBank(207, "泰安", "T"),
        CityQuestionBank(448, "威海", "W"),
        CityQuestionBank(454, "日照", "R"),
        CityQuestionBank(263, "临沂", "L"),
        CityQuestionBank(209, "德州", "D"),
        CityQuestionBank(436, "聊城", "L"),
        CityQuestionBank(467, "滨州", "B"),
        CityQuestionBank(264, "菏泽", "H")
    ).distinctBy(CityQuestionBank::id)

    val Hot: List<CityQuestionBank> = All.filter(CityQuestionBank::isHot)

    val Alphabet: List<String> = All
        .filter { it.id != National.id }
        .map(CityQuestionBank::initial)
        .distinct()
        .sorted()

    fun findById(id: Int): CityQuestionBank =
        All.firstOrNull { it.id == id } ?: National
}

class CityQuestionBankStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "city_question_bank",
        Context.MODE_PRIVATE
    )

    fun getSelectedCityId(): Int =
        preferences.getInt(KEY_SELECTED_CITY_ID, CityQuestionBanks.National.id)

    fun getSelectedCity(): CityQuestionBank =
        CityQuestionBanks.findById(getSelectedCityId())

    fun select(city: CityQuestionBank) {
        preferences.edit()
            .putInt(KEY_SELECTED_CITY_ID, city.id)
            .apply()
    }

    private companion object {
        const val KEY_SELECTED_CITY_ID = "selected_city_id"
    }
}
