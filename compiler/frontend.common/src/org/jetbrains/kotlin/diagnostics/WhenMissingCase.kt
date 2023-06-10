/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

sealed class WhenMissingCase {
    abstract konst branchConditionText: String

    object Unknown : WhenMissingCase() {
        override fun toString(): String = "unknown"

        override konst branchConditionText: String = "else"
    }

    sealed class ConditionTypeIsExpect(konst typeOfDeclaration: String) : WhenMissingCase() {
        object SealedClass : ConditionTypeIsExpect("sealed class")
        object SealedInterface : ConditionTypeIsExpect("sealed interface")
        object Enum : ConditionTypeIsExpect("enum")

        override konst branchConditionText: String = "else"

        override fun toString(): String = "unknown"
    }

    object NullIsMissing : WhenMissingCase() {
        override konst branchConditionText: String = "null"
    }

    sealed class BooleanIsMissing(konst konstue: Boolean) : WhenMissingCase() {
        object TrueIsMissing : BooleanIsMissing(true)
        object FalseIsMissing : BooleanIsMissing(false)

        override konst branchConditionText: String = konstue.toString()
    }

    class IsTypeCheckIsMissing(konst classId: ClassId, konst isSingleton: Boolean) : WhenMissingCase() {
        override konst branchConditionText: String = run {
            konst fqName = classId.asSingleFqName().toString()
            if (isSingleton) fqName else "is $fqName"
        }

        override fun toString(): String {
            konst className = classId.shortClassName
            konst name = if (className.isSpecial) className.asString() else className.identifier
            return if (isSingleton) name else "is $name"
        }
    }

    class EnumCheckIsMissing(konst callableId: CallableId) : WhenMissingCase() {
        override konst branchConditionText: String = callableId.asSingleFqName().toString()

        override fun toString(): String {
            return callableId.callableName.identifier
        }
    }

    override fun toString(): String {
        return branchConditionText
    }
}
