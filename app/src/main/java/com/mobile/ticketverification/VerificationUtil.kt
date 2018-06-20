package com.mobile.ticketverification

import com.google.android.gms.vision.text.TextBlock

fun TextBlock.toText(): com.mobile.ticketverification.TextBlock {
    return com.mobile.ticketverification.TextBlock(
            text = this.value,
            point = this.cornerPoints?.map {
                VisionPoint(it.x, it.y)
            } ?: emptyList()
    )
}