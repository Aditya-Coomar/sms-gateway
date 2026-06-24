package com.aditya.simgateway.core.common

import java.security.SecureRandom

object CompactIdGenerator {

    private const val RADIX = 36
    private const val SEQUENCE_SPACE = RADIX * RADIX
    private const val RANDOM_SPACE = RADIX * RADIX

    private val random = SecureRandom()

    private var lastTimestampMillis: Long = 0L
    private var sequence: Int = 0

    @Synchronized
    fun newId(prefix: String): String {
        val now = System.currentTimeMillis()
        sequence = if (now == lastTimestampMillis) {
            (sequence + 1) % SEQUENCE_SPACE
        } else {
            lastTimestampMillis = now
            0
        }

        val timePart = now.toString(RADIX).padStart(8, '0')
        val sequencePart = sequence.toString(RADIX).padStart(2, '0')
        val randomPart = random.nextInt(RANDOM_SPACE).toString(RADIX).padStart(2, '0')

        return "${prefix}_${timePart}${sequencePart}${randomPart}"
    }
}
