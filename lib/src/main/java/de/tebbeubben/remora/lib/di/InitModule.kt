package de.tebbeubben.remora.lib.di

import android.content.Context
import dagger.Module
import dagger.Provides
import de.tebbeubben.remora.lib.LibraryMode

@Module
internal class InitModule(
    private val context: Context,
    private val mode: LibraryMode
) {

    @Provides
    @ApplicationContext
    fun providesApplicationContext(): Context = context

    @Provides
    fun providesLibraryMode(): LibraryMode = mode
}