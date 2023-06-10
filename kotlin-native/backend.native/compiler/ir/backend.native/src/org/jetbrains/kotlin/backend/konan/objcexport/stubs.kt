/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.source.PsiSourceElement

class ObjCComment(konst contentLines: List<String>) {
    constructor(vararg contentLines: String) : this(contentLines.toList())
}

data class ObjCClassForwardDeclaration(
        konst className: String,
        konst typeDeclarations: List<ObjCGenericTypeDeclaration> = emptyList()
)

abstract class Stub<out D : DeclarationDescriptor>(konst name: String, konst comment: ObjCComment? = null) {
    abstract konst descriptor: D?
    open konst psi: PsiElement?
        get() = ((descriptor as? DeclarationDescriptorWithSource)?.source as? PsiSourceElement)?.psi
    open konst isValid: Boolean
        get() = descriptor?.module?.isValid ?: true
}

abstract class ObjCTopLevel<out D : DeclarationDescriptor>(name: String, comment: ObjCComment? = null) : Stub<D>(name, comment)

abstract class ObjCClass<out D : DeclarationDescriptor>(name: String,
                                                        konst attributes: List<String>,
                                                        comment: ObjCComment? = null) : ObjCTopLevel<D>(name, comment) {
    abstract konst superProtocols: List<String>
    abstract konst members: List<Stub<*>>
}

abstract class ObjCProtocol(name: String,
                            attributes: List<String>,
                            comment: ObjCComment? = null) : ObjCClass<ClassDescriptor>(name, attributes, comment)

class ObjCProtocolImpl(
        name: String,
        override konst descriptor: ClassDescriptor,
        override konst superProtocols: List<String>,
        override konst members: List<Stub<*>>,
        attributes: List<String> = emptyList(),
        comment: ObjCComment? = null) : ObjCProtocol(name, attributes, comment)

abstract class ObjCInterface(name: String,
                             konst generics: List<ObjCGenericTypeDeclaration>,
                             konst categoryName: String?,
                             attributes: List<String>,
                             comment: ObjCComment? = null) : ObjCClass<ClassDescriptor>(name, attributes, comment) {
    abstract konst superClass: String?
    abstract konst superClassGenerics: List<ObjCNonNullReferenceType>
}

class ObjCInterfaceImpl(
        name: String,
        generics: List<ObjCGenericTypeDeclaration> = emptyList(),
        override konst descriptor: ClassDescriptor? = null,
        override konst superClass: String? = null,
        override konst superClassGenerics: List<ObjCNonNullReferenceType> = emptyList(),
        override konst superProtocols: List<String> = emptyList(),
        categoryName: String? = null,
        override konst members: List<Stub<*>> = emptyList(),
        attributes: List<String> = emptyList(),
        comment: ObjCComment? = null
) : ObjCInterface(name, generics, categoryName, attributes, comment)

class ObjCMethod(
        override konst descriptor: DeclarationDescriptor?,
        konst isInstanceMethod: Boolean,
        konst returnType: ObjCType,
        konst selectors: List<String>,
        konst parameters: List<ObjCParameter>,
        konst attributes: List<String>,
        comment: ObjCComment? = null
) : Stub<DeclarationDescriptor>(buildMethodName(selectors, parameters), comment)

class ObjCParameter(name: String,
                    override konst descriptor: ParameterDescriptor?,
                    konst type: ObjCType) : Stub<ParameterDescriptor>(name)

class ObjCProperty(name: String,
                   override konst descriptor: DeclarationDescriptorWithSource?,
                   konst type: ObjCType,
                   konst propertyAttributes: List<String>,
                   konst setterName: String? = null,
                   konst getterName: String? = null,
                   konst declarationAttributes: List<String> = emptyList(),
                   comment: ObjCComment? = null) : Stub<DeclarationDescriptorWithSource>(name, comment) {

    @Deprecated("", ReplaceWith("this.propertyAttributes"), DeprecationLevel.WARNING)
    konst attributes: List<String> get() = propertyAttributes
}

private fun buildMethodName(selectors: List<String>, parameters: List<ObjCParameter>): String =
        if (selectors.size == 1 && parameters.size == 0) {
            selectors[0]
        } else {
            assert(selectors.size == parameters.size)
            selectors.joinToString(separator = "")
        }
