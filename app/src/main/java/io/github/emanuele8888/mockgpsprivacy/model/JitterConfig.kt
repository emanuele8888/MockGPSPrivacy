package io.github.emanuele8888.mockgpsprivacy.model

/**
 * Model for jitter configuration (random variation of the position).
 *
 * @property enabled Whether the jitter simulation is active.
 * @property rangeMeters The maximum displacement radius in meters from the base point.
 */
data class JitterConfig(
    val enabled: Boolean = false,
    val rangeMeters: Double = 10.0
)
