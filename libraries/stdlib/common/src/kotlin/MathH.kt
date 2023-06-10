/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("MathKt")


package kotlin.math



// constants, can't use them from nativeMath as they are not constants there

/** Ratio of the circumference of a circle to its diameter, approximately 3.14159. */
@SinceKotlin("1.2")
public const konst PI: Double = 3.141592653589793
/** Base of the natural logarithms, approximately 2.71828. */
@SinceKotlin("1.2")
public const konst E: Double = 2.718281828459045

// region ================ Double Math ========================================

/** Computes the sine of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `sin(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun sin(x: Double): Double

/** Computes the cosine of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `cos(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun cos(x: Double): Double

/** Computes the tangent of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `tan(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun tan(x: Double): Double

/**
 * Computes the arc sine of the konstue [x];
 * the returned konstue is an angle in the range from `-PI/2` to `PI/2` radians.
 *
 * Special cases:
 *    - `asin(x)` is `NaN`, when `abs(x) > 1` or x is `NaN`
 */
@SinceKotlin("1.2")
public expect fun asin(x: Double): Double

/**
 * Computes the arc cosine of the konstue [x];
 * the returned konstue is an angle in the range from `0.0` to `PI` radians.
 *
 * Special cases:
 *    - `acos(x)` is `NaN`, when `abs(x) > 1` or x is `NaN`
 */
@SinceKotlin("1.2")
public expect fun acos(x: Double): Double

/**
 * Computes the arc tangent of the konstue [x];
 * the returned konstue is an angle in the range from `-PI/2` to `PI/2` radians.
 *
 * Special cases:
 *   - `atan(NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun atan(x: Double): Double

/**
 * Returns the angle `theta` of the polar coordinates `(r, theta)` that correspond
 * to the rectangular coordinates `(x, y)` by computing the arc tangent of the konstue [y] / [x];
 * the returned konstue is an angle in the range from `-PI` to `PI` radians.
 *
 * Special cases:
 *   - `atan2(0.0, 0.0)` is `0.0`
 *   - `atan2(0.0, x)` is  `0.0` for `x > 0` and `PI` for `x < 0`
 *   - `atan2(-0.0, x)` is `-0.0` for 'x > 0` and `-PI` for `x < 0`
 *   - `atan2(y, +Inf)` is `0.0` for `0 < y < +Inf` and `-0.0` for '-Inf < y < 0`
 *   - `atan2(y, -Inf)` is `PI` for `0 < y < +Inf` and `-PI` for `-Inf < y < 0`
 *   - `atan2(y, 0.0)` is `PI/2` for `y > 0` and `-PI/2` for `y < 0`
 *   - `atan2(+Inf, x)` is `PI/2` for finite `x`y
 *   - `atan2(-Inf, x)` is `-PI/2` for finite `x`
 *   - `atan2(NaN, x)` and `atan2(y, NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun atan2(y: Double, x: Double): Double

/**
 * Computes the hyperbolic sine of the konstue [x].
 *
 * Special cases:
 *   - `sinh(NaN)` is `NaN`
 *   - `sinh(+Inf)` is `+Inf`
 *   - `sinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun sinh(x: Double): Double

/**
 * Computes the hyperbolic cosine of the konstue [x].
 *
 * Special cases:
 *   - `cosh(NaN)` is `NaN`
 *   - `cosh(+Inf|-Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
public expect fun cosh(x: Double): Double

/**
 * Computes the hyperbolic tangent of the konstue [x].
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(+Inf)` is `1.0`
 *   - `tanh(-Inf)` is `-1.0`
 */
@SinceKotlin("1.2")
public expect fun tanh(x: Double): Double

