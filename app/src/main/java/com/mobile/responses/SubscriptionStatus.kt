package com.mobile.responses

enum class SubscriptionStatus {
    ACTIVE,
    ACTIVE_FREE_TRIAL,
    PENDING_FREE_TRIAL,
    ENDED_FREE_TRIAL,
    CANCELLED,
    CANCELLED_PAST_DUE,
    MISSING,
    PAST_DUE,
    PENDING_ACTIVATION,
    UNKNOWN
}