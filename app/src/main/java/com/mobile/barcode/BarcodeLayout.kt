package com.mobile.barcode

import android.content.Context
import android.graphics.Bitmap
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.view.View
import com.google.zxing.BarcodeFormat
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.MultiFormatWriter
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_barcode.view.*
import com.google.zxing.EncodeHintType
import java.util.*


class BarcodeLayout(context: Context, attributeSet: AttributeSet? = null) : ConstraintLayout(context, attributeSet) {

    var barcode: String? = null
    private set

    var type: BarcodeFormat? = null
    private set

    var disposable: Disposable? = null

    var lastHeight: Int? = null
    var lastWidth: Int? = null

    val set: ConstraintSet

    init {
        View.inflate(context, R.layout.layout_barcode, this)
        set = ConstraintSet()
        set.clone(this)
    }

    fun bind(barcode: String, type: BarcodeFormat) {
        if (barcode != this.barcode || this.type != type) {
            this.barcode = barcode
            this.type = type
            recalculate(measuredWidth, measuredHeight)
            val dimension:String = when (type) {
                BarcodeFormat.QR_CODE -> "1:1"
                else -> "3:1"
            }
            set.setDimensionRatio(barcode_iv.id, dimension)
            set.applyTo(this)
            val lp:ConstraintLayout.LayoutParams? = layoutParams as? ConstraintLayout.LayoutParams
            lp?.let {
                lp.dimensionRatio = dimension
                layoutParams = lp
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculate(w, h)
    }

    private fun recalculate(w: Int, h: Int) {
        if (barcode == null && type == null) {
            return
        }
        if (w == lastWidth && h == lastHeight) {
            return
        }
        disposable?.dispose()
        barcode?.let { barcode->
            disposable = Single.create<Bitmap> {
                val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8")
                hints.put(EncodeHintType.MARGIN, 0)
                val multiFormatWriter = MultiFormatWriter()
                val bitMatrix = multiFormatWriter.encode(barcode, type, w, h, hints)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                if(it.isDisposed) {
                    return@create
                }
                it.onSuccess(bitmap)
            }.compose(com.mobile.rx.Schedulers.singleDefault())
                    .subscribe { t1, t2 ->
                        t1?.let {
                            barcode_iv.setImageBitmap(it)
                        }
                        t2?.let {

                        }
                    }
        }

    }


}