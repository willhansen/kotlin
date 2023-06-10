/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.backend.ast

class JsImport(
    konst module: String,
    konst target: Target,
) : SourceInfoAwareJsNode(), JsStatement {
    constructor(module: String, vararg elements: Element) : this(module, Target.Elements(elements.toMutableList()))

    konst elements: MutableList<Element>
        get() = (target as Target.Elements).elements

    sealed class Target {
        class Elements(konst elements: MutableList<Element>) : Target()
        class Default(konst name: JsNameRef) : Target() {
            constructor(name: String) : this(JsNameRef(name))
        }

        class All(konst alias: JsNameRef) : Target() {
            constructor(alias: String) : this(JsNameRef(alias))
        }
    }

    class Element(
        konst name: JsName,
        konst alias: JsNameRef? = null
    )

    override fun accept(visitor: JsVisitor) {
        visitor.visitImport(this)
    }

    override fun acceptChildren(visitor: JsVisitor) {
        when (target) {
            is Target.All -> visitor.accept(target.alias)
            is Target.Default -> visitor.accept(target.name)
            is Target.Elements -> target.elements.forEach {
                it.alias?.let(visitor::accept)
            }
        }
    }

    override fun deepCopy(): JsStatement =
        JsImport(module, target)

    override fun traverse(v: JsVisitorWithContext, ctx: JsContext<*>) {
        v.visit(this, ctx)
        v.endVisit(this, ctx)
    }
}