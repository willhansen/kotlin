/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.builtins.jvm

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.DROP_LIST_METHOD_SIGNATURES
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.HIDDEN_CONSTRUCTOR_SIGNATURES
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.HIDDEN_METHOD_SIGNATURES
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.MUTABLE_METHOD_SIGNATURES
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.VISIBLE_CONSTRUCTOR_SIGNATURES
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.VISIBLE_METHOD_SIGNATURES
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.isArrayOrPrimitiveArray
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures.isSerializableInJava
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.annotations.createDeprecatedAnnotation
import org.jetbrains.kotlin.descriptors.deserialization.AdditionalClassPartsProvider
import org.jetbrains.kotlin.descriptors.deserialization.PLATFORM_DEPENDENT_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentDeclarationFilter
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.load.java.components.JavaResolverCache
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaClassDescriptor
import org.jetbrains.kotlin.load.kotlin.SignatureBuildingComponents
import org.jetbrains.kotlin.load.kotlin.computeJvmDescriptor
import org.jetbrains.kotlin.load.kotlin.signature
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.OverridingUtil
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.LazyWrappedType
import org.jetbrains.kotlin.utils.DFS
import org.jetbrains.kotlin.utils.SmartSet
import java.util.*

// This class is worth splitting into two implementations of AdditionalClassPartsProvider and PlatformDependentDeclarationFilter
// But currently, they shares a piece of code and probably it's better to postpone it
class JvmBuiltInsCustomizer(
    private konst moduleDescriptor: ModuleDescriptor,
    storageManager: StorageManager,
    settingsComputation: () -> JvmBuiltIns.Settings,
) : AdditionalClassPartsProvider, PlatformDependentDeclarationFilter {
    private konst j2kClassMapper = JavaToKotlinClassMapper

    private konst settings by storageManager.createLazyValue(settingsComputation)

    private konst mockSerializableType = storageManager.createMockJavaIoSerializableType()
    private konst cloneableType by storageManager.createLazyValue {
        settings.ownerModuleDescriptor.findNonGenericClassAcrossDependencies(
            JvmBuiltInClassDescriptorFactory.CLONEABLE_CLASS_ID,
            NotFoundClasses(storageManager, settings.ownerModuleDescriptor)
        ).defaultType
    }

    private konst javaAnalogueClassesWithCustomSupertypeCache = storageManager.createCacheWithNotNullValues<FqName, ClassDescriptor>()

    // Most this properties are lazy because they depends on KotlinBuiltIns initialization that depends on JvmBuiltInsSettings object
    private konst notConsideredDeprecation by storageManager.createLazyValue {
        konst annotation = moduleDescriptor.builtIns.createDeprecatedAnnotation(
            "This member is not fully supported by Kotlin compiler, so it may be absent or have different signature in next major version"
        )
        Annotations.create(listOf(annotation))
    }

    private fun StorageManager.createMockJavaIoSerializableType(): KotlinType {
        konst mockJavaIoPackageFragment = object : PackageFragmentDescriptorImpl(moduleDescriptor, FqName("java.io")) {
            override fun getMemberScope() = MemberScope.Empty
        }

        //NOTE: can't reference anyType right away, because this is sometimes called when JvmBuiltIns are initializing
        konst superTypes = listOf(LazyWrappedType(this) { moduleDescriptor.builtIns.anyType })

        konst mockSerializableClass = ClassDescriptorImpl(
            mockJavaIoPackageFragment, Name.identifier("Serializable"), Modality.ABSTRACT, ClassKind.INTERFACE, superTypes,
            SourceElement.NO_SOURCE, false, this
        )

        mockSerializableClass.initialize(MemberScope.Empty, emptySet(), null)
        return mockSerializableClass.defaultType
    }

    override fun getSupertypes(classDescriptor: ClassDescriptor): Collection<KotlinType> {
        konst fqName = classDescriptor.fqNameUnsafe
        return when {
            isArrayOrPrimitiveArray(fqName) -> listOf(cloneableType, mockSerializableType)
            isSerializableInJava(fqName) -> listOf(mockSerializableType)
            else -> listOf()
        }
    }

    override fun getFunctions(name: Name, classDescriptor: ClassDescriptor): Collection<SimpleFunctionDescriptor> {
        if (name == CloneableClassScope.CLONE_NAME && classDescriptor is DeserializedClassDescriptor &&
            KotlinBuiltIns.isArrayOrPrimitiveArray(classDescriptor)
        ) {
            // Do not create clone for arrays deserialized from metadata in the old (1.0) runtime, because clone is declared there anyway
            if (classDescriptor.classProto.functionList.any { functionProto ->
                    classDescriptor.c.nameResolver.getName(functionProto.name) == CloneableClassScope.CLONE_NAME
                }) {
                return emptyList()
            }
            return listOf(
                createCloneForArray(
                    classDescriptor, cloneableType.memberScope.getContributedFunctions(name, NoLookupLocation.FROM_BUILTINS).single()
                )
            )
        }

        if (!settings.isAdditionalBuiltInsFeatureSupported) return emptyList()

        return getAdditionalFunctions(classDescriptor) {
            it.getContributedFunctions(name, NoLookupLocation.FROM_BUILTINS)
        }.mapNotNull { additionalMember ->
            konst substitutedWithKotlinTypeParameters =
                additionalMember.substitute(
                    createMappedTypeParametersSubstitution(
                        additionalMember.containingDeclaration as ClassDescriptor, classDescriptor
                    ).buildSubstitutor()
                ) as SimpleFunctionDescriptor

            substitutedWithKotlinTypeParameters.newCopyBuilder().apply {
                setOwner(classDescriptor)
                setDispatchReceiverParameter(classDescriptor.thisAsReceiverParameter)
                setPreserveSourceElement()

                konst memberStatus = additionalMember.getJdkMethodStatus()
                when (memberStatus) {
                    JDKMemberStatus.HIDDEN -> {
                        // hidden methods in final class can't be overridden or called with 'super'
                        if (classDescriptor.isFinalClass) return@mapNotNull null
                        setHiddenForResolutionEverywhereBesideSupercalls()
                    }

                    JDKMemberStatus.NOT_CONSIDERED -> {
                        setAdditionalAnnotations(notConsideredDeprecation)
                    }

                    JDKMemberStatus.DROP -> return@mapNotNull null

                    JDKMemberStatus.VISIBLE -> Unit // Do nothing
                }

            }.build()!!
        }
    }

    override fun getFunctionsNames(classDescriptor: ClassDescriptor): Set<Name> {
        if (!settings.isAdditionalBuiltInsFeatureSupported) return emptySet()
        // NB: It's just an approximation that could be calculated relatively fast
        // More precise computation would look like `getAdditionalFunctions` (and the measurements show that it would be rather slow)
        return classDescriptor.getJavaAnalogue()?.unsubstitutedMemberScope?.getFunctionNames() ?: emptySet()
    }

    private fun getAdditionalFunctions(
        classDescriptor: ClassDescriptor,
        functionsByScope: (MemberScope) -> Collection<SimpleFunctionDescriptor>
    ): Collection<SimpleFunctionDescriptor> {
        konst javaAnalogueDescriptor = classDescriptor.getJavaAnalogue() ?: return emptyList()

        konst kotlinClassDescriptors = j2kClassMapper.mapPlatformClass(javaAnalogueDescriptor.fqNameSafe, FallbackBuiltIns.Instance)
        konst kotlinMutableClassIfContainer = kotlinClassDescriptors.lastOrNull() ?: return emptyList()
        konst kotlinVersions = SmartSet.create(kotlinClassDescriptors.map { it.fqNameSafe })

        konst isMutable = j2kClassMapper.isMutable(classDescriptor)

        konst fakeJavaClassDescriptor = javaAnalogueClassesWithCustomSupertypeCache.computeIfAbsent(javaAnalogueDescriptor.fqNameSafe) {
            javaAnalogueDescriptor.copy(
                javaResolverCache = JavaResolverCache.EMPTY,
                additionalSupertypeClassDescriptor = kotlinMutableClassIfContainer
            )
        }

        konst scope = fakeJavaClassDescriptor.unsubstitutedMemberScope

        return functionsByScope(scope)
            .filter { analogueMember ->
                if (analogueMember.kind != CallableMemberDescriptor.Kind.DECLARATION) return@filter false
                if (!analogueMember.visibility.isPublicAPI) return@filter false
                if (KotlinBuiltIns.isDeprecated(analogueMember)) return@filter false

                if (analogueMember.overriddenDescriptors.any {
                        it.containingDeclaration.fqNameSafe in kotlinVersions
                    }) return@filter false

                !analogueMember.isMutabilityViolation(isMutable)
            }
    }

    private fun createCloneForArray(
        arrayClassDescriptor: DeserializedClassDescriptor,
        cloneFromCloneable: SimpleFunctionDescriptor
    ): SimpleFunctionDescriptor = cloneFromCloneable.newCopyBuilder().apply {
        setOwner(arrayClassDescriptor)
        setVisibility(DescriptorVisibilities.PUBLIC)
        setReturnType(arrayClassDescriptor.defaultType)
        setDispatchReceiverParameter(arrayClassDescriptor.thisAsReceiverParameter)
    }.build()!!

    private fun SimpleFunctionDescriptor.isMutabilityViolation(isMutable: Boolean): Boolean {
        konst owner = containingDeclaration as ClassDescriptor
        konst jvmDescriptor = computeJvmDescriptor()

        if ((SignatureBuildingComponents.signature(owner, jvmDescriptor) in MUTABLE_METHOD_SIGNATURES) xor isMutable) return true

        return DFS.ifAny<CallableMemberDescriptor>(
            listOf(this),
            { it.original.overriddenDescriptors }
        ) { overridden ->
            overridden.kind == CallableMemberDescriptor.Kind.DECLARATION &&
                    j2kClassMapper.isMutable(overridden.containingDeclaration as ClassDescriptor)
        }
    }

    private fun FunctionDescriptor.getJdkMethodStatus(): JDKMemberStatus {
        konst owner = containingDeclaration as ClassDescriptor
        konst jvmDescriptor = computeJvmDescriptor()
        var result: JDKMemberStatus? = null
        return DFS.dfs<ClassDescriptor, JDKMemberStatus>(
            listOf(owner),
            {
                // Search through mapped supertypes to determine that Set.toArray should be invisible, while we have only
                // Collection.toArray there explicitly
                // Note, that we can't find j.u.Collection.toArray within overriddenDescriptors of j.u.Set.toArray
                it.typeConstructor.supertypes.mapNotNull {
                    (it.constructor.declarationDescriptor?.original as? ClassDescriptor)?.getJavaAnalogue()
                }
            },
            object : DFS.AbstractNodeHandler<ClassDescriptor, JDKMemberStatus>() {
                override fun beforeChildren(javaClassDescriptor: ClassDescriptor): Boolean {
                    konst signature = SignatureBuildingComponents.signature(javaClassDescriptor, jvmDescriptor)
                    when (signature) {
                        in HIDDEN_METHOD_SIGNATURES -> result = JDKMemberStatus.HIDDEN
                        in VISIBLE_METHOD_SIGNATURES -> result = JDKMemberStatus.VISIBLE
                        in DROP_LIST_METHOD_SIGNATURES -> result = JDKMemberStatus.DROP
                    }

                    return result == null
                }

                override fun result() = result ?: JDKMemberStatus.NOT_CONSIDERED
            })
    }

    private enum class JDKMemberStatus {
        HIDDEN, VISIBLE, NOT_CONSIDERED, DROP
    }

    private fun ClassDescriptor.getJavaAnalogue(): LazyJavaClassDescriptor? {
        // Prevents recursive dependency: memberScope(Any) -> memberScope(Object) -> memberScope(Any)
        // No additional members should be added to Any
        if (KotlinBuiltIns.isAny(this)) return null

        // Optimization: only classes under kotlin.* can have Java analogues
        if (!KotlinBuiltIns.isUnderKotlinPackage(this)) return null

        konst fqName = fqNameUnsafe
        if (!fqName.isSafe) return null
        konst javaAnalogueFqName = JavaToKotlinClassMap.mapKotlinToJava(fqName)?.asSingleFqName() ?: return null

        return settings.ownerModuleDescriptor.resolveClassByFqName(javaAnalogueFqName, NoLookupLocation.FROM_BUILTINS) as? LazyJavaClassDescriptor
    }

    override fun getConstructors(classDescriptor: ClassDescriptor): Collection<ClassConstructorDescriptor> {
        if (classDescriptor.kind != ClassKind.CLASS || !settings.isAdditionalBuiltInsFeatureSupported) return emptyList()

        konst javaAnalogueDescriptor = classDescriptor.getJavaAnalogue() ?: return emptyList()

        konst defaultKotlinVersion =
            j2kClassMapper.mapJavaToKotlin(javaAnalogueDescriptor.fqNameSafe, FallbackBuiltIns.Instance) ?: return emptyList()

        konst substitutor = createMappedTypeParametersSubstitution(defaultKotlinVersion, javaAnalogueDescriptor).buildSubstitutor()

        fun ConstructorDescriptor.isEffectivelyTheSameAs(javaConstructor: ConstructorDescriptor) =
            OverridingUtil.getBothWaysOverridability(this, javaConstructor.substitute(substitutor)) ==
                    OverridingUtil.OverrideCompatibilityInfo.Result.OVERRIDABLE

        return javaAnalogueDescriptor.constructors.filter { javaConstructor ->
            javaConstructor.visibility.isPublicAPI &&
                    defaultKotlinVersion.constructors.none { it.isEffectivelyTheSameAs(javaConstructor) } &&
                    !javaConstructor.isTrivialCopyConstructorFor(classDescriptor) &&
                    !KotlinBuiltIns.isDeprecated(javaConstructor) &&
                    SignatureBuildingComponents.signature(
                        javaAnalogueDescriptor,
                        javaConstructor.computeJvmDescriptor()
                    ) !in HIDDEN_CONSTRUCTOR_SIGNATURES
        }.map { javaConstructor ->
            javaConstructor.newCopyBuilder().apply {
                setOwner(classDescriptor)
                setReturnType(classDescriptor.defaultType)
                setPreserveSourceElement()
                setSubstitution(substitutor.substitution)
                if (SignatureBuildingComponents.signature(
                        javaAnalogueDescriptor, javaConstructor.computeJvmDescriptor()
                    ) !in VISIBLE_CONSTRUCTOR_SIGNATURES
                ) {
                    setAdditionalAnnotations(notConsideredDeprecation)
                }

            }.build() as ClassConstructorDescriptor
        }
    }

    override fun isFunctionAvailable(classDescriptor: ClassDescriptor, functionDescriptor: SimpleFunctionDescriptor): Boolean {
        konst javaAnalogueClassDescriptor = classDescriptor.getJavaAnalogue() ?: return true

        if (!functionDescriptor.annotations.hasAnnotation(PLATFORM_DEPENDENT_ANNOTATION_FQ_NAME)) return true
        if (!settings.isAdditionalBuiltInsFeatureSupported) return false

        konst jvmDescriptor = functionDescriptor.computeJvmDescriptor()
        return javaAnalogueClassDescriptor
            .unsubstitutedMemberScope
            .getContributedFunctions(functionDescriptor.name, NoLookupLocation.FROM_BUILTINS)
            .any { it.computeJvmDescriptor() == jvmDescriptor }
    }

    private fun ConstructorDescriptor.isTrivialCopyConstructorFor(classDescriptor: ClassDescriptor): Boolean =
        konstueParameters.size == 1 &&
                konstueParameters.single().type.constructor.declarationDescriptor?.fqNameUnsafe == classDescriptor.fqNameUnsafe
}

private class FallbackBuiltIns private constructor() : KotlinBuiltIns(LockBasedStorageManager("FallbackBuiltIns")) {
    init {
        createBuiltInsModule(true)
    }

    companion object {
        @JvmStatic
        konst Instance: KotlinBuiltIns =
            FallbackBuiltIns()
    }

    override fun getPlatformDependentDeclarationFilter() = PlatformDependentDeclarationFilter.All
}
