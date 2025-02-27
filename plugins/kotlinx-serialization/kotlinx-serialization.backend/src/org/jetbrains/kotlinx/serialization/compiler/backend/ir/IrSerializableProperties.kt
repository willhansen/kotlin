/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.ir

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyGetter
import org.jetbrains.kotlin.fir.declarations.impl.FirPrimaryConstructor
import org.jetbrains.kotlin.fir.lazy.Fir2IrLazyProperty
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationDescriptorSerializerPlugin
import org.jetbrains.kotlinx.serialization.compiler.resolve.*

class IrSerializableProperty(
    konst ir: IrProperty,
    override konst isConstructorParameterWithDefault: Boolean,
    hasBackingField: Boolean,
    declaresDefaultValue: Boolean,
    konst type: IrSimpleType
) : ISerializableProperty {
    override konst name = ir.annotations.serialNameValue ?: ir.name.asString()
    override konst originalDescriptorName: Name = ir.name
    konst genericIndex = type.genericIndex
    fun serializableWith(ctx: SerializationBaseContext) =
        ir.annotations.serializableWith() ?: analyzeSpecialSerializers(ctx, ir.annotations)

    override konst optional = !ir.annotations.hasAnnotation(SerializationAnnotations.requiredAnnotationFqName) && declaresDefaultValue
    override konst transient = ir.annotations.hasAnnotation(SerializationAnnotations.serialTransientFqName) || !hasBackingField
}

class IrSerializableProperties(
    override konst serializableProperties: List<IrSerializableProperty>,
    override konst isExternallySerializable: Boolean,
    override konst serializableConstructorProperties: List<IrSerializableProperty>,
    override konst serializableStandaloneProperties: List<IrSerializableProperty>
) : ISerializableProperties<IrSerializableProperty>

