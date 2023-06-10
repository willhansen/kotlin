/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.load.java.lazy

import org.jetbrains.kotlin.builtins.ReflectionTypes
import org.jetbrains.kotlin.descriptors.ClassOrPackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SupertypeLoopChecker
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.java.*
import org.jetbrains.kotlin.load.java.JavaModuleAnnotationsProvider
import org.jetbrains.kotlin.load.java.components.JavaPropertyInitializerEkonstuator
import org.jetbrains.kotlin.load.java.components.JavaResolverCache
import org.jetbrains.kotlin.load.java.components.SignaturePropagator
import org.jetbrains.kotlin.load.java.lazy.types.JavaTypeResolver
import org.jetbrains.kotlin.load.java.sources.JavaSourceElementFactory
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameterListOwner
import org.jetbrains.kotlin.load.java.typeEnhancement.SignatureEnhancement
import org.jetbrains.kotlin.load.kotlin.DeserializedDescriptorResolver
import org.jetbrains.kotlin.load.kotlin.KotlinClassFinder
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.resolve.jvm.SyntheticJavaPartsProvider
import org.jetbrains.kotlin.resolve.sam.SamConversionResolver
import org.jetbrains.kotlin.serialization.deserialization.ErrorReporter
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.checker.NewKotlinTypeChecker

class JavaResolverComponents(
    konst storageManager: StorageManager,
    konst finder: JavaClassFinder,
    konst kotlinClassFinder: KotlinClassFinder,
    konst deserializedDescriptorResolver: DeserializedDescriptorResolver,
    konst signaturePropagator: SignaturePropagator,
    konst errorReporter: ErrorReporter,
    konst javaResolverCache: JavaResolverCache,
    konst javaPropertyInitializerEkonstuator: JavaPropertyInitializerEkonstuator,
    konst samConversionResolver: SamConversionResolver,
    konst sourceElementFactory: JavaSourceElementFactory,
    konst moduleClassResolver: ModuleClassResolver,
    konst packagePartProvider: PackagePartProvider,
    konst supertypeLoopChecker: SupertypeLoopChecker,
    konst lookupTracker: LookupTracker,
    konst module: ModuleDescriptor,
    konst reflectionTypes: ReflectionTypes,
    konst annotationTypeQualifierResolver: AnnotationTypeQualifierResolver,
    konst signatureEnhancement: SignatureEnhancement,
    konst javaClassesTracker: JavaClassesTracker,
    konst settings: JavaResolverSettings,
    konst kotlinTypeChecker: NewKotlinTypeChecker,
    konst javaTypeEnhancementState: JavaTypeEnhancementState,
    konst javaModuleResolver: JavaModuleAnnotationsProvider,
    konst syntheticPartsProvider: SyntheticJavaPartsProvider = SyntheticJavaPartsProvider.EMPTY
) {
    fun replace(
        javaResolverCache: JavaResolverCache = this.javaResolverCache
    ) = JavaResolverComponents(
        storageManager, finder, kotlinClassFinder, deserializedDescriptorResolver,
        signaturePropagator, errorReporter, javaResolverCache,
        javaPropertyInitializerEkonstuator, samConversionResolver, sourceElementFactory,
        moduleClassResolver, packagePartProvider, supertypeLoopChecker, lookupTracker, module, reflectionTypes,
        annotationTypeQualifierResolver, signatureEnhancement, javaClassesTracker, settings,
        kotlinTypeChecker, javaTypeEnhancementState, javaModuleResolver
    )
}

interface JavaResolverSettings {
    konst correctNullabilityForNotNullTypeParameter: Boolean
    konst typeEnhancementImprovementsInStrictMode: Boolean
    konst ignoreNullabilityForErasedValueParameters: Boolean
    konst enhancePrimitiveArrays: Boolean

    object Default : JavaResolverSettings {
        override konst correctNullabilityForNotNullTypeParameter: Boolean
            get() = false

        override konst typeEnhancementImprovementsInStrictMode: Boolean
            get() = false

        override konst ignoreNullabilityForErasedValueParameters: Boolean
            get() = false

        override konst enhancePrimitiveArrays: Boolean
            get() = false
    }

