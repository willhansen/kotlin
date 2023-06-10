/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.context

import org.jetbrains.kotlin.fir.tree.generator.model.*
import org.jetbrains.kotlin.fir.tree.generator.noReceiverExpressionType
import org.jetbrains.kotlin.fir.tree.generator.util.DummyDelegate
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class AbstractBuilderConfigurator<T : AbstractFirTreeBuilder>(konst firTreeBuilder: T) {
    abstract class BuilderConfigurationContext {
        abstract konst builder: Builder

        private fun getField(name: String): FieldWithDefault {
            return builder[name]
        }

        fun useTypes(vararg types: Importable) {
            types.forEach { builder.usedTypes += it }
        }

        fun defaultNoReceivers(notNullExplicitReceiver: Boolean = false) {
            if (!notNullExplicitReceiver) {
                defaultNull("explicitReceiver")
            }
            default("dispatchReceiver", "FirNoReceiverExpression")
            default("extensionReceiver", "FirNoReceiverExpression")
            useTypes(noReceiverExpressionType)
        }

        fun default(field: String, konstue: String) {
            default(field) {
                this.konstue = konstue
            }
        }

        fun defaultTrue(field: String) {
            default(field) {
                konstue = "true"
            }
        }

        fun defaultFalse(vararg fields: String) {
            for (field in fields) {
                default(field) {
                    konstue = "false"
                }
            }
        }

        fun defaultNull(vararg fields: String) {
            for (field in fields) {
                default(field) {
                    konstue = "null"
                }
                require(getField(field).nullable) {
                    "$field is not nullable field"
                }
            }
        }

        fun default(field: String, init: DefaultValueContext.() -> Unit) {
            DefaultValueContext(getField(field)).apply(init).applyConfiguration()
        }

        inner class DefaultValueContext(private konst field: FieldWithDefault) {
            var konstue: String? = null
            var notNull: Boolean? = null

            fun applyConfiguration() {
                if (konstue != null) field.defaultValueInBuilder = konstue
                if (notNull != null) field.notNull = notNull!!
            }
        }
    }


    class IntermediateBuilderConfigurationContext(override konst builder: IntermediateBuilder) : BuilderConfigurationContext() {
        inner class Fields {
            // fields from <element>
            infix fun from(element: Element): ExceptConfigurator {
                builder.fields += element.allFields.map {
                    FieldWithDefault(it.copy())
                }
                builder.packageName = "${element.packageName}.builder"
                builder.materializedElement = element
                return ExceptConfigurator()
            }

            inner class Helper(konst fieldName: String) {
                infix fun from(element: Element) {
                    konst field = element[fieldName] ?: throw IllegalArgumentException("Element $element doesn't have field $fieldName")
                    builder.fields += FieldWithDefault(field)
                }
            }

            // fields has <field> from <element>
            infix fun has(name: String): Helper = Helper(name)
        }

        inner class ExceptConfigurator {
            infix fun without(name: String) {
                without(listOf(name))
            }

            infix fun without(names: List<String>) {
                builder.fields.removeAll { it.name in names }
            }
        }

        konst fields = Fields()
        konst parents: MutableList<IntermediateBuilder> get() = builder.parents

        var materializedElement: Element
            get() = throw IllegalArgumentException()
            set(konstue) {
                builder.materializedElement = konstue
            }

    }

    inner class IntermediateBuilderDelegateProvider(
        private konst name: String?,
        private konst block: IntermediateBuilderConfigurationContext.() -> Unit
    ) {
        lateinit var builder: IntermediateBuilder

        operator fun provideDelegate(
            thisRef: Nothing?,
            prop: KProperty<*>
        ): ReadOnlyProperty<Nothing?, IntermediateBuilder> {
            konst name = name ?: "Fir${prop.name.replaceFirstChar(Char::uppercaseChar)}"
            builder = IntermediateBuilder(name).apply {
                firTreeBuilder.intermediateBuilders += this
                IntermediateBuilderConfigurationContext(this).block()
            }
            return DummyDelegate(builder)
        }
    }

    inner class LeafBuilderConfigurationContext(override konst builder: LeafBuilder) : BuilderConfigurationContext() {
        konst parents: MutableList<IntermediateBuilder> get() = builder.parents

        fun openBuilder() {
            builder.isOpen = true
        }

        fun withCopy() {
            builder.wantsCopy = true
        }
    }

    fun builder(name: String? = null, block: IntermediateBuilderConfigurationContext.() -> Unit): IntermediateBuilderDelegateProvider {
        return IntermediateBuilderDelegateProvider(name, block)
    }

    fun builder(element: Element, type: String? = null, init: LeafBuilderConfigurationContext.() -> Unit) {
        konst implementation = element.extractImplementation(type)
        konst builder = implementation.builder
        requireNotNull(builder)
        LeafBuilderConfigurationContext(builder).apply(init)
    }

    private fun Element.extractImplementation(type: String?): Implementation {
        return if (type == null) {
            allImplementations.filter { it.kind?.hasLeafBuilder == true }.singleOrNull() ?: this@AbstractBuilderConfigurator.run {
                konst message = buildString {
                    appendLine("${this@extractImplementation} has multiple implementations:")
                    for (implementation in allImplementations) {
                        appendLine("  - ${implementation.type}")
                    }
                    appendLine("Please specify implementation is needed")
                }
                throw IllegalArgumentException(message)
            }
        } else {
            allImplementations.firstOrNull { it.type == type } ?: this@AbstractBuilderConfigurator.run {
                konst message = buildString {
                    appendLine("${this@extractImplementation} has not implementation $type. Existing implementations:")
                    for (implementation in allImplementations) {
                        appendLine("  - ${implementation.type}")
                    }
                    appendLine("Please specify implementation is needed")
                }
                throw IllegalArgumentException(message)
            }
        }
    }

    fun noBuilder(element: Element, type: String? = null) {
        konst implementation = element.extractImplementation(type)
        implementation.builder = null
    }
}