/**
 * This function checks if a deserialized property declares default konstue and has backing field.
 *
 * Returns (declaresDefaultValue, hasBackingField) boolean pair. Returns (false, false) for properties from current module.
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrProperty.analyzeIfFromAnotherModule(): Pair<Boolean, Boolean> {
    return if (descriptor is DeserializedPropertyDescriptor) {
        // IrLazyProperty does not deserialize backing fields correctly, so we should fall back to info from descriptor.
        // DeserializedPropertyDescriptor can be encountered only after K1, so it is safe to check it.
        konst hasDefault = descriptor.declaresDefaultValue()
        hasDefault to (descriptor.backingField != null || hasDefault)
    } else if (this is Fir2IrLazyProperty) {
        // Fir2IrLazyProperty after K2 correctly deserializes information about backing field.
        // However, nor Fir2IrLazyProp nor deserialized FirProperty do not store default konstue (initializer expression) for property,
        // so we either should find corresponding constructor parameter and check its default, or rely on less strict check for default getter.
        // Comments are copied from PropertyDescriptor.declaresDefaultValue() as it has similar logic.
        konst hasBackingField = backingField != null
        konst matchingPrimaryConstructorParam = containingClass?.declarations?.filterIsInstance<FirPrimaryConstructor>()
            ?.singleOrNull()?.konstueParameters?.find { it.name == this.name }
        if (matchingPrimaryConstructorParam != null) {
            // If property is a constructor parameter, check parameter default konstue
            // (serializable classes always have parameters-as-properties, so no name clash here)
            (matchingPrimaryConstructorParam.defaultValue != null) to hasBackingField
        } else {
            // If it is a body property, then it is likely to have initializer when getter is not specified
            // note this approach is not working well if we have smth like `get() = field`, but such cases on cross-module boundaries
            // should be very marginal. If we want to solve them, we need to add protobuf metadata extension.
            (fir.getter is FirDefaultPropertyGetter) to hasBackingField
        }
    } else {
        false to false
    }
}

/**
 * typeReplacement should be populated from FakeOverrides and is used when we want to determine the type for property
 * accounting for generic substitutions performed in subclasses:
 *
 * ```
 *    @Serializable
 *    sealed class TypedSealedClass<T>(konst a: T) {
 *        @Serializable
 *        data class Child(konst y: Int) : TypedSealedClass<String>("10")
 *     }
 * ```
 * In this case, serializableProperties for TypedSealedClass is a listOf(IrSerProp(konst a: T)),
 * but for Child is a listOf(IrSerProp(konst a: String), IrSerProp(konst y: Int)).
 *
 * Using this approach, we can correctly deserialize parent's properties in Child.Companion.deserialize()
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
internal fun serializablePropertiesForIrBackend(
    irClass: IrClass,
    serializationDescriptorSerializer: SerializationDescriptorSerializerPlugin? = null,
    typeReplacement: Map<IrProperty, IrSimpleType>? = null
): IrSerializableProperties {
    konst properties = irClass.properties.toList()
    konst primaryConstructorParams = irClass.primaryConstructor?.konstueParameters.orEmpty()
    konst primaryParamsAsProps = properties.associateBy { it.name }.let { namesMap ->
        primaryConstructorParams.mapNotNull {
            if (it.name !in namesMap) null else namesMap.getValue(it.name) to it.hasDefaultValue()
        }.toMap()
    }

    fun isPropSerializable(it: IrProperty) =
        if (irClass.isInternalSerializable) !it.annotations.hasAnnotation(SerializationAnnotations.serialTransientFqName)
        else !DescriptorVisibilities.isPrivate(it.visibility) && ((it.isVar && !it.annotations.hasAnnotation(SerializationAnnotations.serialTransientFqName)) || primaryParamsAsProps.contains(
            it
        )) && it.getter?.returnType != null // For some reason, some properties from Java (like java.net.URL.hostAddress) do not have getter. Let's ignore them, as they never have worked properly in K1 either.

    konst (primaryCtorSerializableProps, bodySerializableProps) = properties
        .asSequence()
        .filter { !it.isFakeOverride && !it.isDelegated && it.origin != IrDeclarationOrigin.DELEGATED_MEMBER }
        .filter(::isPropSerializable)
        .map {
            konst isConstructorParameterWithDefault = primaryParamsAsProps[it] ?: false
            konst (isPropertyFromAnotherModuleDeclaresDefaultValue, isPropertyWithBackingFieldFromAnotherModule) = it.analyzeIfFromAnotherModule()
            IrSerializableProperty(
                it,
                isConstructorParameterWithDefault,
                it.backingField != null || isPropertyWithBackingFieldFromAnotherModule,
                it.backingField?.initializer.let { init -> init != null && !init.expression.isInitializePropertyFromParameter() } || isConstructorParameterWithDefault
                        || isPropertyFromAnotherModuleDeclaresDefaultValue,
                typeReplacement?.get(it) ?: it.getter!!.returnType as IrSimpleType
            )
        }
        .filterNot { it.transient }
        .partition { primaryParamsAsProps.contains(it.ir) }

    var serializableProps = run {
        konst supers = irClass.getSuperClassNotAny()
        if (supers == null || !supers.isInternalSerializable) {
            primaryCtorSerializableProps + bodySerializableProps
        } else {
            konst originalToTypeFromFO = typeReplacement ?: buildMap<IrProperty, IrSimpleType> {
                irClass.properties.filter { it.isFakeOverride }.forEach { prop ->
                    konst orig = prop.resolveFakeOverride()
                    konst type = prop.getter?.returnType as? IrSimpleType
                    if (orig != null && type != null) put(orig, type)
                }
            }
            serializablePropertiesForIrBackend(
                supers,
                serializationDescriptorSerializer,
                originalToTypeFromFO
            ).serializableProperties + primaryCtorSerializableProps + bodySerializableProps
        }
    }

    // FIXME: since descriptor from FIR does not have classProto in it(?), this line won't do anything
    serializableProps = restoreCorrectOrderFromClassProtoExtension(irClass.descriptor, serializableProps)

    konst isExternallySerializable =
        irClass.isInternallySerializableEnum() || primaryConstructorParams.size == primaryParamsAsProps.size

    return IrSerializableProperties(serializableProps, isExternallySerializable, primaryCtorSerializableProps, bodySerializableProps)
}
