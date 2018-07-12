package com.mobile.upload

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.moviepass.BuildConfig
import io.reactivex.Observable
import java.lang.Exception

class UploadManagerImpl(val transfer: TransferUtility) : UploadManager {
    override fun upload(upload: Upload): Observable<UploadData> {
        return Observable.create { emitter ->
            val data = UploadData(0f, upload)
            val observer = transfer.upload(upload.buildConfig, upload.key, upload.file)
            observer.setTransferListener(object : TransferListener {
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    data.progress = bytesCurrent.div(bytesTotal.toFloat())
                    if (emitter.isDisposed) return
                    emitter.onNext(data)
                }

                override fun onStateChanged(id: Int, state: TransferState) {
                    if (emitter.isDisposed) return
                    when (state) {
                        TransferState.CANCELED -> emitter.onComplete()
                        TransferState.COMPLETED -> {
                            emitter.onNext(data.apply { progress = 1f })
                            emitter.onComplete()
                        }
                        TransferState.FAILED -> emitter.onComplete()
                    }
                }

                override fun onError(id: Int, ex: Exception) {
                    if (emitter.isDisposed) return
                    emitter.onError(ex)
                }

            })
        }
    }
}