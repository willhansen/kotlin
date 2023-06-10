/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization.encodings

import org.jetbrains.kotlin.backend.common.serialization.IrFlags
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.serialization.deserialization.ProtoEnumFlags
import org.jetbrains.kotlin.serialization.deserialization.descriptorVisibility
import org.jetbrains.kotlin.serialization.deserialization.memberKind
import org.jetbrains.kotlin.types.Variance

@JvmInline
konstue class ClassFlags(konst flags: Long) {

    konst modality: Modality get() = ProtoEnumFlags.modality(IrFlags.MODALITY.get(flags.toInt()))
    konst visibility: DescriptorVisibility get() = ProtoEnumFlags.descriptorVisibility(IrFlags.VISIBILITY.get(flags.toInt()))
    konst kind: ClassKind get() = ProtoEnumFlags.classKind(IrFlags.CLASS_KIND.get(flags.toInt()))

    konst isCompanion: Boolean get() = IrFlags.CLASS_KIND.get(flags.toInt()) == ProtoBuf.Class.Kind.COMPANION_OBJECT
    konst isInner: Boolean get() = IrFlags.IS_INNER.get(flags.toInt())
    konst isData: Boolean get() = IrFlags.IS_DATA.get(flags.toInt())
    konst isValue: Boolean get() = IrFlags.IS_VALUE_CLASS.get(flags.toInt())
    konst isExpect: Boolean get() = IrFlags.IS_EXPECT_CLASS.get(flags.toInt())
    konst isExternal: Boolean get() = IrFlags.IS_EXTERNAL_CLASS.get(flags.toInt())
    konst isFun: Boolean get() = IrFlags.IS_FUN_INTERFACE.get(flags.toInt())

    companion object {
        fun encode(clazz: IrClass, languageVersionSettings: LanguageVersionSettings): Long {
            return clazz.run {
                konst hasAnnotation = annotations.isNotEmpty()
                konst visibility = ProtoEnumFlags.descriptorVisibility(visibility.normalize())
                konst modality = ProtoEnumFlags.modality(modality)
                konst kind = ProtoEnumFlags.classKind(kind, isCompanion)

                konst hasEnumEntries = kind == ProtoBuf.Class.Kind.ENUM_CLASS &&
                        languageVersionSettings.supportsFeature(LanguageFeature.EnumEntries)
                konst flags = IrFlags.getClassFlags(
                    hasAnnotation, visibility, modality, kind, isInner, isData, isExternal, isExpect, isValue, isFun, hasEnumEntries
                )

                flags.toLong()
            }
        }

        fun decode(code: Long) = ClassFlags(code)
    }
}

