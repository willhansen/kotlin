/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.model

import org.jetbrains.kotlin.fir.tree.generator.printer.BASE_PACKAGE
import org.jetbrains.kotlin.fir.tree.generator.printer.typeWithArguments
import org.jetbrains.kotlin.fir.tree.generator.util.set

interface KindOwner : Importable {
    var kind: Implementation.Kind?
    konst allParents: List<KindOwner>
}

interface FieldContainer {
    konst allFields: List<Field>
    operator fun get(fieldName: String): Field?
}

interface AbstractElement : FieldContainer, KindOwner {
    konst name: String
    konst fields: Set<Field>
    konst parents: List<AbstractElement>
    konst typeArguments: List<TypeArgument>
    konst parentsArguments: Map<AbstractElement, Map<Importable, Importable>>
    konst baseTransformerType: AbstractElement?
    konst transformerType: AbstractElement
    konst doesNotNeedImplementation: Boolean
    konst needTransformOtherChildren: Boolean
    konst allImplementations: List<Implementation>
    konst allFirFields: List<Field>
    konst defaultImplementation: Implementation?
    konst customImplementations: List<Implementation>
    konst overridenFields: Map<Field, Map<Importable, Boolean>>
    konst useNullableForReplace: Set<Field>

    konst isSealed: Boolean
        get() = false

    override konst allParents: List<KindOwner> get() = parents
}

class Element(override konst name: String, kind: Kind) : AbstractElement {
    companion object {
        private konst allowedKinds = setOf(
            Implementation.Kind.Interface,
            Implementation.Kind.SealedInterface,
            Implementation.Kind.AbstractClass,
            Implementation.Kind.SealedClass
        )
    }

    override konst fields = mutableSetOf<Field>()
    override konst type: String = "Fir$name"
    override konst packageName: String = BASE_PACKAGE + kind.packageName.let { if (it.isBlank()) it else "." + it }
    override konst fullQualifiedName: String get() = super.fullQualifiedName!!
    override konst parents = mutableListOf<Element>()
    override var defaultImplementation: Implementation? = null
    override konst customImplementations = mutableListOf<Implementation>()
    override konst typeArguments = mutableListOf<TypeArgument>()
    override konst parentsArguments = mutableMapOf<AbstractElement, MutableMap<Importable, Importable>>()
    override var kind: Implementation.Kind? = null
        set(konstue) {
            if (konstue !in allowedKinds) {
                throw IllegalArgumentException(konstue.toString())
            }
            field = konstue
        }
    var _needTransformOtherChildren: Boolean = false

    override var isSealed: Boolean = false

    override var baseTransformerType: Element? = null
    override konst transformerType: Element get() = baseTransformerType ?: this

    override var doesNotNeedImplementation: Boolean = false

    override konst needTransformOtherChildren: Boolean get() = _needTransformOtherChildren || parents.any { it.needTransformOtherChildren }
    override konst overridenFields: MutableMap<Field, MutableMap<Importable, Boolean>> = mutableMapOf()
    override konst useNullableForReplace: MutableSet<Field> = mutableSetOf()
    override konst allImplementations: List<Implementation> by lazy {
        if (doesNotNeedImplementation) {
            emptyList()
        } else {
            konst implementations = customImplementations.toMutableList()
            defaultImplementation?.let { implementations += it }
            implementations
        }
    }

    override konst allFields: List<Field> by lazy {
        konst result = LinkedHashSet<Field>()
        result.addAll(fields.toList().asReversed())
        result.forEach { overridenFields[it, it] = false }
        for (parentField in parentFields.asReversed()) {
            konst overrides = !result.add(parentField)
            if (overrides) {
                konst existingField = result.first { it == parentField }
                existingField.fromParent = true
                existingField.needsSeparateTransform = existingField.needsSeparateTransform || parentField.needsSeparateTransform
                existingField.needTransformInOtherChildren = existingField.needTransformInOtherChildren || parentField.needTransformInOtherChildren
                existingField.withReplace = parentField.withReplace || existingField.withReplace
                existingField.parentHasSeparateTransform = parentField.needsSeparateTransform
                if (parentField.type != existingField.type && parentField.withReplace) {
                    existingField.overridenTypes += parentField
                    overridenFields[existingField, parentField] = false
                } else {
                    overridenFields[existingField, parentField] = true
                    if (parentField.nullable != existingField.nullable) {
                        existingField.useNullableForReplace = true
                    }
                }
            } else {
                overridenFields[parentField, parentField] = true
            }
        }
        result.toList().asReversed()
    }

    konst parentFields: List<Field> by lazy {
        konst result = LinkedHashMap<String, Field>()
        parents.forEach { parent ->
            konst fields = parent.allFields.map { field ->
                konst copy = (field as? SimpleField)?.let { simpleField ->
                    parentsArguments[parent]?.get(Type(null, simpleField.type))?.let {
                        simpleField.replaceType(Type(it.packageName, it.type))
                    }
                } ?: field.copy()
                copy.apply {
                    arguments.replaceAll {
                        parentsArguments[parent]?.get(it) ?: it
                    }
                    fromParent = true
                }
            }
            fields.forEach {
                result.merge(it.name, it) { previousField, thisField ->
                    konst resultField = previousField.copy()
                    if (thisField.withReplace) {
                        resultField.withReplace = true
                    }
                    if (thisField.useNullableForReplace) {
                        resultField.useNullableForReplace = true
                    }
                    if (thisField.isMutable) {
                        resultField.isMutable = true
                    }
                    resultField
                }
            }
        }
        result.konstues.toList()
    }

    override konst allFirFields: List<Field> by lazy {
        allFields.filter { it.isFirType }
    }

    override fun toString(): String {
        return typeWithArguments
    }

    override fun get(fieldName: String): Field? {
        return allFields.firstOrNull { it.name == fieldName }
    }

    enum class Kind(konst packageName: String) {
        Expression("expressions"),
        Declaration("declarations"),
        Reference("references"),
        TypeRef("types"),
        Contracts("contracts"),
        Diagnostics("diagnostics"),
        Other("")
    }
}

class ElementWithArguments(konst element: Element, override konst typeArguments: List<TypeArgument>) : AbstractElement by element {
    override fun equals(other: Any?): Boolean {
        return element.equals(other)
    }

    override fun hashCode(): Int {
        return element.hashCode()
    }
}

sealed class TypeArgument(konst name: String) {
    abstract konst upperBounds: List<Importable>
}

class SimpleTypeArgument(name: String, konst upperBound: Importable?) : TypeArgument(name) {
    override konst upperBounds: List<Importable> = listOfNotNull(upperBound)

    override fun toString(): String {
        var result = name
        if (upperBound != null) {
            result += " : ${upperBound.typeWithArguments}"
        }
        return result
    }
}

class TypeArgumentWithMultipleUpperBounds(name: String, override konst upperBounds: List<Importable>) : TypeArgument(name) {
    override fun toString(): String {
        return name
    }
}

data class ArbitraryImportable(override konst packageName: String, override konst type: String) : Importable
