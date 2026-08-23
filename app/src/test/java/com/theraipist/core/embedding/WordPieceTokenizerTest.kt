package com.theraipist.core.embedding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verified against the real Xenova/all-MiniLM-L6-v2 vocab.txt (bundled as a test
 * resource) - expected splits below were computed by independently running the
 * reference WordPiece algorithm (greedy longest-match-first) against that exact
 * vocab file, not guessed.
 */
class WordPieceTokenizerTest {

    private lateinit var vocab: Map<String, Int>
    private lateinit var tokenizer: WordPieceTokenizer

    @Before
    fun setup() {
        val text = javaClass.classLoader!!.getResourceAsStream("minilm_vocab.txt")!!
            .bufferedReader().readText()
        vocab = WordPieceTokenizer.loadVocab(text)
        tokenizer = WordPieceTokenizer(vocab)
    }

    @Test
    fun loadsFullVocab() {
        assertEquals(30522, vocab.size)
        assertEquals(0, vocab["[PAD]"])
        assertTrue(vocab.containsKey("[CLS]"))
        assertTrue(vocab.containsKey("[SEP]"))
        assertTrue(vocab.containsKey("[UNK]"))
    }

    @Test
    fun splitsKnownCompoundWords() {
        assertEquals(listOf("therapist", "##s"), tokenizer.wordpiece("therapists"))
        assertEquals(listOf("journal", "##ing"), tokenizer.wordpiece("journaling"))
        assertEquals(listOf("ground", "##ing"), tokenizer.wordpiece("grounding"))
        assertEquals(listOf("mind", "##fulness"), tokenizer.wordpiece("mindfulness"))
        assertEquals(listOf("psycho", "##ther", "##ap", "##ist"), tokenizer.wordpiece("psychotherapist"))
        assertEquals(listOf("un", "##hel", "##pf", "##ul"), tokenizer.wordpiece("unhelpful"))
    }

    @Test
    fun keepsWholeVocabWordsIntact() {
        assertEquals(listOf("reflective"), tokenizer.wordpiece("reflective"))
        assertEquals(listOf("overwhelmed"), tokenizer.wordpiece("overwhelmed"))
    }

    @Test
    fun basicTokenizeLowercasesAndSplitsPunctuation() {
        assertEquals(
            listOf("hello", ",", "world", "!"),
            tokenizer.basicTokenize("Hello, World!")
        )
    }

    @Test
    fun basicTokenizeStripsAccents() {
        assertEquals(listOf("cafe"), tokenizer.basicTokenize("café"))
    }

    @Test
    fun tokenizeWrapsWithClsAndSep() {
        val result = tokenizer.tokenize("grounding")
        val ids = result.inputIds.toList()
        assertEquals(vocab["[CLS]"]!!.toLong(), ids.first())
        assertEquals(vocab["[SEP]"]!!.toLong(), ids.last())
        assertEquals(listOf(vocab["[CLS]"]!!.toLong(), vocab["ground"]!!.toLong(), vocab["##ing"]!!.toLong(), vocab["[SEP]"]!!.toLong()), ids)
        assertEquals(ids.size, result.attentionMask.size)
        assertTrue(result.attentionMask.all { it == 1L })
        assertTrue(result.tokenTypeIds.all { it == 0L })
    }

    @Test
    fun tokenizeTruncatesToMaxSequenceLength() {
        val shortTokenizer = WordPieceTokenizer(vocab, TokenizerConfig(maxSequenceLength = 5))
        val longText = (1..20).joinToString(" ") { "grounding" }
        val result = shortTokenizer.tokenize(longText)
        assertEquals(5, result.inputIds.size)
    }

    @Test
    fun unrepresentableCharacterMapsToUnk() {
        // U+2603 SNOWMAN - not in the vocab and shares no subword with anything that is.
        assertEquals(listOf("[UNK]"), tokenizer.wordpiece("☃"))
    }
}
