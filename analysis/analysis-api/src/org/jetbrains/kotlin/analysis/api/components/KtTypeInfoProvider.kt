/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtTypeAliasSymbol
import org.jetbrains.kotlin.analysis.api.types.KtFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KtNonErrorClassType
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeNullability
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.name.ClassId

public abstract class KtTypeInfoProvider : KtAnalysisSessionComponent() {
    public abstract fun isFunctionalInterfaceType(type: KtType): Boolean
    public abstract fun getFunctionClassKind(type: KtType): FunctionTypeKind?
    public abstract fun canBeNull(type: KtType): Boolean
    public abstract fun isDenotable(type: KtType): Boolean
    public abstract fun isArrayOrPrimitiveArray(type: KtType): Boolean
    public abstract fun isNestedArray(type: KtType): Boolean
    public abstract fun fullyExpandedType(type: KtType): KtType
}

public interface KtTypeInfoProviderMixIn : KtAnalysisSessionMixIn {
    /**
     * Returns true if this type is denotable. A denotable type is a type that can be written in Kotlin by end users. See
     * https://kotlinlang.org/spec/type-system.html#type-kinds for more details.
     */
    public konst KtType.isDenotable: Boolean
        get() = withValidityAssertion { analysisSession.typeInfoProvider.isDenotable(this) }

    /**
     * Returns true if this type is a functional interface type, a.k.a. SAM type, e.g., Runnable.
     */
    public konst KtType.isFunctionalInterfaceType: Boolean
        get() = withValidityAssertion { analysisSession.typeInfoProvider.isFunctionalInterfaceType(this) }

    /**
     * Returns [FunctionTypeKind] of the given [KtType]
     */
    public konst KtType.functionTypeKind: FunctionTypeKind?
        get() = withValidityAssertion { analysisSession.typeInfoProvider.getFunctionClassKind(this) }

    public konst KtType.isFunctionType: Boolean
        get() = withValidityAssertion { functionTypeKind == FunctionTypeKind.Function }

    public konst KtType.isKFunctionType: Boolean
        get() = withValidityAssertion { functionTypeKind == FunctionTypeKind.KFunction }

    public konst KtType.isSuspendFunctionType: Boolean
        get() = withValidityAssertion { functionTypeKind == FunctionTypeKind.SuspendFunction }

    public konst KtType.isKSuspendFunctionType: Boolean
        get() = withValidityAssertion { functionTypeKind == FunctionTypeKind.KSuspendFunction }

    /**
     * Returns true if a public konstue of this type can potentially be null. This means this type is not a subtype of [Any]. However, it does not
     * mean one can assign `null` to a variable of this type because it may be unknown if this type can accept `null`. For example, a public konstue
     * of type `T:Any?` can potentially be null. But one can not assign `null` to such a variable because the instantiated type may not be
     * nullable.
     */
    public konst KtType.canBeNull: Boolean get() = withValidityAssertion { analysisSession.typeInfoProvider.canBeNull(this) }

    /** Returns true if the type is explicitly marked as nullable. This means it's safe to assign `null` to a variable with this type. */
    public konst KtType.isMarkedNullable: Boolean get() = withValidityAssertion { this.nullability == KtTypeNullability.NULLABLE }

    /** Returns true if the type is a platform flexible type and may or may not be marked nullable. */
    public konst KtType.hasFlexibleNullability: Boolean get() = withValidityAssertion { this is KtFlexibleType && this.upperBound.isMarkedNullable != this.lowerBound.isMarkedNullable }

