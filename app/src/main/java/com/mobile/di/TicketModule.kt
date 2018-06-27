package com.mobile.di

import com.mobile.network.Api
import com.mobile.network.ApiModule
import com.mobile.tickets.TicketManager
import com.mobile.tickets.TicketManagerImpl
import dagger.Module
import dagger.Provides

@Module(includes = [ApiModule::class])
class TicketModule {

    @Provides
    fun ticketManager(api: Api):TicketManager {
        return TicketManagerImpl(api)
    }
}