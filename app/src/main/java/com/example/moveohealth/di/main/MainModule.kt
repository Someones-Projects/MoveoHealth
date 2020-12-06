package com.example.moveohealth.di.main

import com.example.moveohealth.api.NotificationAPI
import com.example.moveohealth.repository.MainRepository
import com.example.moveohealth.session.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainModule {

    @ActivityRetainedScoped
    @Provides
    fun provideNotificationAPI(retrofitBuilder: Retrofit.Builder): NotificationAPI {
        return retrofitBuilder
            .build()
            .create(NotificationAPI::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideMainRepository(
        firestore: FirebaseFirestore,
        sessionManager: SessionManager,
        notificationAPI: NotificationAPI
    ): MainRepository {
        return MainRepository(
            firestore,
            sessionManager,
            notificationAPI
        )
    }


}
