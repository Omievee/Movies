package com.mobile.utils.text

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