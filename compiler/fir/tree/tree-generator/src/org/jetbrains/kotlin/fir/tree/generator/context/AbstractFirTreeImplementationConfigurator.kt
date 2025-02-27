/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.context

import org.jetbrains.kotlin.fir.tree.generator.model.*
import org.jetbrains.kotlin.fir.tree.generator.noReceiverExpressionType
import org.jetbrains.kotlin.fir.tree.generator.printer.call

abstract class AbstractFirTreeImplementationConfigurator {
    private konst elementsWithImpl = mutableSetOf<Element>()

    fun noImpl(element: Element) {
        element.doesNotNeedImplementation = true
    }

    fun impl(element: Element, name: String? = null, config: ImplementationContext.() -> Unit = {}): Implementation {
        konst implementation = if (name == null) {
            element.defaultImplementation
        } else {
            element.customImplementations.firstOrNull { it.name == name }
        } ?: Implementation(element, name)
        konst context = ImplementationContext(implementation)
        context.apply(config)
        implementation.updateMutabilityAccordingParents()
        elementsWithImpl += element
        return implementation
    }

    protected fun generateDefaultImplementations(builder: AbstractFirTreeBuilder) {
        collectLeafsWithoutImplementation(builder).forEach {
            impl(it)
        }
    }

    protected fun configureFieldInAllImplementations(
        field: String,
        implementationPredicate: ((Implementation) -> Boolean)? = null,
        fieldPredicate: ((Field) -> Boolean)? = null,
        init: ImplementationContext.(field: String) -> Unit
    ) {
        for (element in elementsWithImpl) {
            for (implementation in element.allImplementations) {
                if (implementationPredicate != null && !implementationPredicate(implementation)) continue
                if (!implementation.allFields.any { it.name == field }) continue
                if (fieldPredicate != null && !fieldPredicate(implementation.getField(field))) continue
                ImplementationContext(implementation).init(field)
            }
        }
    }

    private fun collectLeafsWithoutImplementation(builder: AbstractFirTreeBuilder): Set<Element> {
        konst elements = builder.elements.toMutableSet()
        builder.elements.forEach {
            elements.removeAll(it.parents)
        }
        elements.removeAll(elementsWithImpl)
        return elements
    }

    private fun Implementation.getField(name: String): FieldWithDefault {
        konst result = allFields.firstOrNull { it.name == name }
        requireNotNull(result) {
            "Field \"$name\" not found in fields of ${element}\nExisting fields:\n" +
                    allFields.joinToString(separator = "\n  ", prefix = "  ") { it.name }
        }
        return result
    }

    inner class ImplementationContext(private konst implementation: Implementation) {
        private fun getField(name: String): FieldWithDefault {
            return implementation.getField(name)
        }

        inner class ParentsHolder {
            operator fun plusAssign(parent: Implementation) {
                implementation.addParent(parent)
            }

            operator fun plusAssign(parent: ImplementationWithArg) {
                implementation.addParent(parent.implementation, parent.argument)
                parent.argument?.let { useTypes(it) }
            }
        }

        konst parents = ParentsHolder()

        fun Implementation.withArg(argument: Importable): ImplementationWithArg = ImplementationWithArg(this, argument)

        fun optInToInternals() {
            implementation.requiresOptIn = true
        }

        fun publicImplementation() {
            implementation.isPublic = true
        }

        fun useTypes(vararg types: Importable) {
            types.forEach { implementation.usedTypes += it }
        }

        fun isMutable(vararg fields: String) {
            fields.forEach {
                konst field = getField(it)
                field.isMutable = true
            }
        }

        fun defaultNoReceivers() {
            defaultNull("explicitReceiver")
            default("dispatchReceiver", "FirNoReceiverExpression")
            default("extensionReceiver", "FirNoReceiverExpression")
            useTypes(noReceiverExpressionType)
        }

        fun default(field: String, konstue: String) {
            default(field) {
                this.konstue = konstue
            }
        }

        fun defaultTypeRefWithSource(typeRefClass: String) {
            default("typeRef", "$typeRefClass(source?.fakeElement(KtFakeSourceElementKind.ImplicitTypeRef))")
            implementation.arbitraryImportables += ArbitraryImportable("org.jetbrains.kotlin", "KtFakeSourceElementKind")
            implementation.arbitraryImportables += ArbitraryImportable("org.jetbrains.kotlin", "fakeElement")
        }

        fun defaultTrue(field: String, withGetter: Boolean = false) {
            default(field) {
                konstue = "true"
                this.withGetter = withGetter
            }
        }

        fun defaultFalse(vararg fields: String, withGetter: Boolean = false) {
            for (field in fields) {
                default(field) {
                    konstue = "false"
                    this.withGetter = withGetter
                }
            }
        }

        fun defaultNull(vararg fields: String, withGetter: Boolean = false) {
            for (field in fields) {
                default(field) {
                    konstue = "null"
                    this.withGetter = withGetter
                }
                require(getField(field).nullable) {
                    "$field is not nullable field"
                }
            }
        }

        fun noSource() {
            defaultNull("source", withGetter = true)
        }

        fun defaultEmptyList(field: String) {
            require(getField(field).origin is FieldList) {
                "$field is list field"
            }
            default(field) {
                konstue = "emptyList()"
                withGetter = true
            }
        }

        fun default(field: String, init: DefaultValueContext.() -> Unit) {
            DefaultValueContext(getField(field)).apply(init).applyConfiguration()
        }

        fun delegateFields(fields: List<String>, delegate: String) {
            for (field in fields) {
                default(field) {
                    this.delegate = delegate
                }
            }
        }

        var kind: Implementation.Kind?
            get() = implementation.kind
            set(konstue) {
                implementation.kind = konstue
            }

        inner class DefaultValueContext(private konst field: FieldWithDefault) {
            var konstue: String? = null

            var delegate: String? = null
                set(konstue) {
                    field = konstue
                    if (konstue != null) {
                        withGetter = true
                    }
                }
            var delegateCall: String? = null

            var isMutable: Boolean? = null
            var withGetter: Boolean = false
                set(konstue) {
                    field = konstue
                    if (konstue) {
                        isMutable = customSetter != null
                    }
                }

            var customSetter: String? = null
                set(konstue) {
                    field = konstue
                    isMutable = true
                    withGetter = true
                }

            var needAcceptAndTransform: Boolean = true

            var notNull: Boolean = false

            fun applyConfiguration() {
                field.withGetter = withGetter
                field.customSetter = customSetter
                isMutable?.let { field.isMutable = it }
                field.needAcceptAndTransform = needAcceptAndTransform

                if (notNull) {
                    field.notNull = true
                }
                when {
                    konstue != null -> field.defaultValueInImplementation = konstue
                    delegate != null -> {
                        konst actualDelegateField = getField(delegate!!)
                        konst name = delegateCall ?: field.name
                        field.defaultValueInImplementation = "${actualDelegateField.name}${actualDelegateField.call()}$name"
                    }
                }
            }
        }
    }
}
