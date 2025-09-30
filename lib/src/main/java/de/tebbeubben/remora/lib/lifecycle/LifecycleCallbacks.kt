package de.tebbeubben.remora.lib.lifecycle

import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
internal class LifecycleCallbacks @Inject constructor(
    private val hooks: Lazy<Set<@JvmSuppressWildcards LibraryLifecycleCallback>>
) : LibraryLifecycleCallback {
    override suspend fun onStartup() = hooks.get().forEach { it.onStartup() }
    override suspend fun onShutdown() = hooks.get().forEach { it.onShutdown() }
    override suspend fun onConfigure() = hooks.get().forEach { it.onConfigure() }
    override suspend fun onInitFirebase() = hooks.get().forEach { it.onInitFirebase() }
    override suspend fun onReset() = hooks.get().forEach { it.onReset() }
}