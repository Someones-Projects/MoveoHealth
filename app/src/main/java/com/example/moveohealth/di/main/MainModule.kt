package com.example.moveohealth.di.main

import com.example.moveohealth.repository.AuthRepository
import com.example.moveohealth.repository.MainRepository
import com.example.moveohealth.session.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainModule {

    @ActivityRetainedScoped
    @Provides
    fun provideMainRepository(
        firestore: FirebaseFirestore,
        sessionManager: SessionManager
    ): MainRepository {
        return MainRepository(
            firestore,
            sessionManager
        )
    }
}
