package com.mobile.ticketverification

import com.google.android.gms.vision.barcode.Barcode
import com.mobile.camera.BarcodeData
import com.mobile.camera.BarcodeFormat

fun toBarcode(barcode: Barcode): BarcodeData? {
    val value = barcode.displayValue ?: return null
    val raw = barcode.rawValue ?: return null
    val type = BarcodeFormat.values().find {
        it.code == barcode.format
    } ?: return null
    val points = barcode.cornerPoints?.map {
        VisionPoint(it.x, it.y)
    } ?: return null
    return BarcodeData(
            format = type,
            rawValue = raw,
            displayValue = value,
            points = barcode.cornerPoints.map {
                VisionPoint(it.x, it.y)
            }
    )
}