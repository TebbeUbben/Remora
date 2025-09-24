package de.tebbeubben.remora.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.tebbeubben.remora.lib.LibraryMode
import de.tebbeubben.remora.lib.RemoraLib
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RemoraModule {

    @Provides
    @Singleton
    fun providesRemoraLib(@ApplicationContext context: Context): RemoraLib = RemoraLib.initialize(context, LibraryMode.FOLLOWER)

}