/**
 * Computes the inverse hyperbolic sine of the konstue [x].
 *
 * The returned konstue is `y` such that `sinh(y) == x`.
 *
 * Special cases:
 *   - `asinh(NaN)` is `NaN`
 *   - `asinh(+Inf)` is `+Inf`
 *   - `asinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun asinh(x: Double): Double

/**
 * Computes the inverse hyperbolic cosine of the konstue [x].
 *
 * The returned konstue is positive `y` such that `cosh(y) == x`.
 *
 * Special cases:
 *   - `acosh(NaN)` is `NaN`
 *   - `acosh(x)` is `NaN` when `x < 1`
 *   - `acosh(+Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
public expect fun acosh(x: Double): Double

/**
 * Computes the inverse hyperbolic tangent of the konstue [x].
 *
 * The returned konstue is `y` such that `tanh(y) == x`.
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(x)` is `NaN` when `x > 1` or `x < -1`
 *   - `tanh(1.0)` is `+Inf`
 *   - `tanh(-1.0)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun atanh(x: Double): Double

/**
 * Computes `sqrt(x^2 + y^2)` without intermediate overflow or underflow.
 *
 * Special cases:
 *   - returns `+Inf` if any of arguments is infinite
 *   - returns `NaN` if any of arguments is `NaN` and the other is not infinite
 */
@SinceKotlin("1.2")
public expect fun hypot(x: Double, y: Double): Double

/**
 * Computes the positive square root of the konstue [x].
 *
 * Special cases:
 *   - `sqrt(x)` is `NaN` when `x < 0` or `x` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun sqrt(x: Double): Double

/**
 * Computes Euler's number `e` raised to the power of the konstue [x].
 *
 * Special cases:
 *   - `exp(NaN)` is `NaN`
 *   - `exp(+Inf)` is `+Inf`
 *   - `exp(-Inf)` is `0.0`
 */
@SinceKotlin("1.2")
public expect fun exp(x: Double): Double

/**
 * Computes `exp(x) - 1`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 *
 * Special cases:
 *   - `expm1(NaN)` is `NaN`
 *   - `expm1(+Inf)` is `+Inf`
 *   - `expm1(-Inf)` is `-1.0`
 *
 * @see [exp] function.
 */
@SinceKotlin("1.2")
public expect fun expm1(x: Double): Double

/**
 * Computes the logarithm of the konstue [x] to the given [base].
 *
 * Special cases:
 *   - `log(x, b)` is `NaN` if either `x` or `b` are `NaN`
 *   - `log(x, b)` is `NaN` when `x < 0` or `b <= 0` or `b == 1.0`
 *   - `log(+Inf, +Inf)` is `NaN`
 *   - `log(+Inf, b)` is `+Inf` for `b > 1` and `-Inf` for `b < 1`
 *   - `log(0.0, b)` is `-Inf` for `b > 1` and `+Inf` for `b > 1`
 *
 * See also logarithm functions for common fixed bases: [ln], [log10] and [log2].
 */
@SinceKotlin("1.2")
public expect fun log(x: Double, base: Double): Double

/**
 * Computes the natural logarithm (base `E`) of the konstue [x].
 *
 * Special cases:
 *   - `ln(NaN)` is `NaN`
 *   - `ln(x)` is `NaN` when `x < 0.0`
 *   - `ln(+Inf)` is `+Inf`
 *   - `ln(0.0)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun ln(x: Double): Double

/**
 * Computes the common logarithm (base 10) of the konstue [x].
 *
 * @see [ln] function for special cases.
 */
@SinceKotlin("1.2")
public expect fun log10(x: Double): Double

/**
 * Computes the binary logarithm (base 2) of the konstue [x].
 *
 * @see [ln] function for special cases.
 */
@SinceKotlin("1.2")
public expect fun log2(x: Double): Double

/**
 * Computes `ln(x + 1)`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 *
 * Special cases:
 *   - `ln1p(NaN)` is `NaN`
 *   - `ln1p(x)` is `NaN` where `x < -1.0`
 *   - `ln1p(-1.0)` is `-Inf`
 *   - `ln1p(+Inf)` is `+Inf`
 *
 * @see [ln] function
 * @see [expm1] function
 */
@SinceKotlin("1.2")
public expect fun ln1p(x: Double): Double

/**
 * Rounds the given konstue [x] to an integer towards positive infinity.

 * @return the smallest double konstue that is greater than or equal to the given konstue [x] and is a mathematical integer.
 *
 * Special cases:
 *   - `ceil(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun ceil(x: Double): Double

/**
 * Rounds the given konstue [x] to an integer towards negative infinity.

 * @return the largest double konstue that is smaller than or equal to the given konstue [x] and is a mathematical integer.
 *
 * Special cases:
 *   - `floor(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun floor(x: Double): Double

/**
 * Rounds the given konstue [x] to an integer towards zero.
 *
 * @return the konstue [x] having its fractional part truncated.
 *
 * Special cases:
 *   - `truncate(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun truncate(x: Double): Double

/**
 * Rounds the given konstue [x] towards the closest integer with ties rounded towards even integer.
 *
 * Special cases:
 *   - `round(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun round(x: Double): Double

/**
 * Returns the absolute konstue of the given konstue [x].
 *
 * Special cases:
 *   - `abs(NaN)` is `NaN`
 *
 * @see absoluteValue extension property for [Double]
 */
