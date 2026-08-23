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
            sizeBytes = 668_788_096L,
            minRamBytes = 2_000_000_000L,
            downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            sha256 = "9fecc3b3cd76bba89d504f29b616eedf7da85b96540e490ca5824d3f7d2776a0"
        ),
        LocalModel(
            id = "qwen2.5-1.5b",
            name = "Qwen2.5 1.5B Instruct",
            fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            sizeBytes = 1_117_320_736L,
            minRamBytes = 3_000_000_000L,
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            sha256 = "6a1a2eb6d15622bf3c96857206351ba97e1af16c30d7a74ee38970e434e9407e"
        ),
        LocalModel(
            id = "phi-3.5-mini",
            name = "Phi-3.5-mini Instruct",
            fileName = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sizeBytes = 2_393_232_672L,
            minRamBytes = 4_500_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sha256 = "e4165e3a71af97f1b4820da61079826d8752a2088e313af0c7d346796c38eff5"
        ),
        LocalModel(
            id = "llama-3.2-3b",
            name = "Llama 3.2 3B Instruct",
            fileName = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            sizeBytes = 2_019_377_696L,
            minRamBytes = 6_000_000_000L,
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            sha256 = "6c1a2b41161032677be168d354123594c0e6e67d2b9227c84f296ad037c728ff"
        )
    )

    fun byId(id: String): LocalModel? = allModels.firstOrNull { it.id == id }
}