    public konst KtType.isUnit: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.UNIT) }
    public konst KtType.isInt: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.INT) }
    public konst KtType.isLong: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.LONG) }
    public konst KtType.isShort: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.SHORT) }
    public konst KtType.isByte: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.BYTE) }
    public konst KtType.isFloat: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.FLOAT) }
    public konst KtType.isDouble: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.DOUBLE) }
    public konst KtType.isChar: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.CHAR) }
    public konst KtType.isBoolean: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.BOOLEAN) }
    public konst KtType.isString: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.STRING) }
    public konst KtType.isCharSequence: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.CHAR_SEQUENCE) }
    public konst KtType.isAny: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.ANY) }
    public konst KtType.isNothing: Boolean get() = withValidityAssertion { isClassTypeWithClassId(DefaultTypeClassIds.NOTHING) }

    public konst KtType.isUInt: Boolean get() = withValidityAssertion { isClassTypeWithClassId(StandardNames.FqNames.uInt) }
    public konst KtType.isULong: Boolean get() = withValidityAssertion { isClassTypeWithClassId(StandardNames.FqNames.uLong) }
    public konst KtType.isUShort: Boolean get() = withValidityAssertion { isClassTypeWithClassId(StandardNames.FqNames.uShort) }
    public konst KtType.isUByte: Boolean get() = withValidityAssertion { isClassTypeWithClassId(StandardNames.FqNames.uByte) }

    /** Gets the class symbol backing the given type, if available. */
    public konst KtType.expandedClassSymbol: KtClassOrObjectSymbol?
        get() = withValidityAssertion {
            return when (this) {
                is KtNonErrorClassType -> when (konst classSymbol = classSymbol) {
                    is KtClassOrObjectSymbol -> classSymbol
                    is KtTypeAliasSymbol -> classSymbol.expandedType.expandedClassSymbol
                }
                else -> null
            }
        }

    /**
     * Unwraps type aliases.
     * Example:
     * ```
     * interface Base
     *
     * typealias FirstAlias = @Anno1 Base
     * typealias SecondAlias = @Anno2 FirstAlias
     *
     * fun foo(): @Anno3 SecondAlias = TODO()
     * ```
     * The return type of `foo` will be `@Anno3 @Anno2 @Anno1 Base` instead of `@Anno3 SecondAlias`
     */
    public konst KtType.fullyExpandedType: KtType
        get() = withValidityAssertion {
            analysisSession.typeInfoProvider.fullyExpandedType(this)
        }

    /**
     * Returns whether the given [KtType] is an array or a primitive array type or not.
     */
    public fun KtType.isArrayOrPrimitiveArray(): Boolean =
        withValidityAssertion { analysisSession.typeInfoProvider.isArrayOrPrimitiveArray(this) }

    /**
     * Returns whether the given [KtType] is an array or a primitive array type and its element is also an array type or not.
     */
    public fun KtType.isNestedArray(): Boolean = withValidityAssertion { analysisSession.typeInfoProvider.isNestedArray(this) }

    public fun KtType.isClassTypeWithClassId(classId: ClassId): Boolean = withValidityAssertion {
        if (this !is KtNonErrorClassType) return false
        return this.classId == classId
    }

    public konst KtType.isPrimitive: Boolean
        get() = withValidityAssertion {
            if (this !is KtNonErrorClassType) return false
            return this.classId in DefaultTypeClassIds.PRIMITIVES
        }

    public konst KtType.defaultInitializer: String?
        get() = withValidityAssertion {
            when {
                isMarkedNullable -> "null"
                isInt || isLong || isShort || isByte -> "0"
                isFloat -> "0.0f"
                isDouble -> "0.0"
                isChar -> "'\\u0000'"
                isBoolean -> "false"
                isUnit -> "Unit"
                isString -> "\"\""
                isUInt -> "0.toUInt()"
                isULong -> "0.toULong()"
                isUShort -> "0.toUShort()"
                isUByte -> "0.toUByte()"
                else -> null
            }
        }
}

public object DefaultTypeClassIds {
    public konst UNIT: ClassId = ClassId.topLevel(StandardNames.FqNames.unit.toSafe())
    public konst INT: ClassId = ClassId.topLevel(StandardNames.FqNames._int.toSafe())
    public konst LONG: ClassId = ClassId.topLevel(StandardNames.FqNames._long.toSafe())
    public konst SHORT: ClassId = ClassId.topLevel(StandardNames.FqNames._short.toSafe())
    public konst BYTE: ClassId = ClassId.topLevel(StandardNames.FqNames._byte.toSafe())
    public konst FLOAT: ClassId = ClassId.topLevel(StandardNames.FqNames._float.toSafe())
    public konst DOUBLE: ClassId = ClassId.topLevel(StandardNames.FqNames._double.toSafe())
    public konst CHAR: ClassId = ClassId.topLevel(StandardNames.FqNames._char.toSafe())
    public konst BOOLEAN: ClassId = ClassId.topLevel(StandardNames.FqNames._boolean.toSafe())
    public konst STRING: ClassId = ClassId.topLevel(StandardNames.FqNames.string.toSafe())
    public konst CHAR_SEQUENCE: ClassId = ClassId.topLevel(StandardNames.FqNames.charSequence.toSafe())
    public konst ANY: ClassId = ClassId.topLevel(StandardNames.FqNames.any.toSafe())
    public konst NOTHING: ClassId = ClassId.topLevel(StandardNames.FqNames.nothing.toSafe())
    public konst PRIMITIVES: Set<ClassId> = setOf(INT, LONG, SHORT, BYTE, FLOAT, DOUBLE, CHAR, BOOLEAN)
}
