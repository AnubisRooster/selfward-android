package com.theraipist.core.local

/**
 * Portable catalog of GGUF models the app can download and run on-device.
 * Kept in sync with the iOS `LLMModelCatalog`.
 */
object GGUFModelCatalog {

    val allModels: List<LocalModel> = listOf(
        LocalModel(
            id = "tinyllama-1.1b",
            name = "TinyLlama 1.1B Chat",
            fileName = "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            sizeBytes = 700_000_000L,
            minRamBytes = 2_000_000_000L,
            downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        ),
        LocalModel(
            id = "qwen2.5-1.5b",
            name = "Qwen2.5 1.5B Instruct",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            sizeBytes = 1_000_000_000L,
            minRamBytes = 3_000_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf"
        ),
        LocalModel(
            id = "phi-3.5-mini",
            name = "Phi-3.5-mini Instruct",
            fileName = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sizeBytes = 2_400_000_000L,
            minRamBytes = 4_500_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf"
        ),
        LocalModel(
            id = "llama-3.2-3b",
            name = "Llama 3.2 3B Instruct",
            fileName = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            sizeBytes = 2_000_000_000L,
            minRamBytes = 6_000_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf"
        )
    )

    fun byId(id: String): LocalModel? = allModels.firstOrNull { it.id == id }
}
