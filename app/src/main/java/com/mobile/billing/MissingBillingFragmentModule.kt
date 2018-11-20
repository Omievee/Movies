package com.mobile.billing

import com.mobile.network.Api
import com.mobile.network.BillingApi
import com.mobile.session.SessionManager
import dagger.Module
import dagger.Provides

@Module
class MissingBillingFragmentModule {

    @Provides
    fun providePresenter(fragment: MissingBillingFragment, sessionManager: SessionManager, api: Api, billingApi:BillingApi): MissingBillingFragmentPresenter {
        return MissingBillingFragmentPresenter(fragment, sessionManager, api, billingApi)
    }

}