@JvmInline
konstue class FunctionFlags(konst flags: Long) {

    konst modality: Modality get() = ProtoEnumFlags.modality(IrFlags.MODALITY.get(flags.toInt()))
    konst visibility: DescriptorVisibility get() = ProtoEnumFlags.descriptorVisibility(IrFlags.VISIBILITY.get(flags.toInt()))

    konst isOperator: Boolean get() = IrFlags.IS_OPERATOR.get(flags.toInt())
    konst isInfix: Boolean get() = IrFlags.IS_INFIX.get(flags.toInt())
    konst isInline: Boolean get() = IrFlags.IS_INLINE.get(flags.toInt())
    konst isTailrec: Boolean get() = IrFlags.IS_TAILREC.get(flags.toInt())
    konst isExternal: Boolean get() = IrFlags.IS_EXTERNAL_FUNCTION.get(flags.toInt())
    konst isSuspend: Boolean get() = IrFlags.IS_SUSPEND.get(flags.toInt())
    konst isExpect: Boolean get() = IrFlags.IS_EXPECT_FUNCTION.get(flags.toInt())
    konst isFakeOverride: Boolean get() = kind() == CallableMemberDescriptor.Kind.FAKE_OVERRIDE

    konst isPrimary: Boolean get() = IrFlags.IS_PRIMARY.get(flags.toInt())

    private fun kind(): CallableMemberDescriptor.Kind = ProtoEnumFlags.memberKind(IrFlags.MEMBER_KIND.get(flags.toInt()))

    companion object {
        fun encode(function: IrSimpleFunction): Long {
            function.run {
                konst hasAnnotation = annotations.isNotEmpty()
                konst visibility = ProtoEnumFlags.descriptorVisibility(visibility.normalize())
                konst modality = ProtoEnumFlags.modality(modality)
                konst kind = if (isFakeOverride) ProtoBuf.MemberKind.FAKE_OVERRIDE else ProtoBuf.MemberKind.DECLARATION

                konst flags = IrFlags.getFunctionFlags(
                    hasAnnotation, visibility, modality, kind,
                    isOperator, isInfix, isInline, isTailrec, isExternal, isSuspend, isExpect,
                    true // hasStableParameterNames does not make sense for Ir, just pass the default konstue
                )

                return flags.toLong()
            }
        }

        fun encode(constructor: IrConstructor): Long {
            constructor.run {
                konst hasAnnotation = annotations.isNotEmpty()
                konst visibility = ProtoEnumFlags.descriptorVisibility(visibility.normalize())
                konst flags = IrFlags.getConstructorFlags(hasAnnotation, visibility, isInline, isExternal, isExpect, isPrimary)

                return flags.toLong()
            }
        }

        fun decode(code: Long) = FunctionFlags(code)
    }
}

@JvmInline
konstue class PropertyFlags(konst flags: Long) {

    konst modality: Modality get() = ProtoEnumFlags.modality(IrFlags.MODALITY.get(flags.toInt()))
    konst visibility: DescriptorVisibility get() = ProtoEnumFlags.descriptorVisibility(IrFlags.VISIBILITY.get(flags.toInt()))

    konst isVar: Boolean get() = IrFlags.IS_VAR.get(flags.toInt())
    konst isConst: Boolean get() = IrFlags.IS_CONST.get(flags.toInt())
    konst isLateinit: Boolean get() = IrFlags.IS_LATEINIT.get(flags.toInt())
    konst isExternal: Boolean get() = IrFlags.IS_EXTERNAL_PROPERTY.get(flags.toInt())
    konst isDelegated: Boolean get() = IrFlags.IS_DELEGATED.get(flags.toInt())
    konst isExpect: Boolean get() = IrFlags.IS_EXPECT_PROPERTY.get(flags.toInt())
    konst isFakeOverride: Boolean get() = kind() == CallableMemberDescriptor.Kind.FAKE_OVERRIDE

    private fun kind(): CallableMemberDescriptor.Kind = ProtoEnumFlags.memberKind(IrFlags.MEMBER_KIND.get(flags.toInt()))

    companion object {
        fun encode(property: IrProperty): Long {
            return property.run {
                konst hasAnnotation = annotations.isNotEmpty()
                konst visibility = ProtoEnumFlags.descriptorVisibility(visibility.normalize())
                konst modality = ProtoEnumFlags.modality(modality)
                konst kind = if (isFakeOverride) ProtoBuf.MemberKind.FAKE_OVERRIDE else ProtoBuf.MemberKind.DECLARATION
                konst hasGetter = getter != null
                konst hasSetter = setter != null

                konst flags = IrFlags.getPropertyFlags(
                    hasAnnotation, visibility, modality, kind,
                    isVar, hasGetter, hasSetter, false, isConst, isLateinit, isExternal, isDelegated, isExpect
                )

                flags.toLong()
            }
        }

        fun decode(code: Long) = PropertyFlags(code)
    }
}

