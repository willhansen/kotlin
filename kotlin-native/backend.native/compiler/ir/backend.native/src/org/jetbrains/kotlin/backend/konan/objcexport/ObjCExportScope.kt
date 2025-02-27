/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.backend.konan.descriptors.isInterface
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.types.KotlinType

interface ObjCExportScope {
    class RecursionBreachException : Exception {
        constructor(type: KotlinType) : super("$type was already encountered during type mapping process.")
    }

    konst parent: ObjCExportScope?
        get() = null

    fun deriveForType(kotlinType: KotlinType): ObjCTypeExportScope = ObjCTypeExportScopeImpl(kotlinType, this)
    fun deriveForClass(container: DeclarationDescriptor, namer: ObjCExportNamer): ObjCClassExportScope =
            ObjCClassExportScopeImpl(container, namer, this)
}

internal inline fun <reified T : ObjCExportScope> ObjCExportScope.nearestScopeOfType(): T? {
    var parent: ObjCExportScope? = this
    while (parent != null) {
        if (parent is T) {
            return parent
        }
        parent = parent.parent
    }
    return null
}

internal object ObjCRootExportScope : ObjCExportScope

interface ObjCClassExportScope : ObjCExportScope {
    fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): ObjCGenericTypeUsage?
}

private class ObjCClassExportScopeImpl constructor(
        container: DeclarationDescriptor,
        konst namer: ObjCExportNamer,
        override konst parent: ObjCExportScope?,
) : ObjCClassExportScope {
    private konst typeParameterNames: List<TypeParameterDescriptor> =
            if (container is ClassDescriptor && !container.isInterface) {
                container.typeConstructor.parameters
            } else {
                emptyList<TypeParameterDescriptor>()
            }

    override fun getGenericTypeUsage(typeParameterDescriptor: TypeParameterDescriptor?): ObjCGenericTypeUsage? {
        return typeParameterDescriptor?.let { descriptor ->
            typeParameterNames.firstOrNull {
                it == descriptor || it.isCapturedFromOuterDeclaration && it.original == descriptor
            }?.let {
                ObjCGenericTypeParameterUsage(it, namer)
            }
        }
    }
}

interface ObjCTypeExportScope : ObjCExportScope {
    konst kotlinType: KotlinType
}

private class ObjCTypeExportScopeImpl(override konst kotlinType: KotlinType, override konst parent: ObjCExportScope?) : ObjCTypeExportScope {
    init {
        var parent = this.parent
        while (parent != null && parent is ObjCTypeExportScope) {
            if (parent.kotlinType == kotlinType)
                throw ObjCExportScope.RecursionBreachException(kotlinType)
            parent = parent.parent
        }
    }
}