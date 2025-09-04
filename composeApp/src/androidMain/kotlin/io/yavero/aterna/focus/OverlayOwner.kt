package io.yavero.aterna.focus

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Minimal owner to host a ComposeView from a Service (not an Activity).
 */
internal class OverlayOwner :
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val controller = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()


    init {
        controller.performAttach()
        controller.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = vmStore

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        vmStore.clear()
    }
}
