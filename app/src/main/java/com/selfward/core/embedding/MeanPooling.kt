package com.selfward.core.embedding

/**
 * Mean pooling over a transformer's per-token hidden states, matching
 * sentence-transformers' `Pooling` module for MiniLM: average the vectors of the
 * real tokens and ignore padding.
 *
 * Kept engine-free and separate from the ONNX Runtime call so it can be tested.
 * Getting this wrong — most easily by averaging over the sequence length instead
 * of the real-token count — still yields plausible-looking unit-length-ish
 * vectors, so it would never crash and would only surface as quietly worse
 * retrieval.
 */
object MeanPooling {

    /**
     * @param tokenEmbeddings one hidden-state vector per token position
     * @param attentionMask 1 for a real token, 0 for padding; must line up with
     *   [tokenEmbeddings], since a mismatch means the model returned a different
     *   shape than the tokenizer produced and any pooled result would be wrong.
     */
    fun pool(tokenEmbeddings: Array<FloatArray>, attentionMask: LongArray): FloatArray {
        val hiddenSize = tokenEmbeddings.firstOrNull()?.size ?: return FloatArray(0)
        require(tokenEmbeddings.size == attentionMask.size) {
            "token/mask length mismatch (${tokenEmbeddings.size} vs ${attentionMask.size})"
        }
        val sum = FloatArray(hiddenSize)
        var realTokens = 0f
        for (i in tokenEmbeddings.indices) {
            if (attentionMask[i] == 0L) continue
            realTokens += 1f
            val embedding = tokenEmbeddings[i]
            for (j in 0 until hiddenSize) sum[j] += embedding[j]
        }
        if (realTokens == 0f) return sum
        for (j in 0 until hiddenSize) sum[j] /= realTokens
        return sum
    }
}
