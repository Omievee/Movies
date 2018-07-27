package com.mobile.camera

import com.google.android.gms.vision.barcode.Barcode
import com.mobile.ticketverification.VisionPoint

class BarcodeData(val format: BarcodeFormat,
                  val rawValue: String,
                  val displayValue: String,
                  val points: List<VisionPoint>)

enum class BarcodeFormat(val code: Int) {
    CODE_128(Barcode.CODE_128),
    CODE_39(Barcode.CODE_39),
    CODE_93(Barcode.CODE_93),
    CODABAR(Barcode.CODABAR),
    DATA_MATRIX(Barcode.DATA_MATRIX),
    EAN_13(Barcode.EAN_13),
    EAN_8(Barcode.EAN_8),
    ITF(Barcode.ITF),
    QR_CODE(Barcode.QR_CODE),
    UPC_A(Barcode.UPC_A),
    UPC_E(Barcode.UPC_E),
    PDF417(Barcode.PDF417),
    AZTEC(Barcode.AZTEC)
}