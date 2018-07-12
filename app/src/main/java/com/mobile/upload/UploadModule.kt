package com.mobile.upload

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.mobile.application.Application
import com.mobile.network.ApiModule
import com.moviepass.BuildConfig
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ApiModule::class])
class UploadModule {

    @Provides
    @Singleton
    fun provideCredProvider(app: Application): CognitoCachingCredentialsProvider {
        return CognitoCachingCredentialsProvider(
                app,
                BuildConfig.COGNITO_KEY,
                Regions.US_EAST_1);
    }

    @Provides
    @Singleton
    fun provideAmazonS3(credProvider: CognitoCachingCredentialsProvider): AmazonS3 {
        return AmazonS3Client(credProvider)
    }

    @Provides
    @Singleton
    fun provideTransferUtility(app: Application, s3: AmazonS3): TransferUtility {
        return TransferUtility.builder()
                .context(app)
                .awsConfiguration(AWSMobileClient.getInstance().configuration)
                .s3Client(s3)
                .build()
    }

    @Provides
    @Singleton
    fun provideUploadManager(transferUtility: TransferUtility): UploadManager {
        return UploadManagerImpl(transferUtility)
    }
}