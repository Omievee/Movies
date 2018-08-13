package com.mobile.profile

enum class CancellationReason(val code: Int, val reasonName: String){
    PRICE(0,"Price"),
    THEATER_SELECTION(1, "Theater selection"),
    EASE_OF_USE(2, "Ease of use"),
    LACK_OF_USE(3, "Lack of use"),
    OTHER(4, "Other");

    companion object {

        private val map = CancellationReason.values().associateBy(CancellationReason::reasonName)

        fun getList(): List<String>{
            val list: MutableList<String> = mutableListOf()
            map.iterator().forEach {
                list.add(it.key)
            }
            return list.toList()
        }

        fun getReasonNumber(reason: String) : Long{
            return map[reason]?.code?.toLong() ?: 0
        }

        fun getTitle() : String{
            return "Reason for cancellation"
        }
    }
}