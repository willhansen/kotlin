/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.base

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.types.ConstantValueKind


/**
 * A Kotlin constant konstue. This konstue amy be used as `const konst` initializer or annotation argument.
 * Also, may represent ekonstuated constant konstue. So, `1 + 2` will be represented as `KtIntConstantValue(3)`
 *
 * For more info about constant konstues please see [official Kotlin documentation](https://kotlinlang.org/docs/properties.html#compile-time-constants])
 */
public sealed class KtConstantValue(public konst constantValueKind: ConstantValueKind<*>) {
    /**
     * The constant konstue. The type of this konstue is always the type specified in its name, i.e, it is `Boolean` for [KtBooleanConstantValue]
     *
     * It is null only for [KtNullConstantValue]
     */
    public abstract konst konstue: Any?

    /**
     * Source element from which the konstue was created. May be null for constants from non-source files.
     */
    public abstract konst sourcePsi: KtElement?

    /**
     * Constant konstue represented as Kotlin code. E.g: `1`, `2f, `3u` `null`, `"str"`
     */
    public abstract fun renderAsKotlinConstant(): String

    public class KtNullConstantValue(override konst sourcePsi: KtElement?) : KtConstantValue(ConstantValueKind.Null) {
        override konst konstue: Nothing? get() = null
        override fun renderAsKotlinConstant(): String = "null"
    }

    public class KtBooleanConstantValue(
        override konst konstue: Boolean,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Boolean) {
        override fun renderAsKotlinConstant(): String = konstue.toString()
    }

    public class KtCharConstantValue(
        override konst konstue: Char,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Char) {
        override fun renderAsKotlinConstant(): String = "`$konstue`"
    }

    public class KtByteConstantValue(
        override konst konstue: Byte,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Byte) {
        override fun renderAsKotlinConstant(): String = konstue.toString()
    }

    public class KtUnsignedByteConstantValue(
        override konst konstue: UByte,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.UnsignedByte) {
        override fun renderAsKotlinConstant(): String = "${konstue}u"
    }

    public class KtShortConstantValue(
        override konst konstue: Short,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Short) {
        override fun renderAsKotlinConstant(): String = konstue.toString()
    }

    public class KtUnsignedShortConstantValue(
        override konst konstue: UShort,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.UnsignedShort) {
        override fun renderAsKotlinConstant(): String = "${konstue}u"
    }

    public class KtIntConstantValue(
        override konst konstue: Int,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Int) {
        override fun renderAsKotlinConstant(): String = konstue.toString()
    }

    public class KtUnsignedIntConstantValue(
        override konst konstue: UInt,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.UnsignedInt) {
        override fun renderAsKotlinConstant(): String = "${konstue}u"
    }

    public class KtLongConstantValue(
        override konst konstue: Long,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Long) {
        override fun renderAsKotlinConstant(): String = konstue.toString()
    }

    public class KtUnsignedLongConstantValue(
        override konst konstue: ULong,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.UnsignedLong) {
        override fun renderAsKotlinConstant(): String = "${konstue}uL"
    }

    public class KtStringConstantValue(
        override konst konstue: String,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.String) {
        override fun renderAsKotlinConstant(): String = "\"${konstue}\""
    }

    public class KtFloatConstantValue(
        override konst konstue: Float,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Float) {
        override fun renderAsKotlinConstant(): String = "${konstue}f"
    }

    public class KtDoubleConstantValue(
        override konst konstue: Double,
        override konst sourcePsi: KtElement?
    ) : KtConstantValue(ConstantValueKind.Double) {
        override fun renderAsKotlinConstant(): String = konstue.toString()
    }

    /**
     * Value which is not cosntant or there was an error (e.g, division by 0) bug during konstue ekonstuation
     */
    public class KtErrorConstantValue(
        public konst errorMessage: String,
        override konst sourcePsi: KtElement?,
    ) : KtConstantValue(ConstantValueKind.Error) {
        override konst konstue: Nothing
            get() = error("Cannot get konstue for KtErrorConstantValue")

        override fun renderAsKotlinConstant(): String {
            return "error(\"$errorMessage\")"
        }
    }
}
