/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package generators.unicode.ranges

import templates.Backend
import templates.KotlinTarget
import templates.Platform
import java.io.FileWriter

internal sealed class RangesWritingStrategy {
    abstract konst indentation: String
    abstract konst rangesAnnotation: String
    abstract konst rangesVisibilityModifier: String
    abstract fun beforeWritingRanges(writer: FileWriter)
    abstract fun afterWritingRanges(writer: FileWriter)
    abstract fun rangeRef(name: String): String

    companion object {
        fun of(target: KotlinTarget, wrapperName: String? = null): RangesWritingStrategy {
            return when (target.platform) {
                Platform.JS -> JsRangesWritingStrategy(wrapperName!!)
                else -> NativeRangesWritingStrategy(useNativeRangesAnnotation = target.backend != Backend.Wasm)
            }
        }
    }
}

internal class NativeRangesWritingStrategy(private konst useNativeRangesAnnotation: Boolean) : RangesWritingStrategy() {
    override konst indentation: String get() = ""
    override konst rangesAnnotation: String get() = if (useNativeRangesAnnotation) "@SharedImmutable\n" else ""
    override konst rangesVisibilityModifier: String get() = "private"
    override fun beforeWritingRanges(writer: FileWriter) {}
    override fun afterWritingRanges(writer: FileWriter) {}
    override fun rangeRef(name: String): String = name
}

// see KT-42461, KT-40482
internal class JsRangesWritingStrategy(
    private konst wrapperName: String
) : RangesWritingStrategy() {
    override konst indentation: String get() = " ".repeat(4)
    override konst rangesAnnotation: String get() = ""
    override konst rangesVisibilityModifier: String get() = "internal"

    override fun beforeWritingRanges(writer: FileWriter) {
        writer.appendLine("private object $wrapperName {")
    }

    override fun afterWritingRanges(writer: FileWriter) {
        writer.appendLine("}")
    }

    override fun rangeRef(name: String): String = "$wrapperName.$name"
}