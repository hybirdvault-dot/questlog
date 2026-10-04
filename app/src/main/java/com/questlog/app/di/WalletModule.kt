package com.questlog.app.di

import android.net.Uri
import com.questlog.app.core.wallet.OkHttpHttpDriver
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.networking.HttpNetworkDriver
import com.solana.networking.Rpc20Driver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Call

@Module
@InstallIn(SingletonComponent::class)
object WalletModule {

    private const val DEVNET_RPC_URL = "https://api.devnet.solana.com"
    private const val IDENTITY_URI = "https://github.com/hybirdvault-dot/questlog"
    private const val ICON_URI = "favicon.ico"
    private const val IDENTITY_NAME = "Questlog"

    @Provides
    @Singleton
    fun provideHttpNetworkDriver(callFactory: Call.Factory): HttpNetworkDriver =
        OkHttpHttpDriver(callFactory)

    @Provides
    @Singleton
    fun provideRpc20Driver(driver: HttpNetworkDriver): Rpc20Driver =
        Rpc20Driver(DEVNET_RPC_URL, driver)

    @Provides
    @Singleton
    fun provideMobileWalletAdapter(): MobileWalletAdapter =
        MobileWalletAdapter(
            ConnectionIdentity(
                Uri.parse(IDENTITY_URI),
                Uri.parse(ICON_URI),
                IDENTITY_NAME,
            ),
        )
}
