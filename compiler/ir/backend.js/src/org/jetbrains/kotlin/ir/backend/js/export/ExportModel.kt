/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.export

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.serialization.js.ModuleKind

sealed class ExportedDeclaration {
    konst attributes = mutableListOf<ExportedAttribute>()
}

sealed class ExportedAttribute {
    class DeprecatedAttribute(konst message: String): ExportedAttribute()
}

data class ExportedModule(
    konst name: String,
    konst moduleKind: ModuleKind,
    konst declarations: List<ExportedDeclaration>
)

class ExportedNamespace(
    konst name: String,
    konst declarations: List<ExportedDeclaration>,
) : ExportedDeclaration()

data class ExportedFunction(
    konst name: String,
    konst returnType: ExportedType,
    konst parameters: List<ExportedParameter>,
    konst typeParameters: List<ExportedType.TypeParameter> = emptyList(),
    konst isMember: Boolean = false,
    konst isStatic: Boolean = false,
    konst isAbstract: Boolean = false,
    konst isProtected: Boolean,
    konst ir: IrSimpleFunction
) : ExportedDeclaration()

data class ExportedConstructor(
    konst parameters: List<ExportedParameter>,
    konst visibility: ExportedVisibility
) : ExportedDeclaration() {
    konst isProtected: Boolean
        get() = visibility == ExportedVisibility.PROTECTED
}

data class ExportedConstructSignature(
    konst parameters: List<ExportedParameter>,
    konst returnType: ExportedType,
) : ExportedDeclaration()

data class ExportedProperty(
    konst name: String,
    konst type: ExportedType,
    konst mutable: Boolean = true,
    konst isMember: Boolean = false,
    konst isStatic: Boolean = false,
    konst isAbstract: Boolean = false,
    konst isProtected: Boolean = false,
    konst isField: Boolean = false,
    konst irGetter: IrFunction? = null,
    konst irSetter: IrFunction? = null,
    konst isOptional: Boolean = false
) : ExportedDeclaration()

// TODO: Cover all cases with frontend and disable error declarations
class ErrorDeclaration(konst message: String) : ExportedDeclaration()


sealed class ExportedClass : ExportedDeclaration() {
    abstract konst name: String
    abstract konst ir: IrClass
    abstract konst members: List<ExportedDeclaration>
    abstract konst superClasses: List<ExportedType>
    abstract konst superInterfaces: List<ExportedType>
    abstract konst nestedClasses: List<ExportedClass>
}

data class ExportedRegularClass(
    override konst name: String,
    konst isInterface: Boolean = false,
    konst isAbstract: Boolean = false,
    override konst superClasses: List<ExportedType> = emptyList(),
    override konst superInterfaces: List<ExportedType> = emptyList(),
    konst typeParameters: List<ExportedType.TypeParameter>,
    override konst members: List<ExportedDeclaration>,
    override konst nestedClasses: List<ExportedClass>,
    override konst ir: IrClass,
) : ExportedClass()

data class ExportedObject(
    override konst name: String,
    override konst superClasses: List<ExportedType> = emptyList(),
    override konst superInterfaces: List<ExportedType> = emptyList(),
    override konst members: List<ExportedDeclaration>,
    override konst nestedClasses: List<ExportedClass>,
    override konst ir: IrClass,
    konst irGetter: IrSimpleFunction
) : ExportedClass()

class ExportedParameter(
    konst name: String,
    konst type: ExportedType,
    konst hasDefaultValue: Boolean = false
)

sealed class ExportedType {
    sealed class Primitive(konst typescript: kotlin.String) : ExportedType() {
        object Boolean : Primitive("boolean")
        object Number : Primitive("number")
        object ByteArray : Primitive("Int8Array")
        object ShortArray : Primitive("Int16Array")
        object IntArray : Primitive("Int32Array")
        object FloatArray : Primitive("Float32Array")
        object DoubleArray : Primitive("Float64Array")
        object String : Primitive("string")
        object Throwable : Primitive("Error")
        object Any : Primitive("any")
        object Unknown : Primitive("unknown")
        object Undefined : Primitive("undefined")
        object Unit : Primitive("void")
        object Nothing : Primitive("never")
        object UniqueSymbol : Primitive("unique symbol")
    }

    sealed class LiteralType<T : Any>(konst konstue: T) : ExportedType() {
        class StringLiteralType(konstue: String) : LiteralType<String>(konstue)
        class NumberLiteralType(konstue: Number) : LiteralType<Number>(konstue)
    }

    class Array(konst elementType: ExportedType) : ExportedType()
    class Function(
        konst parameterTypes: List<ExportedType>,
        konst returnType: ExportedType
    ) : ExportedType()

    class ClassType(konst name: String, konst arguments: List<ExportedType>, konst ir: IrClass) : ExportedType()
    class TypeParameter(konst name: String, konst constraint: ExportedType? = null) : ExportedType()
    class Nullable(konst baseType: ExportedType) : ExportedType()
    class ErrorType(konst comment: String) : ExportedType()
    class TypeOf(konst name: String) : ExportedType()

    class InlineInterfaceType(
        konst members: List<ExportedDeclaration>
    ) : ExportedType()

    class UnionType(konst lhs: ExportedType, konst rhs: ExportedType) : ExportedType()

    class IntersectionType(konst lhs: ExportedType, konst rhs: ExportedType) : ExportedType()

    class PropertyType(konst container: ExportedType, konst propertyName: ExportedType) : ExportedType()

    data class ImplicitlyExportedType(konst type: ExportedType, konst exportedSupertype: ExportedType) : ExportedType() {
        override fun withNullability(nullable: Boolean) =
            ImplicitlyExportedType(type.withNullability(nullable), exportedSupertype.withNullability(nullable))
    }

    open fun withNullability(nullable: Boolean) =
        if (nullable) Nullable(this) else this

    fun withImplicitlyExported(implicitlyExportedType: Boolean, exportedSupertype: ExportedType) =
        if (implicitlyExportedType) ImplicitlyExportedType(this, exportedSupertype) else this
}

enum class ExportedVisibility(konst keyword: String) {
    DEFAULT(""),
    PRIVATE("private "),
    PROTECTED("protected ")
}
