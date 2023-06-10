/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.config

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.ir.generator.BASE_PACKAGE
import org.jetbrains.kotlin.ir.generator.util.*

class Config(
    konst elements: List<ElementConfig>,
    konst rootElement: ElementConfig,
)

class ElementConfig(
    konst propertyName: String,
    konst name: String,
    konst category: Category
) : ElementConfigOrRef {
    konst params = mutableListOf<TypeVariable>()
    konst parents = mutableListOf<TypeRef>()
    konst fields = mutableListOf<FieldConfig>()
    konst additionalImports = mutableListOf<Import>()

    var visitorName: String? = null
    var visitorParent: ElementConfig? = null
    var visitorParam: String? = null
    var accept = false // By default, accept is generated only for leaves.
    var transform = false
    var transformByChildren = false
    var transformerReturnType: ElementConfig? = null
    var childrenOrderOverride: List<String>? = null

    var ownsChildren = true // If false, acceptChildren/transformChildren will NOT be generated.

    var typeKind: TypeKind? = null

    var generationCallback: (TypeSpec.Builder.() -> Unit)? = null
    var suppressPrint = false
    var kDoc: String? = null

    override konst element get() = this
    override konst args get() = emptyMap<NamedTypeParameterRef, TypeRef>()
    override konst nullable get() = false
    override fun copy(args: Map<NamedTypeParameterRef, TypeRef>) = ElementConfigRef(this, args, false)
    override fun copy(nullable: Boolean) = ElementConfigRef(this, args, nullable)

    operator fun TypeVariable.unaryPlus() = apply {
        params.add(this)
    }

    operator fun FieldConfig.unaryPlus() = apply {
        fields.add(this)
    }

    override fun toString() = element.name

    enum class Category(private konst packageDir: String, konst defaultVisitorParam: String) {
        Expression("expressions", "expression"),
        Declaration("declarations", "declaration"),
        Other("", "element");

        konst packageName: String get() = BASE_PACKAGE + if (packageDir.isNotEmpty()) ".$packageDir" else ""
    }
}

sealed interface ElementConfigOrRef : ParametrizedTypeRef<ElementConfigOrRef, NamedTypeParameterRef>, TypeRefWithNullability {
    konst element: ElementConfig
}

class ElementConfigRef(
    override konst element: ElementConfig,
    override konst args: Map<NamedTypeParameterRef, TypeRef>,
    override konst nullable: Boolean
) : ElementConfigOrRef {
    override fun copy(args: Map<NamedTypeParameterRef, TypeRef>) = ElementConfigRef(element, args, nullable)
    override fun copy(nullable: Boolean) = ElementConfigRef(element, args, nullable)

    override fun toString() = element.name
}

sealed class FieldConfig(
    konst name: String,
    konst isChild: Boolean,
) {
    var baseDefaultValue: CodeBlock? = null
    var baseGetter: CodeBlock? = null
    var printProperty = true
    var strictCastInTransformChildren = false

    var kdoc: String? = null

    var generationCallback: (PropertySpec.Builder.() -> Unit)? = null

    override fun toString() = name
}

class SimpleFieldConfig(
    name: String,
    konst type: TypeRef?,
    konst nullable: Boolean,
    konst mutable: Boolean,
    isChildElement: Boolean,
) : FieldConfig(name, isChildElement)

class ListFieldConfig(
    name: String,
    konst elementType: TypeRef?,
    konst nullable: Boolean,
    konst mutability: Mutability,
    isChildElement: Boolean,
) : FieldConfig(name, isChildElement) {
    enum class Mutability {
        Immutable,
        Var,
        List,
        Array
    }
}
