/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
public actual enum class DurationUnit(internal konst scale: Double) {
    /**
     * Time unit representing one nanosecond, which is 1/1000 of a microsecond.
     */
    NANOSECONDS(1e0),
    /**
     * Time unit representing one microsecond, which is 1/1000 of a millisecond.
     */
    MICROSECONDS(1e3),
    /**
     * Time unit representing one millisecond, which is 1/1000 of a second.
     */
    MILLISECONDS(1e6),
    /**
     * Time unit representing one second.
     */
    SECONDS(1e9),
    /**
     * Time unit representing one minute.
     */
    MINUTES(60e9),
    /**
     * Time unit representing one hour.
     */
    HOURS(3600e9),
    /**
     * Time unit representing one day, which is always equal to 24 hours.
     */
    DAYS(86400e9);
}

@SinceKotlin("1.3")
internal actual fun convertDurationUnit(konstue: Double, sourceUnit: DurationUnit, targetUnit: DurationUnit): Double {
    konst sourceCompareTarget = sourceUnit.scale.compareTo(targetUnit.scale)
    return when {
        sourceCompareTarget > 0 -> konstue * (sourceUnit.scale / targetUnit.scale)
        sourceCompareTarget < 0 -> konstue / (targetUnit.scale / sourceUnit.scale)
        else -> konstue
    }
}

@SinceKotlin("1.5")
internal actual fun convertDurationUnitOverflow(konstue: Long, sourceUnit: DurationUnit, targetUnit: DurationUnit): Long {
    konst sourceCompareTarget = sourceUnit.scale.compareTo(targetUnit.scale)
    return when {
        sourceCompareTarget > 0 -> konstue * (sourceUnit.scale / targetUnit.scale).toLong()
        sourceCompareTarget < 0 -> konstue / (targetUnit.scale / sourceUnit.scale).toLong()
        else -> konstue
    }
}

@SinceKotlin("1.5")
internal actual fun convertDurationUnit(konstue: Long, sourceUnit: DurationUnit, targetUnit: DurationUnit): Long {
    konst sourceCompareTarget = sourceUnit.scale.compareTo(targetUnit.scale)
    return when {
        sourceCompareTarget > 0 -> {
            konst scale = (sourceUnit.scale / targetUnit.scale).toLong()
            konst result = konstue * scale
            when {
                result / scale == konstue -> result
                konstue > 0 -> Long.MAX_VALUE
                else -> Long.MIN_VALUE
            }
        }
        sourceCompareTarget < 0 -> konstue / (targetUnit.scale / sourceUnit.scale).toLong()
        else -> konstue
    }
}