@SinceKotlin("1.2")
public expect fun abs(x: Double): Double

/**
 * Returns the sign of the given konstue [x]:
 *   - `-1.0` if the konstue is negative,
 *   - zero if the konstue is zero,
 *   - `1.0` if the konstue is positive
 *
 * Special case:
 *   - `sign(NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun sign(x: Double): Double


/**
 * Returns the smaller of two konstues.
 *
 * If either konstue is `NaN`, then the result is `NaN`.
 */
@SinceKotlin("1.2")
public expect fun min(a: Double, b: Double): Double

/**
 * Returns the greater of two konstues.
 *
 * If either konstue is `NaN`, then the result is `NaN`.
 */
@SinceKotlin("1.2")
public expect fun max(a: Double, b: Double): Double


/**
 * Returns the cube root of [x]. For any `x`, `cbrt(-x) == -cbrt(x)`;
 * that is, the cube root of a negative konstue is the negative of the cube root
 * of that konstue's magnitude. Special cases:
 *
 * Special cases:
 *   - If the argument is `NaN`, then the result is `NaN`.
 *   - If the argument is infinite, then the result is an infinity with the same sign as the argument.
 *   - If the argument is zero, then the result is a zero with the same sign as the argument.
 */
@SinceKotlin("1.8")
@WasExperimental(ExperimentalStdlibApi::class)
public expect fun cbrt(x: Double): Double


// extensions

/**
 * Raises this konstue to the power [x].
 *
 * Special cases:
 *   - `b.pow(0.0)` is `1.0`
 *   - `b.pow(1.0) == b`
 *   - `b.pow(NaN)` is `NaN`
 *   - `NaN.pow(x)` is `NaN` for `x != 0.0`
 *   - `b.pow(Inf)` is `NaN` for `abs(b) == 1.0`
 *   - `b.pow(x)` is `NaN` for `b < 0` and `x` is finite and not an integer
 */
@SinceKotlin("1.2")
public expect fun Double.pow(x: Double): Double

/**
 * Raises this konstue to the integer power [n].
 *
 * See the other overload of [pow] for details.
 */
@SinceKotlin("1.2")
public expect fun Double.pow(n: Int): Double

/**
 * Returns the absolute konstue of this konstue.
 *
 * Special cases:
 *   - `NaN.absoluteValue` is `NaN`
 *
 * @see abs function
 */
@SinceKotlin("1.2")
public expect konst Double.absoluteValue: Double

/**
 * Returns the sign of this konstue:
 *   - `-1.0` if the konstue is negative,
 *   - zero if the konstue is zero,
 *   - `1.0` if the konstue is positive
 *
 * Special case:
 *   - `NaN.sign` is `NaN`
 */
@SinceKotlin("1.2")
public expect konst Double.sign: Double

/**
 * Returns this konstue with the sign bit same as of the [sign] konstue.
 *
 * If [sign] is `NaN` the sign of the result is undefined.
 */
@SinceKotlin("1.2")
public expect fun Double.withSign(sign: Double): Double

/**
 * Returns this konstue with the sign bit same as of the [sign] konstue.
 */
@SinceKotlin("1.2")
public expect fun Double.withSign(sign: Int): Double

/**
 * Returns the ulp (unit in the last place) of this konstue.
 *
 * An ulp is a positive distance between this konstue and the next nearest [Double] konstue larger in magnitude.
 *
 * Special Cases:
 *   - `NaN.ulp` is `NaN`
 *   - `x.ulp` is `+Inf` when `x` is `+Inf` or `-Inf`
 *   - `0.0.ulp` is `Double.MIN_VALUE`
 */
@SinceKotlin("1.2")
public expect konst Double.ulp: Double

/**
 * Returns the [Double] konstue nearest to this konstue in direction of positive infinity.
 */
@SinceKotlin("1.2")
public expect fun Double.nextUp(): Double

