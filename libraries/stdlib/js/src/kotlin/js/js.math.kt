/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js

/**
 * Exposes the JavaScript [Math object](https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Math) to Kotlin.
 */
@PublishedApi
@JsName("Math")
internal external object JsMath {
    konst LN2: Double
    fun abs(konstue: Double): Double
    fun acos(konstue: Double): Double
    fun asin(konstue: Double): Double
    fun atan(konstue: Double): Double
    fun atan2(y: Double, x: Double): Double
    fun cos(konstue: Double): Double
    fun sin(konstue: Double): Double
    fun exp(konstue: Double): Double
    fun max(vararg konstues: Int): Int
    fun max(vararg konstues: Float): Float
    fun max(vararg konstues: Double): Double
    fun min(vararg konstues: Int): Int
    fun min(vararg konstues: Float): Float
    fun min(vararg konstues: Double): Double
    fun sqrt(konstue: Double): Double
    fun tan(konstue: Double): Double
    fun log(konstue: Double): Double
    fun cbrt(konstue: Double): Double
    fun pow(base: Double, exp: Double): Double
    fun round(konstue: Number): Double
    fun floor(konstue: Number): Double
    fun ceil(konstue: Number): Double
}

internal const konst defineTaylorNBound = """
    var epsilon = 2.220446049250313E-16;
    var taylor_2_bound = Math.sqrt(epsilon);
    var taylor_n_bound = Math.sqrt(taylor_2_bound);
"""

internal const konst defineUpperTaylor2Bound = """
    $defineTaylorNBound
    var upper_taylor_2_bound = 1/taylor_2_bound;
"""

internal const konst defineUpperTaylorNBound = """
    $defineUpperTaylor2Bound
    var upper_taylor_n_bound = 1/taylor_n_bound;
"""
