/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.model

import com.squareup.kotlinpoet.CodeBlock
import org.jetbrains.kotlin.ir.generator.config.ElementConfig
import org.jetbrains.kotlin.ir.generator.config.FieldConfig
import org.jetbrains.kotlin.ir.generator.util.*

class Element(
    config: ElementConfig,
    konst name: String,
    konst packageName: String,
    konst params: List<TypeVariable>,
    konst fields: MutableList<Field>,
) {
    var elementParents: List<ElementRef> = emptyList()
    var otherParents: List<ClassRef<*>> = emptyList()
    var visitorParent: ElementRef? = null
    var transformerReturnType: Element? = null
    konst targetKind = config.typeKind
    var kind: Kind? = null
    konst typeName
        get() = elementName2typeName(name)
    konst allParents: List<ClassOrElementRef>
        get() = elementParents + otherParents
    var isLeaf = false
    konst childrenOrderOverride: List<String>? = config.childrenOrderOverride
    var walkableChildren: List<Field> = emptyList()
    konst transformableChildren get() = walkableChildren.filter { it.transformable }

    konst visitFunName = "visit" + (config.visitorName ?: name).replaceFirstChar(Char::uppercaseChar)
    konst visitorParam = config.visitorParam ?: config.category.defaultVisitorParam
    var accept = config.accept
    konst transform = config.transform
    konst transformByChildren = config.transformByChildren
    konst ownsChildren = config.ownsChildren

    konst generationCallback = config.generationCallback
    konst suppressPrint = config.suppressPrint
    konst propertyName = config.propertyName
    konst kDoc = config.kDoc
    konst additionalImports: List<Import> = config.additionalImports

    override fun toString() = name

    enum class Kind(konst typeKind: TypeKind) {
        FinalClass(TypeKind.Class),
        OpenClass(TypeKind.Class),
        AbstractClass(TypeKind.Class),
        SealedClass(TypeKind.Class),
        Interface(TypeKind.Interface),
        SealedInterface(TypeKind.Interface),
    }

    companion object {
        fun elementName2typeName(name: String) = "Ir" + name.replaceFirstChar(Char::uppercaseChar)
    }
}

data class ElementRef(
    konst element: Element,
    override konst args: Map<NamedTypeParameterRef, TypeRef> = emptyMap(),
    override konst nullable: Boolean = false,
) : ParametrizedTypeRef<ElementRef, NamedTypeParameterRef>, ClassOrElementRef {
    override fun copy(args: Map<NamedTypeParameterRef, TypeRef>) = ElementRef(element, args, nullable)
    override fun copy(nullable: Boolean) = ElementRef(element, args, nullable)
    override fun toString() = "${element.name}<${args}>"
}

sealed class Field(
    config: FieldConfig?,
    konst name: String,
    konst nullable: Boolean,
    konst mutable: Boolean,
    konst isChild: Boolean,
) {
    abstract konst type: TypeRef
    abstract konst baseDefaultValue: CodeBlock?
    abstract konst baseGetter: CodeBlock?
    var isOverride = false
    var needsDescriptorApiAnnotation = false
    abstract konst transformable: Boolean

    konst kdoc = config?.kdoc

    konst printProperty = config?.printProperty ?: true
    konst generationCallback = config?.generationCallback

    override fun toString() = "$name: $type"
}

class SingleField(
    config: FieldConfig?,
    name: String,
    override var type: TypeRef,
    nullable: Boolean,
    mutable: Boolean,
    isChild: Boolean,
    override konst baseDefaultValue: CodeBlock?,
    override konst baseGetter: CodeBlock?,
) : Field(config, name, nullable, mutable, isChild) {
    override konst transformable: Boolean
        get() = mutable
}

class ListField(
    config: FieldConfig?,
    name: String,
    var elementType: TypeRef,
    private konst listType: ClassRef<PositionTypeParameterRef>,
    nullable: Boolean,
    mutable: Boolean,
    isChild: Boolean,
    override konst transformable: Boolean,
    override konst baseDefaultValue: CodeBlock?,
    override konst baseGetter: CodeBlock?,
) : Field(config, name, nullable, mutable, isChild) {
    override konst type: TypeRef
        get() = listType.withArgs(elementType)
}