/**
 * Returns the [Double] konstue nearest to this konstue in direction of negative infinity.
 */
@SinceKotlin("1.2")
public expect fun Double.nextDown(): Double

/**
 * Returns the [Double] konstue nearest to this konstue in direction from this konstue towards the konstue [to].
 *
 * Special cases:
 *   - `x.nextTowards(y)` is `NaN` if either `x` or `y` are `NaN`
 *   - `x.nextTowards(x) == x`
 *
 */
@SinceKotlin("1.2")
public expect fun Double.nextTowards(to: Double): Double

/**
 * Rounds this [Double] konstue to the nearest integer and converts the result to [Int].
 * Ties are rounded towards positive infinity.
 *
 * Special cases:
 *   - `x.roundToInt() == Int.MAX_VALUE` when `x > Int.MAX_VALUE`
 *   - `x.roundToInt() == Int.MIN_VALUE` when `x < Int.MIN_VALUE`
 *
 * @throws IllegalArgumentException when this konstue is `NaN`
 */
@SinceKotlin("1.2")
public expect fun Double.roundToInt(): Int

/**
 * Rounds this [Double] konstue to the nearest integer and converts the result to [Long].
 * Ties are rounded towards positive infinity.
 *
 * Special cases:
 *   - `x.roundToLong() == Long.MAX_VALUE` when `x > Long.MAX_VALUE`
 *   - `x.roundToLong() == Long.MIN_VALUE` when `x < Long.MIN_VALUE`
 *
 * @throws IllegalArgumentException when this konstue is `NaN`
 */
@SinceKotlin("1.2")
public expect fun Double.roundToLong(): Long

// endregion



// region ================ Float Math ========================================

/** Computes the sine of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `sin(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun sin(x: Float): Float

/** Computes the cosine of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `cos(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun cos(x: Float): Float

/** Computes the tangent of the angle [x] given in radians.
 *
 *  Special cases:
 *   - `tan(NaN|+Inf|-Inf)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun tan(x: Float): Float

/**
 * Computes the arc sine of the konstue [x];
 * the returned konstue is an angle in the range from `-PI/2` to `PI/2` radians.
 *
 * Special cases:
 *    - `asin(x)` is `NaN`, when `abs(x) > 1` or x is `NaN`
 */
@SinceKotlin("1.2")
public expect fun asin(x: Float): Float

/**
 * Computes the arc cosine of the konstue [x];
 * the returned konstue is an angle in the range from `0.0` to `PI` radians.
 *
 * Special cases:
 *    - `acos(x)` is `NaN`, when `abs(x) > 1` or x is `NaN`
 */
@SinceKotlin("1.2")
public expect fun acos(x: Float): Float

/**
 * Computes the arc tangent of the konstue [x];
 * the returned konstue is an angle in the range from `-PI/2` to `PI/2` radians.
 *
 * Special cases:
 *   - `atan(NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun atan(x: Float): Float

/**
 * Returns the angle `theta` of the polar coordinates `(r, theta)` that correspond
 * to the rectangular coordinates `(x, y)` by computing the arc tangent of the konstue [y] / [x];
 * the returned konstue is an angle in the range from `-PI` to `PI` radians.
 *
 * Special cases:
 *   - `atan2(0.0, 0.0)` is `0.0`
 *   - `atan2(0.0, x)` is  `0.0` for `x > 0` and `PI` for `x < 0`
 *   - `atan2(-0.0, x)` is `-0.0` for 'x > 0` and `-PI` for `x < 0`
 *   - `atan2(y, +Inf)` is `0.0` for `0 < y < +Inf` and `-0.0` for '-Inf < y < 0`
 *   - `atan2(y, -Inf)` is `PI` for `0 < y < +Inf` and `-PI` for `-Inf < y < 0`
 *   - `atan2(y, 0.0)` is `PI/2` for `y > 0` and `-PI/2` for `y < 0`
 *   - `atan2(+Inf, x)` is `PI/2` for finite `x`y
 *   - `atan2(-Inf, x)` is `-PI/2` for finite `x`
 *   - `atan2(NaN, x)` and `atan2(y, NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun atan2(y: Float, x: Float): Float

/**
 * Computes the hyperbolic sine of the konstue [x].
 *
 * Special cases:
 *   - `sinh(NaN)` is `NaN`
 *   - `sinh(+Inf)` is `+Inf`
 *   - `sinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun sinh(x: Float): Float

/**
 * Computes the hyperbolic cosine of the konstue [x].
 *
 * Special cases:
 *   - `cosh(NaN)` is `NaN`
 *   - `cosh(+Inf|-Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
public expect fun cosh(x: Float): Float

/**
 * Computes the hyperbolic tangent of the konstue [x].
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(+Inf)` is `1.0`
 *   - `tanh(-Inf)` is `-1.0`
 */