    companion object {
        fun create(
            correctNullabilityForNotNullTypeParameter: Boolean,
            typeEnhancementImprovementsInStrictMode: Boolean,
            ignoreNullabilityForErasedValueParameters: Boolean,
            enhancePrimitiveArrays: Boolean,
        ): JavaResolverSettings =
            object : JavaResolverSettings {
                override konst correctNullabilityForNotNullTypeParameter get() = correctNullabilityForNotNullTypeParameter
                override konst typeEnhancementImprovementsInStrictMode get() = typeEnhancementImprovementsInStrictMode
                override konst ignoreNullabilityForErasedValueParameters get() = ignoreNullabilityForErasedValueParameters
                override konst enhancePrimitiveArrays get() = enhancePrimitiveArrays
            }
    }
}

class LazyJavaResolverContext internal constructor(
    konst components: JavaResolverComponents,
    konst typeParameterResolver: TypeParameterResolver,
    internal konst delegateForDefaultTypeQualifiers: Lazy<JavaTypeQualifiersByElementType?>
) {
    constructor(
        components: JavaResolverComponents,
        typeParameterResolver: TypeParameterResolver,
        typeQualifiersComputation: () -> JavaTypeQualifiersByElementType?
    ) : this(components, typeParameterResolver, lazy(LazyThreadSafetyMode.NONE, typeQualifiersComputation))

    konst defaultTypeQualifiers: JavaTypeQualifiersByElementType? by delegateForDefaultTypeQualifiers

    konst typeResolver = JavaTypeResolver(this, typeParameterResolver)

    konst storageManager: StorageManager
        get() = components.storageManager

    konst module: ModuleDescriptor get() = components.module
}

fun LazyJavaResolverContext.child(
    typeParameterResolver: TypeParameterResolver
) = LazyJavaResolverContext(components, typeParameterResolver, delegateForDefaultTypeQualifiers)

fun LazyJavaResolverContext.computeNewDefaultTypeQualifiers(
    additionalAnnotations: Annotations
): JavaTypeQualifiersByElementType? =
    components.annotationTypeQualifierResolver.extractAndMergeDefaultQualifiers(defaultTypeQualifiers, additionalAnnotations)

fun LazyJavaResolverContext.replaceComponents(
    components: JavaResolverComponents
) = LazyJavaResolverContext(components, typeParameterResolver, delegateForDefaultTypeQualifiers)

private fun LazyJavaResolverContext.child(
    containingDeclaration: DeclarationDescriptor,
    typeParameterOwner: JavaTypeParameterListOwner?,
    typeParametersIndexOffset: Int = 0,
    delegateForTypeQualifiers: Lazy<JavaTypeQualifiersByElementType?>
) = LazyJavaResolverContext(
    components,
    typeParameterOwner?.let {
        LazyJavaTypeParameterResolver(this, containingDeclaration, it, typeParametersIndexOffset) }
        ?: typeParameterResolver,
    delegateForTypeQualifiers
)

fun LazyJavaResolverContext.childForMethod(
    containingDeclaration: DeclarationDescriptor,
    typeParameterOwner: JavaTypeParameterListOwner,
    typeParametersIndexOffset: Int = 0
) = child(containingDeclaration, typeParameterOwner, typeParametersIndexOffset, delegateForDefaultTypeQualifiers)

fun LazyJavaResolverContext.childForClassOrPackage(
    containingDeclaration: ClassOrPackageFragmentDescriptor,
    typeParameterOwner: JavaTypeParameterListOwner? = null,
    typeParametersIndexOffset: Int = 0
) = child(
    containingDeclaration, typeParameterOwner, typeParametersIndexOffset,
    lazy(LazyThreadSafetyMode.NONE) { computeNewDefaultTypeQualifiers(containingDeclaration.annotations) }
)

fun LazyJavaResolverContext.copyWithNewDefaultTypeQualifiers(
    additionalAnnotations: Annotations
) = when {
    additionalAnnotations.isEmpty() -> this
    else -> LazyJavaResolverContext(
        components, typeParameterResolver,
        lazy(LazyThreadSafetyMode.NONE) { computeNewDefaultTypeQualifiers(additionalAnnotations) }
    )
}
