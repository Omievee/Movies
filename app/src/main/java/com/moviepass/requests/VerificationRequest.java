package com.moviepass.requests;

import com.moviepass.utils.AppUtils;

/**
 * Created by anubis on 7/17/17.
 */

public class VerificationRequest {

    String device_name;
    String os_version;
    String upload_method;


    public VerificationRequest() {
        this.device_name = AppUtils.getDeviceName();
        this.os_version = AppUtils.getOsCodename();
        this.upload_method = "DIRECT_UPLOAD";
    }
}
