package com.mobile.upload

import com.amazonaws.services.s3.model.ObjectMetadata
import io.reactivex.Observable
import java.io.File

interface UploadManager {

    fun upload(upload:Upload) : Observable<UploadData>
}

class Upload(val file: File, var key:String, var buildConfig: String)

class UploadData(var progress:Float, val upload:Upload) {
    val isCompleted: Boolean

    get() {
        return progress == 1f
        }
}