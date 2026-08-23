package com.theraipist.core.embedding

import java.text.Normalizer

data class TokenizerConfig(
    val doLowerCase: Boolean = true,
    val maxSequenceLength: Int = 256,
    val unkToken: String = "[UNK]",
    val clsToken: String = "[CLS]",
    val sepToken: String = "[SEP]"
)

data class TokenizedInput(val inputIds: LongArray, val attentionMask: LongArray, val tokenTypeIds: LongArray)

/**
 * BERT-style WordPiece tokenizer, matching HuggingFace's BasicTokenizer +
 * WordpieceTokenizer algorithm (greedy longest-match-first subword splitting).
 * Pure Kotlin, no ONNX/Android dependency, so it's independently testable
 * against a real vocab file.
 */
class WordPieceTokenizer(
    private val vocab: Map<String, Int>,
    private val config: TokenizerConfig = TokenizerConfig()
) {
    private val maxInputCharsPerWord = 100

    fun tokenize(text: String): TokenizedInput {
        val wordpieces = basicTokenize(text).flatMap { wordpiece(it) }
        val truncated = wordpieces.take((config.maxSequenceLength - 2).coerceAtLeast(0))
        val tokens = listOf(config.clsToken) + truncated + listOf(config.sepToken)
        val unkId = (vocab[config.unkToken] ?: 0).toLong()
        val ids = LongArray(tokens.size) { i -> vocab[tokens[i]]?.toLong() ?: unkId }
        val mask = LongArray(ids.size) { 1L }
        val typeIds = LongArray(ids.size) { 0L }
        return TokenizedInput(ids, mask, typeIds)
    }

    /** Splits text into words: clean → space CJK chars → lowercase/strip accents → split on punctuation. */
    fun basicTokenize(text: String): List<String> {
        val cleaned = cleanText(text)
        val spacedCjk = spaceCjkChars(cleaned)
        val splitTokens = mutableListOf<String>()
        for (rawToken in whitespaceTokenize(spacedCjk)) {
            val token = if (config.doLowerCase) stripAccents(rawToken.lowercase()) else rawToken
            splitTokens.addAll(splitOnPunctuation(token))
        }
        return whitespaceTokenize(splitTokens.joinToString(" "))
    }

    /** Greedy longest-match-first subword split of a single already-basic-tokenized word. */
    fun wordpiece(token: String): List<String> {
        if (token.length > maxInputCharsPerWord) return listOf(config.unkToken)
        val subTokens = mutableListOf<String>()
        var start = 0
        while (start < token.length) {
            var end = token.length
            var matched: String? = null
            while (start < end) {
                val substr = if (start > 0) "##${token.substring(start, end)}" else token.substring(start, end)
                if (vocab.containsKey(substr)) {
                    matched = substr
                    break
                }
                end -= 1
            }
            if (matched == null) return listOf(config.unkToken)
            subTokens.add(matched)
            start = end
        }
        return subTokens
    }

    private fun cleanText(text: String): String {
        val sb = StringBuilder(text.length)
        for (c in text) {
            if (c.code == 0 || c.code == 0xFFFD || isControlChar(c)) continue
            sb.append(if (isWhitespaceChar(c)) ' ' else c)
        }
        return sb.toString()
    }

    private fun spaceCjkChars(text: String): String {
        val sb = StringBuilder(text.length * 2)
        for (c in text) {
            if (isCjkChar(c.code)) sb.append(' ').append(c).append(' ') else sb.append(c)
        }
        return sb.toString()
    }

    private fun isCjkChar(cp: Int): Boolean =
        cp in 0x4E00..0x9FFF || cp in 0x3400..0x4DBF || cp in 0xF900..0xFAFF

    private fun isControlChar(c: Char): Boolean {
        if (c == '\t' || c == '\n' || c == '\r') return false
        val type = Character.getType(c)
        return type == Character.CONTROL.toInt() || type == Character.FORMAT.toInt()
    }

    private fun isWhitespaceChar(c: Char): Boolean {
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') return true
        return Character.getType(c) == Character.SPACE_SEPARATOR.toInt()
    }

    private fun stripAccents(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }

    private fun splitOnPunctuation(text: String): List<String> {
        val output = mutableListOf<StringBuilder>()
        var startNewWord = true
        for (c in text) {
            if (isPunctuation(c)) {
                output.add(StringBuilder().append(c))
                startNewWord = true
            } else {
                if (startNewWord) output.add(StringBuilder())
                output.last().append(c)
                startNewWord = false
            }
        }
        return output.map { it.toString() }
    }

    private fun isPunctuation(c: Char): Boolean {
        val code = c.code
        if (code in 33..47 || code in 58..64 || code in 91..96 || code in 123..126) return true
        val type = Character.getType(c)
        return type == Character.CONNECTOR_PUNCTUATION.toInt() ||
            type == Character.DASH_PUNCTUATION.toInt() ||
            type == Character.START_PUNCTUATION.toInt() ||
            type == Character.END_PUNCTUATION.toInt() ||
            type == Character.INITIAL_QUOTE_PUNCTUATION.toInt() ||
            type == Character.FINAL_QUOTE_PUNCTUATION.toInt() ||
            type == Character.OTHER_PUNCTUATION.toInt()
    }

    private fun whitespaceTokenize(text: String): List<String> =
        text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    companion object {
        fun loadVocab(text: String): Map<String, Int> {
            val vocab = LinkedHashMap<String, Int>()
            text.lineSequence().forEachIndexed { index, line ->
                val token = line.trimEnd('\n', '\r')
                if (token.isNotEmpty()) vocab[token] = index
            }
            return vocab
        }
    }
}
