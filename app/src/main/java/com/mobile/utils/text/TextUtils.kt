package com.mobile.utils.text

import java.math.BigDecimal
import java.text.DecimalFormat

fun String?.toSentenceCase(): String {
    if (this == null) {
        return ""
    }
    return this.mapIndexed { index, c ->
        if (index == 0) {
            c.toUpperCase().toString()
        } else
            if (c.isUpperCase()) {
                if (this.get(index - 1).isLowerCase()) {
                    (" " + c.toUpperCase())
                } else {
                    c.toString()
                }
            } else
                c.toString()
    }.joinToString("")
}

fun Double?.toCurrency(): String {
    return DecimalFormat.getCurrencyInstance().format(this)
}

fun Double.toFixed(decimalPlaces: Int): Double {
    val bd = BigDecimal(this).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)
    return bd.toDouble()
}

fun Double.toMiles(): Double {
    return this / 1609.34
}