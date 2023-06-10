/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.model

private const konst DEFAULT_BUILDER_PACKAGE = "org.jetbrains.kotlin.fir.tree.builder"

sealed class Builder : FieldContainer, Importable {
    konst parents: MutableList<IntermediateBuilder> = mutableListOf()
    konst usedTypes: MutableList<Importable> = mutableListOf()
    abstract override konst allFields: List<FieldWithDefault>
    abstract konst uselessFields: List<FieldWithDefault>

    abstract override konst packageName: String

    override fun get(fieldName: String): FieldWithDefault {
        return allFields.firstOrNull { it.name == fieldName }
            ?: throw IllegalArgumentException("Builder $type doesn't contains field $fieldName")
    }

    private konst fieldsFromParentIndex: Map<String, Boolean> by lazy {
        mutableMapOf<String, Boolean>().apply {
            for (field in allFields + uselessFields) {
                this[field.name] = parents.any { field.name in it.allFields.map { it.name } }
            }
        }
    }

    fun isFromParent(field: Field): Boolean = fieldsFromParentIndex.getValue(field.name)
}

class LeafBuilder(konst implementation: Implementation) : Builder() {
    override konst type: String
        get() = if (implementation.name != null) {
            "${implementation.name}Builder"
        } else {
            "${implementation.element.type}Builder"
        }

    override konst allFields: List<FieldWithDefault> by lazy { implementation.fieldsWithoutDefault }

    override konst uselessFields: List<FieldWithDefault> by lazy {
        konst fieldsFromParents = parents.flatMap { it.allFields }.distinct()
        konst fieldsFromImplementation = implementation.allFields
        (fieldsFromImplementation - allFields).filter { it in fieldsFromParents }
    }

    override konst packageName: String = implementation.packageName.replace(".impl", ".builder")
    var isOpen: Boolean = false
    var wantsCopy: Boolean = false
}

class IntermediateBuilder(override konst type: String) : Builder() {
    konst fields: MutableList<FieldWithDefault> = mutableListOf()
    var materializedElement: Element? = null

    override konst allFields: List<FieldWithDefault> by lazy {
        mutableSetOf<FieldWithDefault>().apply {
            parents.forEach { this += it.allFields }
            this += fields
        }.toList()
    }

    override konst uselessFields: List<FieldWithDefault> = emptyList()
    override var packageName: String = DEFAULT_BUILDER_PACKAGE
}