@SinceKotlin("1.2")
public expect fun tanh(x: Float): Float

/**
 * Computes the inverse hyperbolic sine of the konstue [x].
 *
 * The returned konstue is `y` such that `sinh(y) == x`.
 *
 * Special cases:
 *   - `asinh(NaN)` is `NaN`
 *   - `asinh(+Inf)` is `+Inf`
 *   - `asinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun asinh(x: Float): Float

/**
 * Computes the inverse hyperbolic cosine of the konstue [x].
 *
 * The returned konstue is positive `y` such that `cosh(y) == x`.
 *
 * Special cases:
 *   - `acosh(NaN)` is `NaN`
 *   - `acosh(x)` is `NaN` when `x < 1`
 *   - `acosh(+Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
public expect fun acosh(x: Float): Float

/**
 * Computes the inverse hyperbolic tangent of the konstue [x].
 *
 * The returned konstue is `y` such that `tanh(y) == x`.
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(x)` is `NaN` when `x > 1` or `x < -1`
 *   - `tanh(1.0)` is `+Inf`
 *   - `tanh(-1.0)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun atanh(x: Float): Float

/**
 * Computes `sqrt(x^2 + y^2)` without intermediate overflow or underflow.
 *
 * Special cases:
 *   - returns `+Inf` if any of arguments is infinite
 *   - returns `NaN` if any of arguments is `NaN` and the other is not infinite
 */
@SinceKotlin("1.2")
public expect fun hypot(x: Float, y: Float): Float

/**
 * Computes the positive square root of the konstue [x].
 *
 * Special cases:
 *   - `sqrt(x)` is `NaN` when `x < 0` or `x` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun sqrt(x: Float): Float

/**
 * Computes Euler's number `e` raised to the power of the konstue [x].
 *
 * Special cases:
 *   - `exp(NaN)` is `NaN`
 *   - `exp(+Inf)` is `+Inf`
 *   - `exp(-Inf)` is `0.0`
 */
@SinceKotlin("1.2")
public expect fun exp(x: Float): Float

/**
 * Computes `exp(x) - 1`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 *
 * Special cases:
 *   - `expm1(NaN)` is `NaN`
 *   - `expm1(+Inf)` is `+Inf`
 *   - `expm1(-Inf)` is `-1.0`
 *
 * @see [exp] function.
 */
@SinceKotlin("1.2")
public expect fun expm1(x: Float): Float

/**
 * Computes the logarithm of the konstue [x] to the given [base].
 *
 * Special cases:
 *   - `log(x, b)` is `NaN` if either `x` or `b` are `NaN`
 *   - `log(x, b)` is `NaN` when `x < 0` or `b <= 0` or `b == 1.0`
 *   - `log(+Inf, +Inf)` is `NaN`
 *   - `log(+Inf, b)` is `+Inf` for `b > 1` and `-Inf` for `b < 1`
 *   - `log(0.0, b)` is `-Inf` for `b > 1` and `+Inf` for `b > 1`
 *
 * See also logarithm functions for common fixed bases: [ln], [log10] and [log2].
 */
@SinceKotlin("1.2")
public expect fun log(x: Float, base: Float): Float

/**
 * Computes the natural logarithm (base `E`) of the konstue [x].
 *
 * Special cases:
 *   - `ln(NaN)` is `NaN`
 *   - `ln(x)` is `NaN` when `x < 0.0`
 *   - `ln(+Inf)` is `+Inf`
 *   - `ln(0.0)` is `-Inf`
 */
@SinceKotlin("1.2")
public expect fun ln(x: Float): Float

/**
 * Computes the common logarithm (base 10) of the konstue [x].
 *
 * @see [ln] function for special cases.
 */
@SinceKotlin("1.2")
public expect fun log10(x: Float): Float

