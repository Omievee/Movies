package com.mobile.responses

import com.google.gson.annotations.SerializedName
import com.mobile.model.*
import com.mobile.requests.PendingCharges


class RestrictionsResponse(
        var countDown: Int = 0,
        var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.UNKNOWN,
        var facebook: Boolean = false,
        var has3d: Boolean = false,
        var hasAllFormats: Boolean = false,
        var proofOfPurchaseRequired: Boolean = false,
        var popInfo: PopInfo? = null,
        var hasActiveCard: Boolean = false,
        @SerializedName("capInfo")
        var cappedPlan:CappedPlan? = CappedPlan(),
        var alert: Alert? = null,
        var logoutInfo: LogoutInfo? = null,
        var blockRepeatShowings:Boolean = true,
        var subscriptionActivationRequired:Boolean = false,
        var canReactivate: CanReactivate? = null,
        var userSegments:List<Int> = emptyList(),
        var peakPassInfo:PeakPassInfo = PeakPassInfo(),
        val pendingChargesRestriction:PendingCharges?=null
)
