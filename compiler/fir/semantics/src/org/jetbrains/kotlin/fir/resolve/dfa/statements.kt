/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import org.jetbrains.kotlin.fir.types.ConeKotlinType

sealed class Statement {
    abstract konst variable: DataFlowVariable
}

/*
 * Examples:
 * d == Null
 * d != Null
 * d == True
 * d == False
 */
data class OperationStatement(override konst variable: DataFlowVariable, konst operation: Operation) : Statement() {
    override fun toString(): String {
        return "$variable $operation"
    }
}

sealed class TypeStatement : Statement() {
    abstract override konst variable: RealVariable
    abstract konst exactType: Set<ConeKotlinType>

    konst isEmpty: Boolean
        get() = exactType.isEmpty()

    konst isNotEmpty: Boolean
        get() = !isEmpty

    override fun toString(): String {
        return "$variable: $exactType"
    }
}

class Implication(
    konst condition: OperationStatement,
    konst effect: Statement
) {
    override fun toString(): String {
        return "$condition -> $effect"
    }
}

enum class Operation {
    EqTrue, EqFalse, EqNull, NotEqNull;

    fun konstueIfKnown(given: Operation): Boolean? = when (this) {
        EqTrue, EqFalse -> if (given == NotEqNull) null else given == this
        EqNull -> given == EqNull
        NotEqNull -> given != EqNull
    }

    override fun toString(): String = when (this) {
        EqTrue -> "== True"
        EqFalse -> "== False"
        EqNull -> "== Null"
        NotEqNull -> "!= Null"
    }
}