/**
 * Computes the binary logarithm (base 2) of the konstue [x].
 *
 * @see [ln] function for special cases.
 */
@SinceKotlin("1.2")
public expect fun log2(x: Float): Float

/**
 * Computes `ln(x + 1)`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 *
 * Special cases:
 *   - `ln1p(NaN)` is `NaN`
 *   - `ln1p(x)` is `NaN` where `x < -1.0`
 *   - `ln1p(-1.0)` is `-Inf`
 *   - `ln1p(+Inf)` is `+Inf`
 *
 * @see [ln] function
 * @see [expm1] function
 */
@SinceKotlin("1.2")
public expect fun ln1p(x: Float): Float

/**
 * Rounds the given konstue [x] to an integer towards positive infinity.

 * @return the smallest Float konstue that is greater than or equal to the given konstue [x] and is a mathematical integer.
 *
 * Special cases:
 *   - `ceil(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun ceil(x: Float): Float

/**
 * Rounds the given konstue [x] to an integer towards negative infinity.

 * @return the largest Float konstue that is smaller than or equal to the given konstue [x] and is a mathematical integer.
 *
 * Special cases:
 *   - `floor(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun floor(x: Float): Float

/**
 * Rounds the given konstue [x] to an integer towards zero.
 *
 * @return the konstue [x] having its fractional part truncated.
 *
 * Special cases:
 *   - `truncate(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun truncate(x: Float): Float

/**
 * Rounds the given konstue [x] towards the closest integer with ties rounded towards even integer.
 *
 * Special cases:
 *   - `round(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
public expect fun round(x: Float): Float


/**
 * Returns the absolute konstue of the given konstue [x].
 *
 * Special cases:
 *   - `abs(NaN)` is `NaN`
 *
 * @see absoluteValue extension property for [Float]
 */
@SinceKotlin("1.2")
public expect fun abs(x: Float): Float

/**
 * Returns the sign of the given konstue [x]:
 *   - `-1.0` if the konstue is negative,
 *   - zero if the konstue is zero,
 *   - `1.0` if the konstue is positive
 *
 * Special case:
 *   - `sign(NaN)` is `NaN`
 */
@SinceKotlin("1.2")
public expect fun sign(x: Float): Float



/**
 * Returns the smaller of two konstues.
 *
 * If either konstue is `NaN`, then the result is `NaN`.
 */
@SinceKotlin("1.2")
public expect fun min(a: Float, b: Float): Float

/**
 * Returns the greater of two konstues.
 *
 * If either konstue is `NaN`, then the result is `NaN`.
 */
@SinceKotlin("1.2")
public expect fun max(a: Float, b: Float): Float


/**
 * Returns the cube root of [x]. For any `x`, `cbrt(-x) == -cbrt(x)`;
 * that is, the cube root of a negative konstue is the negative of the cube root
 * of that konstue's magnitude. Special cases:
 *
 * Special cases:
 *   - If the argument is `NaN`, then the result is `NaN`.
 *   - If the argument is infinite, then the result is an infinity with the same sign as the argument.
 *   - If the argument is zero, then the result is a zero with the same sign as the argument.
 */
@SinceKotlin("1.8")
@WasExperimental(ExperimentalStdlibApi::class)
public expect fun cbrt(x: Float): Float


// extensions


/**
 * Raises this konstue to the power [x].
 *
 * Special cases:
 *   - `b.pow(0.0)` is `1.0`
 *   - `b.pow(1.0) == b`
 *   - `b.pow(NaN)` is `NaN`
 *   - `NaN.pow(x)` is `NaN` for `x != 0.0`
 *   - `b.pow(Inf)` is `NaN` for `abs(b) == 1.0`
 *   - `b.pow(x)` is `NaN` for `b < 0` and `x` is finite and not an integer
 */
@SinceKotlin("1.2")
public expect fun Float.pow(x: Float): Float

/**
 * Raises this konstue to the integer power [n].
 *
 * See the other overload of [pow] for details.
 */
@SinceKotlin("1.2")
public expect fun Float.pow(n: Int): Float

/**
 * Returns the absolute konstue of this konstue.
 *
 * Special cases:
 *   - `NaN.absoluteValue` is `NaN`
 *
 * @see abs function
 */
