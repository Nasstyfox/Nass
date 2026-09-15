package com.example.nass.util

import java.util.Locale

object Formatters {
    /** R 349.99 — always 2 decimals, always "R " prefix. */
    fun price(value: Double): String = String.format(Locale.US, "R %.2f", value)

    /** 1 item / 5 items — plural helper. */
    fun plural(count: Int, singular: String, plural: String = "${singular}s"): String =
        "$count ${if (count == 1) singular else plural}"
}