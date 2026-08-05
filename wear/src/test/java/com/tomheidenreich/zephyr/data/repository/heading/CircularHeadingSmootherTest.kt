package com.tomheidenreich.zephyr.data.repository.heading

import org.junit.Assert.assertEquals
import org.junit.Test

class CircularHeadingSmootherTest {
    @Test
    fun `smooths small heading jitter`() {
        val smoother = CircularHeadingSmoother()

        val first = smoother.update(90.0)
        val second = smoother.update(92.0)
        val third = smoother.update(89.0)

        assertEquals(90.0, first ?: -1.0, 0.0001)
        assertEquals(90.0, second ?: -1.0, 0.0001)
        assertEquals(90.0, third ?: -1.0, 0.0001)
    }

    @Test
    fun `smooths across wrap around`() {
        val smoother = CircularHeadingSmoother()

        smoother.update(358.0)
        val result = smoother.update(2.0)

        assertEquals(358.8, result ?: -1.0, 0.2)
    }
}