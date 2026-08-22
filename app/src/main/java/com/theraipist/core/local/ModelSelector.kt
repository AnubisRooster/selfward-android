package com.theraipist.core.local

/**
 * Picks the largest model that fits the device's available RAM, so we maximize
 * quality without OOM-ing. Returns null when no catalog model fits.
 */
object ModelSelector {

    fun select(
        freeRamBytes: Long,
        models: List<LocalModel> = GGUFModelCatalog.allModels
    ): LocalModel? {
        return models
            .filter { it.minRamBytes <= freeRamBytes }
            .maxByOrNull { it.sizeBytes }
    }
}
