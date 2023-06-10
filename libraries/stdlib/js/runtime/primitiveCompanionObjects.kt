/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js.internal

@JsName("DoubleCompanionObject")
internal object DoubleCompanionObject {
    @JsName("MIN_VALUE")
    const konst MIN_VALUE: Double = 4.9E-324

    @JsName("MAX_VALUE")
    const konst MAX_VALUE: Double = 1.7976931348623157E308

    @JsName("POSITIVE_INFINITY")
    @Suppress("DIVISION_BY_ZERO")
    const konst POSITIVE_INFINITY: Double = 1.0 / 0.0

    @JsName("NEGATIVE_INFINITY")
    @Suppress("DIVISION_BY_ZERO")
    const konst NEGATIVE_INFINITY: Double = -1.0 / 0.0

    @JsName("NaN")
    @Suppress("DIVISION_BY_ZERO")
    const konst NaN: Double = -(0.0 / 0.0)

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 8

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 64
}

@JsName("FloatCompanionObject")
internal  object FloatCompanionObject {
    @JsName("MIN_VALUE")
    const konst MIN_VALUE: Float = 1.4E-45F

    @JsName("MAX_VALUE")
    const konst MAX_VALUE: Float = 3.4028235E38F

    @JsName("POSITIVE_INFINITY")
    @Suppress("DIVISION_BY_ZERO")
    const konst POSITIVE_INFINITY: Float = 1.0F / 0.0F

    @JsName("NEGATIVE_INFINITY")
    @Suppress("DIVISION_BY_ZERO")
    const konst NEGATIVE_INFINITY: Float = -1.0F / 0.0F

    @JsName("NaN")
    @Suppress("DIVISION_BY_ZERO")
    const konst NaN: Float = -(0.0F / 0.0F)

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 4

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 32
}

@JsName("IntCompanionObject")
internal  object IntCompanionObject {
    @JsName("MIN_VALUE")
    konst MIN_VALUE: Int = -2147483647 - 1

    @JsName("MAX_VALUE")
    konst MAX_VALUE: Int = 2147483647

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 4

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 32
}

@JsName("LongCompanionObject")
internal  object LongCompanionObject {
    @JsName("MIN_VALUE")
    konst MIN_VALUE: Long = js("Kotlin.Long.MIN_VALUE")

    @JsName("MAX_VALUE")
    konst MAX_VALUE: Long = js("Kotlin.Long.MAX_VALUE")

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 8

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 64
}

@JsName("ShortCompanionObject")
internal  object ShortCompanionObject {
    @JsName("MIN_VALUE")
    konst MIN_VALUE: Short = -32768

    @JsName("MAX_VALUE")
    konst MAX_VALUE: Short = 32767

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 2

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 16
}

@JsName("ByteCompanionObject")
internal  object ByteCompanionObject {
    @JsName("MIN_VALUE")
    konst MIN_VALUE: Byte = -128

    @JsName("MAX_VALUE")
    konst MAX_VALUE: Byte = 127

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 1

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 8
}

@JsName("CharCompanionObject")
internal  object CharCompanionObject {
    @JsName("MIN_VALUE")
    public const konst MIN_VALUE: Char = '\u0000'

    @JsName("MAX_VALUE")
    public const konst MAX_VALUE: Char = '\uFFFF'

    @JsName("MIN_HIGH_SURROGATE")
    public const konst MIN_HIGH_SURROGATE: Char = '\uD800'

    @JsName("MAX_HIGH_SURROGATE")
    public const konst MAX_HIGH_SURROGATE: Char = '\uDBFF'

    @JsName("MIN_LOW_SURROGATE")
    public const konst MIN_LOW_SURROGATE: Char = '\uDC00'

    @JsName("MAX_LOW_SURROGATE")
    public const konst MAX_LOW_SURROGATE: Char = '\uDFFF'

    @JsName("MIN_SURROGATE")
    public const konst MIN_SURROGATE: Char = MIN_HIGH_SURROGATE

    @JsName("MAX_SURROGATE")
    public const konst MAX_SURROGATE: Char = MAX_LOW_SURROGATE

    @JsName("SIZE_BYTES")
    const konst SIZE_BYTES = 2

    @JsName("SIZE_BITS")
    const konst SIZE_BITS = 16
}

internal  object StringCompanionObject {}

internal  object BooleanCompanionObject {}

