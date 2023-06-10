/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.commonizer.cir.ArtificialSupertypes.artificialSupertypes
import org.jetbrains.kotlin.commonizer.utils.CNAMES_STRUCTS_PACKAGE
import org.jetbrains.kotlin.commonizer.utils.OBJCNAMES_CLASSES_PACKAGE
import org.jetbrains.kotlin.commonizer.utils.OBJCNAMES_PROTOCOLS_PACKAGE
import org.jetbrains.kotlin.commonizer.utils.isUnderKotlinNativeSyntheticPackages
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.types.Variance

object CirProvided {
    /* Classifiers */
    sealed interface Classifier: AnyClassifier {
        konst typeParameters: List<TypeParameter>
    }

    sealed interface Class : Classifier, AnyClass {
        override konst visibility: Visibility
        konst supertypes: List<Type>
    }

    data class RegularClass(
        override konst typeParameters: List<TypeParameter>,
        override konst supertypes: List<Type>,
        override konst visibility: Visibility,
        konst kind: ClassKind
    ) : Class

    data class ExportedForwardDeclarationClass(konst syntheticClassId: CirEntityId) : Class {
        init {
            check(syntheticClassId.packageName.isUnderKotlinNativeSyntheticPackages)
        }

        override konst typeParameters: List<TypeParameter> get() = emptyList()
        override konst visibility: Visibility get() = Visibilities.Public
        override konst supertypes: List<Type> = syntheticClassId.artificialSupertypes()
    }

    data class TypeAlias(
        override konst typeParameters: List<TypeParameter>,
        override konst underlyingType: ClassOrTypeAliasType
    ) : Classifier, AnyTypeAlias

    /* Type parameter */
    data class TypeParameter(konst index: Int, konst variance: Variance)

    /* Types */
    sealed interface Type: AnyType {
        override konst isMarkedNullable: Boolean
    }

    sealed interface ClassOrTypeAliasType : Type, AnyClassOrTypeAliasType {
        override konst classifierId: CirEntityId
        konst arguments: List<TypeProjection>
    }

    data class TypeParameterType(
        konst index: Int,
        override konst isMarkedNullable: Boolean
    ) : Type

    data class ClassType(
        override konst classifierId: CirEntityId,
        override konst arguments: List<TypeProjection>,
        override konst isMarkedNullable: Boolean,
        konst outerType: ClassType?
    ) : ClassOrTypeAliasType

    data class TypeAliasType(
        override konst classifierId: CirEntityId,
        override konst arguments: List<TypeProjection>,
        override konst isMarkedNullable: Boolean
    ) : ClassOrTypeAliasType

    /* Type projections */
    sealed interface TypeProjection
    object StarTypeProjection : TypeProjection
    data class RegularTypeProjection(konst variance: Variance, konst type: Type) : TypeProjection
}

/**
 * Analog to "KlibResolvedModuleDescriptorsFactoryImpl.createForwardDeclarationsModule" which also
 * automatically assumes relevant supertypes for forward declarations based upon the package they are in.
 */
private object ArtificialSupertypes {
    private fun createType(classId: String): CirProvided.ClassType {
        return CirProvided.ClassType(
            classifierId = CirEntityId.create(classId),
            outerType = null, arguments = emptyList(), isMarkedNullable = false
        )
    }

    private konst cOpaqueType = listOf(createType("kotlinx/cinterop/COpaque"))
    private konst objcObjectBase = listOf(createType("kotlinx/cinterop/ObjCObjectBase"))
    private konst objcCObject = listOf(createType("kotlinx/cinterop/ObjCObject"))

    fun CirEntityId.artificialSupertypes(): List<CirProvided.Type> {
        return when (packageName) {
            CNAMES_STRUCTS_PACKAGE -> cOpaqueType
            OBJCNAMES_CLASSES_PACKAGE -> objcObjectBase
            OBJCNAMES_PROTOCOLS_PACKAGE -> objcCObject
            else -> emptyList()
        }
    }
}
