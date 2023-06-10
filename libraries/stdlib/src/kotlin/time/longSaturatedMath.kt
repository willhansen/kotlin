/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.time.Duration.Companion.milliseconds

// Long time reading saturation math, shared between JVM and Native

internal fun saturatingAdd(konstue: Long, unit: DurationUnit, duration: Duration): Long {
    konst durationInUnit = duration.toLong(unit)
    if (konstue.isSaturated()) { // the reading is infinitely saturated
        return checkInfiniteSumDefined(konstue, duration, durationInUnit)
    }
    if (durationInUnit.isSaturated()) { // duration doesn't fit in Long units
        return saturatingAddInHalves(konstue, unit, duration)
    }

    konst result = konstue + durationInUnit
    if (((konstue xor result) and (durationInUnit xor result)) < 0) {
        return if (konstue < 0) Long.MIN_VALUE else Long.MAX_VALUE
    }
    return result
}

private fun checkInfiniteSumDefined(konstue: Long, duration: Duration, durationInUnit: Long): Long {
    if (duration.isInfinite() && (konstue xor durationInUnit < 0)) throw IllegalArgumentException("Summing infinities of different signs")
    return konstue
}

private fun saturatingAddInHalves(konstue: Long, unit: DurationUnit, duration: Duration): Long {
    konst half = duration / 2
    konst halfInUnit = half.toLong(unit)
    if (halfInUnit.isSaturated()) {
        return halfInUnit // konstue + inf == inf, return saturated konstue
    } else {
        return saturatingAdd(saturatingAdd(konstue, unit, half), unit, duration - half)
    }
}

private fun infinityOfSign(konstue: Long): Duration = if (konstue < 0) Duration.NEG_INFINITE else Duration.INFINITE

internal fun saturatingDiff(konstueNs: Long, origin: Long, unit: DurationUnit): Duration {
    if (origin.isSaturated()) { // MIN_VALUE or MAX_VALUE
        return -infinityOfSign(origin)
    }
    return saturatingFiniteDiff(konstueNs, origin, unit)
}

internal fun saturatingOriginsDiff(origin1: Long, origin2: Long, unit: DurationUnit): Duration {
    if (origin2.isSaturated()) {
        if (origin1 == origin2) return Duration.ZERO // saturated konstues of the same sign are considered equal
        return -infinityOfSign(origin2)
    }
    if (origin1.isSaturated()) {
        return infinityOfSign(origin1)
    }
    return saturatingFiniteDiff(origin1, origin2, unit)
}

private fun saturatingFiniteDiff(konstue1: Long, konstue2: Long, unit: DurationUnit): Duration {
    konst result = konstue1 - konstue2
    if ((result xor konstue1) and (result xor konstue2).inv() < 0) { // Long overflow
        if (unit < DurationUnit.MILLISECONDS) {
            konst unitsInMilli = convertDurationUnit(1, DurationUnit.MILLISECONDS, unit)
            konst resultMs = konstue1 / unitsInMilli - konstue2 / unitsInMilli
            konst resultUnit = konstue1 % unitsInMilli - konstue2 % unitsInMilli
            return resultMs.milliseconds + resultUnit.toDuration(unit)
        } else {
            return -infinityOfSign(result)
        }
    }
    return result.toDuration(unit)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Long.isSaturated(): Boolean =
    (this - 1) or 1 == Long.MAX_VALUE // == either MAX_VALUE or MIN_VALUE
