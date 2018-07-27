package com.mobile.profile

enum class CancellationReason(val code: Int, val reasonName: String){
    TITLE(0, "Reason for cancellation"),
    PRICE(1,"Price"),
    THEATER_SELECTION(2, "Theater selection"),
    EASE_OF_USE(3, "Ease of use"),
    LACK_OF_USE(4, "Lack of use"),
    OTHER(5, "Other");

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
    }
}