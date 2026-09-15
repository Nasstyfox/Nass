package com.example.nass.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun price_formatsWholeNumberWithTwoDecimals() {
        assertEquals("R 100.00", Formatters.price(100.0))
    }

    @Test
    fun price_formatsDecimalCorrectly() {
        assertEquals("R 349.99", Formatters.price(349.99))
    }

    @Test
    fun price_roundsToOneDecimal() {
        assertEquals("R 49.90", Formatters.price(49.9))
    }

    @Test
    fun price_handlesZero() {
        assertEquals("R 0.00", Formatters.price(0.0))
    }

    @Test
    fun plural_usesSingularForOne() {
        assertEquals("1 item", Formatters.plural(1, "item"))
    }

    @Test
    fun plural_usesPluralForMany() {
        assertEquals("5 items", Formatters.plural(5, "item"))
    }

    @Test
    fun plural_usesPluralForZero() {
        assertEquals("0 items", Formatters.plural(0, "item"))
    }
}