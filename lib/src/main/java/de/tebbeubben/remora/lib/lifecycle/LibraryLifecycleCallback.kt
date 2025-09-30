package de.tebbeubben.remora.lib.lifecycle

internal interface LibraryLifecycleCallback {
    suspend fun onStartup() {}
    suspend fun onShutdown() {}
    suspend fun onConfigure() {}
    suspend fun onInitFirebase() {}
    suspend fun onReset() {}
}