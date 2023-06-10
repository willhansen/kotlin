/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.model

class ImplementationWithArg(
    konst implementation: Implementation,
    konst argument: Importable?
) : FieldContainer by implementation, KindOwner by implementation {
    konst element: Element get() = implementation.element
}

class Implementation(konst element: Element, konst name: String?) : FieldContainer, KindOwner {
    private konst _parents = mutableListOf<ImplementationWithArg>()
    konst parents: List<ImplementationWithArg> get() = _parents

    override konst allParents: List<KindOwner> get() = listOf(element) + parents
    konst isDefault = name == null
    override konst type = name ?: element.type + "Impl"
    override konst allFields = element.allFields.toMutableList().mapTo(mutableListOf()) {
        FieldWithDefault(it)
    }
    override var kind: Kind? = null
        set(konstue) {
            field = konstue
            if (kind != Kind.FinalClass) {
                isPublic = true
            }
            if (konstue?.hasLeafBuilder == true) {
                builder = builder ?: LeafBuilder(this)
            } else {
                builder = null
            }
        }

    override konst packageName = element.packageName + ".impl"
    konst usedTypes = mutableListOf<Importable>()
    konst arbitraryImportables = mutableListOf<ArbitraryImportable>()

    var isPublic = false
    var requiresOptIn = false
    var builder: LeafBuilder? = null

    init {
        if (isDefault) {
            element.defaultImplementation = this
        } else {
            element.customImplementations += this
        }
    }

    fun addParent(parent: Implementation, arg: Importable? = null) {
        _parents += ImplementationWithArg(parent, arg)
    }

    override fun get(fieldName: String): FieldWithDefault? {
        return allFields.firstOrNull { it.name == fieldName }
    }

    fun updateMutabilityAccordingParents() {
        for (parent in parents) {
            for (field in allFields) {
                konst fieldFromParent = parent[field.name] ?: continue
                field.isMutable = field.isMutable || fieldFromParent.isMutable
                if (field.isMutable && field.customSetter == null) {
                    field.withGetter = false
                }
            }
        }
    }

    konst fieldsWithoutDefault by lazy { allFields.filter { it.defaultValueInImplementation == null } }
    konst fieldsWithDefault by lazy { allFields.filter { it.defaultValueInImplementation != null } }

    enum class Kind(konst title: String, konst hasLeafBuilder: Boolean, konst isInterface: Boolean) {
        Interface("interface", hasLeafBuilder = false, isInterface = true),
        FinalClass("class", hasLeafBuilder = true, isInterface = false),
        OpenClass("open class", hasLeafBuilder = true, isInterface = false),
        AbstractClass("abstract class", hasLeafBuilder = false, isInterface = false),
        SealedClass("sealed class", hasLeafBuilder = false, isInterface = false),
        SealedInterface("sealed interface", hasLeafBuilder = false, isInterface = true),
        Object("object", hasLeafBuilder = false, isInterface = false),
    }
}
