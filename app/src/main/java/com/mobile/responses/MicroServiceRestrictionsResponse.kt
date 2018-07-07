package com.mobile.responses

import com.mobile.model.Alert
import com.mobile.model.LogoutInfo
import com.mobile.model.PopInfo
import com.mobile.model.canReactivate


class MicroServiceRestrictionsResponse(
        var countDown: Int = 0,
        var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.UNKNOWN,
        var facebook: Boolean = false,
        var has3d: Boolean = false,
        var hasAllFormats: Boolean = false,
        var proofOfPurchaseRequired: Boolean = false,
        var popInfo: PopInfo? = null,
        var hasActiveCard: Boolean = false,
        var alert: Alert? = null,
        var logoutInfo: LogoutInfo? = null,
        var subscriptionActivationRequired:Boolean = false,
        var canReactivate: canReactivate? = null,
        var userSegments:List<Int> = emptyList()
)
