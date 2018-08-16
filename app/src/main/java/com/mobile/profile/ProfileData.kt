package com.mobile.profile

import android.support.v7.util.DiffUtil

class ProfileData(
        val data: List<ProfilePresentation>,
        val diffResult: DiffUtil.DiffResult
)