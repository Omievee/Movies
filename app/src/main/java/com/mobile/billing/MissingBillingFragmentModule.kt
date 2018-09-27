package com.mobile.billing

import com.mobile.network.Api
import com.mobile.session.SessionManager
import dagger.Module
import dagger.Provides

@Module
class MissingBillingFragmentModule {

    @Provides
    fun providePresenter(fragment: MissingBillingFragment, sessionManager: SessionManager, api: Api): MissingBillingFragmentPresenter {
        return MissingBillingFragmentPresenter(fragment, sessionManager, api)
    }

}
