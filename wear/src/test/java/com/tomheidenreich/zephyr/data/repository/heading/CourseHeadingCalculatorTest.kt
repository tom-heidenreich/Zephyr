package com.tomheidenreich.zephyr.data.repository.heading

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CourseHeadingCalculatorTest {
    @Test
    fun `uses explicit bearing when moving`() {
        val previous = HeadingSample(latitude = 0.0, longitude = 0.0)
        val current = HeadingSample(latitude = 0.0, longitude = 1.0, speedMps = 1.5, bearingDegrees = 42.0)

        val result = CourseHeadingCalculator.resolveHeading(previous, current)

        assertEquals(42.0, result ?: -1.0, 0.0001)
    }

    @Test
    fun `derives course over ground from successive positions`() {
        val previous = HeadingSample(latitude = 0.0, longitude = 0.0)
        val current = HeadingSample(latitude = 0.0, longitude = 1.0)

        val result = CourseHeadingCalculator.resolveHeading(previous, current)

        assertEquals(90.0, result ?: -1.0, 0.0001)
    }

    @Test
    fun `returns null when movement is too small`() {
        val previous = HeadingSample(latitude = 0.0, longitude = 0.0)
        val current = HeadingSample(latitude = 0.000001, longitude = 0.000001)

        val result = CourseHeadingCalculator.resolveHeading(previous, current)

        assertNull(result)
    }
}