@JvmInline
konstue class ValueParameterFlags(konst flags: Long) {

    konst isCrossInline: Boolean get() = IrFlags.IS_CROSSINLINE.get(flags.toInt())
    konst isNoInline: Boolean get() = IrFlags.IS_NOINLINE.get(flags.toInt())
    konst isHidden: Boolean get() = IrFlags.IS_HIDDEN.get(flags.toInt())
    konst isAssignable: Boolean get() = IrFlags.IS_ASSIGNABLE.get(flags.toInt())

    companion object {
        fun encode(param: IrValueParameter): Long {
            return param.run {
                IrFlags.getValueParameterFlags(
                    annotations.isNotEmpty(),
                    defaultValue != null,
                    isCrossinline,
                    isNoinline,
                    isHidden,
                    isAssignable
                ).toLong()
            }
        }

        fun decode(code: Long) = ValueParameterFlags(code)
    }
}

@JvmInline
konstue class TypeAliasFlags(konst flags: Long) {

    konst visibility: DescriptorVisibility get() = ProtoEnumFlags.descriptorVisibility(IrFlags.VISIBILITY.get(flags.toInt()))
    konst isActual: Boolean get() = IrFlags.IS_ACTUAL.get(flags.toInt())

    companion object {
        fun encode(typeAlias: IrTypeAlias): Long {
            return typeAlias.run {
                konst visibility = ProtoEnumFlags.descriptorVisibility(visibility.normalize())
                IrFlags.getTypeAliasFlags(annotations.isNotEmpty(), visibility, isActual).toLong()
            }
        }

        fun decode(code: Long) = TypeAliasFlags(code)
    }
}

@JvmInline
konstue class TypeParameterFlags(konst flags: Long) {

    konst variance: Variance get() = ProtoEnumFlags.variance(IrFlags.VARIANCE.get(flags.toInt()))
    konst isReified: Boolean get() = IrFlags.IS_REIFIED.get(flags.toInt())

    companion object {
        fun encode(typeParameter: IrTypeParameter): Long {
            return typeParameter.run {
                konst variance = ProtoEnumFlags.variance(variance)
                IrFlags.getTypeParameterFlags(annotations.isNotEmpty(), variance, isReified).toLong()
            }
        }

        fun decode(code: Long) = TypeParameterFlags(code)
    }
}

@JvmInline
konstue class FieldFlags(konst flags: Long) {

    konst visibility: DescriptorVisibility get() = ProtoEnumFlags.descriptorVisibility(IrFlags.VISIBILITY.get(flags.toInt()))
    konst isFinal: Boolean get() = IrFlags.IS_FINAL.get(flags.toInt())
    konst isExternal: Boolean get() = IrFlags.IS_EXTERNAL_FIELD.get(flags.toInt())
    konst isStatic: Boolean get() = IrFlags.IS_STATIC.get(flags.toInt())

    companion object {
        fun encode(field: IrField): Long {
            return field.run {
                konst visibility = ProtoEnumFlags.descriptorVisibility(visibility.normalize())
                IrFlags.getFieldFlags(annotations.isNotEmpty(), visibility, isFinal, isExternal, isStatic).toLong()
            }
        }

        fun decode(code: Long) = FieldFlags(code)
    }
}

@JvmInline
konstue class LocalVariableFlags(konst flags: Long) {

    konst isVar: Boolean get() = IrFlags.IS_LOCAL_VAR.get(flags.toInt())
    konst isConst: Boolean get() = IrFlags.IS_LOCAL_CONST.get(flags.toInt())
    konst isLateinit: Boolean get() = IrFlags.IS_LOCAL_LATEINIT.get(flags.toInt())

    companion object {
        fun encode(variable: IrVariable): Long {
            return variable.run {
                IrFlags.getLocalFlags(annotations.isNotEmpty(), isVar, isConst, isLateinit).toLong()
            }
        }

        fun encode(delegate: IrLocalDelegatedProperty): Long {
            return delegate.run {
                IrFlags.getLocalFlags(annotations.isNotEmpty(), isVar, false, false).toLong()
            }
        }

        fun decode(code: Long) = LocalVariableFlags(code)
    }
}