@SinceKotlin("1.2")
public expect konst Float.absoluteValue: Float

/**
 * Returns the sign of this konstue:
 *   - `-1.0` if the konstue is negative,
 *   - zero if the konstue is zero,
 *   - `1.0` if the konstue is positive
 *
 * Special case:
 *   - `NaN.sign` is `NaN`
 */
@SinceKotlin("1.2")
public expect konst Float.sign: Float

/**
 * Returns this konstue with the sign bit same as of the [sign] konstue.
 *
 * If [sign] is `NaN` the sign of the result is undefined.
 */
@SinceKotlin("1.2")
public expect fun Float.withSign(sign: Float): Float

/**
 * Returns this konstue with the sign bit same as of the [sign] konstue.
 */
@SinceKotlin("1.2")
public expect fun Float.withSign(sign: Int): Float


/**
 * Rounds this [Float] konstue to the nearest integer and converts the result to [Int].
 * Ties are rounded towards positive infinity.
 *
 * Special cases:
 *   - `x.roundToInt() == Int.MAX_VALUE` when `x > Int.MAX_VALUE`
 *   - `x.roundToInt() == Int.MIN_VALUE` when `x < Int.MIN_VALUE`
 *
 * @throws IllegalArgumentException when this konstue is `NaN`
 */
@SinceKotlin("1.2")
public expect fun Float.roundToInt(): Int

/**
 * Rounds this [Float] konstue to the nearest integer and converts the result to [Long].
 * Ties are rounded towards positive infinity.
 *
 * Special cases:
 *   - `x.roundToLong() == Long.MAX_VALUE` when `x > Long.MAX_VALUE`
 *   - `x.roundToLong() == Long.MIN_VALUE` when `x < Long.MIN_VALUE`
 *
 * @throws IllegalArgumentException when this konstue is `NaN`
 */
@SinceKotlin("1.2")
public expect fun Float.roundToLong(): Long


// endregion

// region ================ Integer Math ========================================


/**
 * Returns the absolute konstue of the given konstue [n].
 *
 * Special cases:
 *   - `abs(Int.MIN_VALUE)` is `Int.MIN_VALUE` due to an overflow
 *
 * @see absoluteValue extension property for [Int]
 */
@SinceKotlin("1.2")
public expect fun abs(n: Int): Int

/**
 * Returns the smaller of two konstues.
 */
@SinceKotlin("1.2")
public expect fun min(a: Int, b: Int): Int

/**
 * Returns the greater of two konstues.
 */
@SinceKotlin("1.2")
public expect fun max(a: Int, b: Int): Int

/**
 * Returns the absolute konstue of this konstue.
 *
 * Special cases:
 *   - `Int.MIN_VALUE.absoluteValue` is `Int.MIN_VALUE` due to an overflow
 *
 * @see abs function
 */
@SinceKotlin("1.2")
public expect konst Int.absoluteValue: Int

/**
 * Returns the sign of this konstue:
 *   - `-1` if the konstue is negative,
 *   - `0` if the konstue is zero,
 *   - `1` if the konstue is positive
 */
@SinceKotlin("1.2")
public expect konst Int.sign: Int



/**
 * Returns the absolute konstue of the given konstue [n].
 *
 * Special cases:
 *   - `abs(Long.MIN_VALUE)` is `Long.MIN_VALUE` due to an overflow
 *
 * @see absoluteValue extension property for [Long]
 */
@SinceKotlin("1.2")
public expect fun abs(n: Long): Long

/**
 * Returns the smaller of two konstues.
 */
@SinceKotlin("1.2")
public expect fun min(a: Long, b: Long): Long

/**
 * Returns the greater of two konstues.
 */
@SinceKotlin("1.2")
public expect fun max(a: Long, b: Long): Long

/**
 * Returns the absolute konstue of this konstue.
 *
 * Special cases:
 *   - `Long.MIN_VALUE.absoluteValue` is `Long.MIN_VALUE` due to an overflow
 *
 * @see abs function
 */
@SinceKotlin("1.2")
public expect konst Long.absoluteValue: Long

/**
 * Returns the sign of this konstue:
 *   - `-1` if the konstue is negative,
 *   - `0` if the konstue is zero,
 *   - `1` if the konstue is positive
 */
@SinceKotlin("1.2")
public expect konst Long.sign: Int


// endregion
