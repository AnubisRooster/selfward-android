package com.selfward.core.catalog

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelRefusalTest {

    /** The message that started all of this, verbatim from OpenRouter. */
    @Test
    fun aModelGatedToAgenticHarnessesIsARefusal() {
        assertTrue(
            ModelRefusal.isPermanent(
                "Chat request failed: thinkingmachines/inkling-small:free is only " +
                    "available on agentic harnesses. Try plugging it into a coding agent " +
                    "or productivity app listed on https://openrouter.ai/apps"
            )
        )
    }

    @Test
    fun anUnknownOrWithdrawnModelIsARefusal() {
        assertTrue(ModelRefusal.isPermanent("Chat request failed: not a valid model id"))
        assertTrue(ModelRefusal.isPermanent("No endpoints found for vendor/gone:free"))
        assertTrue(ModelRefusal.isPermanent("model not found"))
    }

    /**
     * Setting a model aside for a rate limit would burn the free list down in an
     * afternoon and leave someone stuck on a worse model for good.
     */
    @Test
    fun aRateLimitIsNotARefusal() {
        assertFalse(ModelRefusal.isPermanent("Rate limit exceeded, please try again later"))
        assertFalse(ModelRefusal.isPermanent("You are being rate-limited"))
    }

    @Test
    fun aPassingOutageIsNotARefusal() {
        assertFalse(ModelRefusal.isPermanent("Provider temporarily unavailable"))
        assertFalse(ModelRefusal.isPermanent("upstream timeout"))
        assertFalse(ModelRefusal.isPermanent("Service overloaded, try again"))
        assertFalse(ModelRefusal.isPermanent("503 from upstream"))
    }

    /**
     * A message carrying both reads as transient. Retrying a model that might
     * still work costs one request; discarding one that does costs the client a
     * better model permanently.
     */
    @Test
    fun anAmbiguousMessageIsTreatedAsTransient() {
        assertFalse(
            ModelRefusal.isPermanent("model not found right now, temporarily unavailable")
        )
    }

    @Test
    fun anUnrecognisedOrAbsentMessageIsNotARefusal() {
        assertFalse(ModelRefusal.isPermanent("something went wrong"))
        assertFalse(ModelRefusal.isPermanent(null))
        assertFalse(ModelRefusal.isPermanent(""))
    }

    @Test
    fun matchingIgnoresCase() {
        assertTrue(ModelRefusal.isPermanent("ONLY AVAILABLE ON AGENTIC HARNESSES"))
    }
}
