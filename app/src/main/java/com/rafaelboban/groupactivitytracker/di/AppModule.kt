package com.rafaelboban.groupactivitytracker.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.network.api.AuthInterceptor
import com.rafaelboban.groupactivitytracker.network.api.UserInterceptor
import com.rafaelboban.groupactivitytracker.network.ws.CustomGsonMessageAdapter
import com.rafaelboban.groupactivitytracker.network.ws.EventApi
import com.rafaelboban.groupactivitytracker.network.ws.FlowStreamAdapter
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApiService(client: OkHttpClient): ApiService {
        return Retrofit.Builder()
            //.baseUrl("http://192.168.236.123:8080")
            .baseUrl("http://192.168.5.18:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideEventApi(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): EventApi {
        return Scarlet.Builder()
            .backoffStrategy(ExponentialBackoffStrategy(Constants.RECONNECT_INTERVAL, Constants.RECONNECT_INTERVAL_MAX))
            //.webSocketFactory(okHttpClient.newWebSocketFactory("http://192.168.236.123:8080/ws/event"))
            .webSocketFactory(okHttpClient.newWebSocketFactory("http://192.168.5.18:8080/ws/event"))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory)
            .addMessageAdapterFactory(CustomGsonMessageAdapter.Factory(gson))
            .build()
            .create()
    }

    @Singleton
    @Provides
    fun provideHttpClient(authInterceptor: AuthInterceptor, userInterceptor: UserInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(userInterceptor)
            .addInterceptor(HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(preferences: SharedPreferences) = AuthInterceptor(preferences)

    @Singleton
    @Provides
    fun provideUserInterceptor(preferences: SharedPreferences) = UserInterceptor(preferences)

    @Singleton
    @Provides
    fun provideLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Singleton
    @Provides
    fun provideGson(): Gson = Gson()
}