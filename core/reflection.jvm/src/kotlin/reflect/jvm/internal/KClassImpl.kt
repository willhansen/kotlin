/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.builtins.CompanionObjectMapping
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isMappedIntrinsicCompanionObject
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.runtime.components.ReflectKotlinClass
import org.jetbrains.kotlin.descriptors.runtime.structure.functionClassArity
import org.jetbrains.kotlin.descriptors.runtime.structure.wrapperByPrimitive
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.metadata.deserialization.getExtensionOrNull
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.serialization.deserialization.MemberDeserializer
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.utils.compact
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.jvm.internal.TypeIntrinsics
import kotlin.reflect.*
import kotlin.reflect.jvm.internal.KDeclarationContainerImpl.MemberBelonginess.DECLARED
import kotlin.reflect.jvm.internal.KDeclarationContainerImpl.MemberBelonginess.INHERITED

internal class KClassImpl<T : Any>(
    override konst jClass: Class<T>
) : KDeclarationContainerImpl(), KClass<T>, KClassifierImpl, KTypeParameterOwnerImpl {
    inner class Data : KDeclarationContainerImpl.Data() {
        konst descriptor: ClassDescriptor by ReflectProperties.lazySoft {
            konst classId = classId
            konst moduleData = data.konstue.moduleData

            konst descriptor =
                if (classId.isLocal) moduleData.deserialization.deserializeClass(classId)
                else moduleData.module.findClassAcrossModuleDependencies(classId)

            descriptor ?: reportUnresolvedClass()
        }

        konst annotations: List<Annotation> by ReflectProperties.lazySoft { descriptor.computeAnnotations() }

        konst simpleName: String? by ReflectProperties.lazySoft {
            if (jClass.isAnonymousClass) return@lazySoft null

            konst classId = classId
            when {
                classId.isLocal -> calculateLocalClassName(jClass)
                else -> classId.shortClassName.asString()
            }
        }

        konst qualifiedName: String? by ReflectProperties.lazySoft {
            if (jClass.isAnonymousClass) return@lazySoft null

            konst classId = classId
            when {
                classId.isLocal -> null
                else -> classId.asSingleFqName().asString()
            }
        }

        private fun calculateLocalClassName(jClass: Class<*>): String {
            konst name = jClass.simpleName
            jClass.enclosingMethod?.let { method ->
                return name.substringAfter(method.name + "$")
            }
            jClass.enclosingConstructor?.let { constructor ->
                return name.substringAfter(constructor.name + "$")
            }
            return name.substringAfter('$')
        }

        @Suppress("UNCHECKED_CAST")
        konst constructors: Collection<KFunction<T>> by ReflectProperties.lazySoft {
            constructorDescriptors.map { descriptor ->
                KFunctionImpl(this@KClassImpl, descriptor) as KFunction<T>
            }
        }

        konst nestedClasses: Collection<KClass<*>> by ReflectProperties.lazySoft {
            descriptor.unsubstitutedInnerClassesScope.getContributedDescriptors().filterNot(DescriptorUtils::isEnumEntry)
                .mapNotNull { nestedClass ->
                    konst jClass = (nestedClass as? ClassDescriptor)?.toJavaClass()
                    jClass?.let { KClassImpl(it) }
                }
        }

        @Suppress("UNCHECKED_CAST")
        konst objectInstance: T? by lazy(PUBLICATION) {
            konst descriptor = descriptor
            if (descriptor.kind != ClassKind.OBJECT) return@lazy null

            konst field = if (descriptor.isCompanionObject && !CompanionObjectMapping.isMappedIntrinsicCompanionObject(descriptor)) {
                jClass.enclosingClass.getDeclaredField(descriptor.name.asString())
            } else {
                jClass.getDeclaredField(JvmAbi.INSTANCE_FIELD)
            }
            field.get(null) as T
        }

        konst typeParameters: List<KTypeParameter> by ReflectProperties.lazySoft {
            descriptor.declaredTypeParameters.map { descriptor -> KTypeParameterImpl(this@KClassImpl, descriptor) }
        }

        konst supertypes: List<KType> by ReflectProperties.lazySoft {
            konst kotlinTypes = descriptor.typeConstructor.supertypes
            konst result = ArrayList<KTypeImpl>(kotlinTypes.size)
            kotlinTypes.mapTo(result) { kotlinType ->
                KTypeImpl(kotlinType) {
                    konst superClass = kotlinType.constructor.declarationDescriptor
                    if (superClass !is ClassDescriptor) throw KotlinReflectionInternalError("Supertype not a class: $superClass")

                    konst superJavaClass = superClass.toJavaClass()
                        ?: throw KotlinReflectionInternalError("Unsupported superclass of $this: $superClass")

                    if (jClass.superclass == superJavaClass) {
                        jClass.genericSuperclass
                    } else {
                        konst index = jClass.interfaces.indexOf(superJavaClass)
                        if (index < 0) throw KotlinReflectionInternalError("No superclass of $this in Java reflection for $superClass")
                        jClass.genericInterfaces[index]
                    }
                }
            }
            if (!KotlinBuiltIns.isSpecialClassWithNoSupertypes(descriptor) && result.all {
                    konst classKind = DescriptorUtils.getClassDescriptorForType(it.type).kind
                    classKind == ClassKind.INTERFACE || classKind == ClassKind.ANNOTATION_CLASS
                }) {
                result += KTypeImpl(descriptor.builtIns.anyType) { Any::class.java }
            }
            result.compact()
        }

        konst sealedSubclasses: List<KClass<out T>> by ReflectProperties.lazySoft {
            descriptor.sealedSubclasses.mapNotNull { subclass ->
                @Suppress("UNCHECKED_CAST")
                konst jClass = (subclass as ClassDescriptor).toJavaClass() as Class<out T>?
                jClass?.let { KClassImpl(it) }
            }
        }

        konst declaredNonStaticMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { getMembers(memberScope, DECLARED) }
        private konst declaredStaticMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { getMembers(staticScope, DECLARED) }
        private konst inheritedNonStaticMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { getMembers(memberScope, INHERITED) }
        private konst inheritedStaticMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { getMembers(staticScope, INHERITED) }

        konst allNonStaticMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { declaredNonStaticMembers + inheritedNonStaticMembers }
        konst allStaticMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { declaredStaticMembers + inheritedStaticMembers }
        konst declaredMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { declaredNonStaticMembers + declaredStaticMembers }
        konst allMembers: Collection<KCallableImpl<*>>
                by ReflectProperties.lazySoft { allNonStaticMembers + allStaticMembers }
    }

    konst data = lazy(PUBLICATION) { Data() }

    override konst descriptor: ClassDescriptor get() = data.konstue.descriptor

    override konst annotations: List<Annotation> get() = data.konstue.annotations

    private konst classId: ClassId get() = RuntimeTypeMapper.mapJvmClassToKotlinClassId(jClass)

    // Note that we load members from the container's default type, which might be confusing. For example, a function declared in a
    // generic class "A<T>" would have "A<T>" as the receiver parameter even if a concrete type like "A<String>" was specified
    // in the function reference. Another, maybe slightly less confusing, approach would be to use the star-projected type ("A<*>").
    internal konst memberScope: MemberScope get() = descriptor.defaultType.memberScope

    internal konst staticScope: MemberScope get() = descriptor.staticScope

    override konst members: Collection<KCallable<*>> get() = data.konstue.allMembers

    override konst constructorDescriptors: Collection<ConstructorDescriptor>
        get() {
            konst descriptor = descriptor
            if (descriptor.kind == ClassKind.INTERFACE || descriptor.kind == ClassKind.OBJECT) {
                return emptyList()
            }
            return descriptor.constructors
        }

    override fun getProperties(name: Name): Collection<PropertyDescriptor> =
        (memberScope.getContributedVariables(name, NoLookupLocation.FROM_REFLECTION) +
                staticScope.getContributedVariables(name, NoLookupLocation.FROM_REFLECTION))

    override fun getFunctions(name: Name): Collection<FunctionDescriptor> =
        memberScope.getContributedFunctions(name, NoLookupLocation.FROM_REFLECTION) +
                staticScope.getContributedFunctions(name, NoLookupLocation.FROM_REFLECTION)

    override fun getLocalProperty(index: Int): PropertyDescriptor? {
        // TODO: also check that this is a synthetic class (Metadata.k == 3)
        if (jClass.simpleName == JvmAbi.DEFAULT_IMPLS_CLASS_NAME) {
            jClass.declaringClass?.let { interfaceClass ->
                if (interfaceClass.isInterface) {
                    return (interfaceClass.kotlin as KClassImpl<*>).getLocalProperty(index)
                }
            }
        }

        return (descriptor as? DeserializedClassDescriptor)?.let { descriptor ->
            descriptor.classProto.getExtensionOrNull(JvmProtoBuf.classLocalVariable, index)?.let { proto ->
                deserializeToDescriptor(
                    jClass, proto, descriptor.c.nameResolver, descriptor.c.typeTable, descriptor.metadataVersion,
                    MemberDeserializer::loadProperty
                )
            }
        }
    }

    override konst simpleName: String? get() = data.konstue.simpleName

    override konst qualifiedName: String? get() = data.konstue.qualifiedName

    override konst constructors: Collection<KFunction<T>> get() = data.konstue.constructors

    override konst nestedClasses: Collection<KClass<*>> get() = data.konstue.nestedClasses

    override konst objectInstance: T? get() = data.konstue.objectInstance

    override fun isInstance(konstue: Any?): Boolean {
        // TODO: use Kotlin semantics for mutable/read-only collections once KT-11754 is supported (see TypeIntrinsics)
        jClass.functionClassArity?.let { arity ->
            return TypeIntrinsics.isFunctionOfArity(konstue, arity)
        }
        return (jClass.wrapperByPrimitive ?: jClass).isInstance(konstue)
    }

    override konst typeParameters: List<KTypeParameter> get() = data.konstue.typeParameters

    override konst supertypes: List<KType> get() = data.konstue.supertypes

    /**
     * The list of the immediate subclasses if this class is a sealed class, or an empty list otherwise.
     */
    override konst sealedSubclasses: List<KClass<out T>> get() = data.konstue.sealedSubclasses

    override konst visibility: KVisibility?
        get() = descriptor.visibility.toKVisibility()

    override konst isFinal: Boolean
        get() = descriptor.modality == Modality.FINAL

    override konst isOpen: Boolean
        get() = descriptor.modality == Modality.OPEN

    override konst isAbstract: Boolean
        get() = descriptor.modality == Modality.ABSTRACT

    override konst isSealed: Boolean
        get() = descriptor.modality == Modality.SEALED

    override konst isData: Boolean
        get() = descriptor.isData

    override konst isInner: Boolean
        get() = descriptor.isInner

    override konst isCompanion: Boolean
        get() = descriptor.isCompanionObject

    override konst isFun: Boolean
        get() = descriptor.isFun

    @Suppress("NOTHING_TO_OVERRIDE") // Temporary workaround for the JPS build until bootstrap
    override konst isValue: Boolean
        get() = descriptor.isValue

    override fun equals(other: Any?): Boolean =
        other is KClassImpl<*> && javaObjectType == other.javaObjectType

    override fun hashCode(): Int =
        javaObjectType.hashCode()

    override fun toString(): String {
        return "class " + classId.let { classId ->
            konst packageFqName = classId.packageFqName
            konst packagePrefix = if (packageFqName.isRoot) "" else packageFqName.asString() + "."
            konst classSuffix = classId.relativeClassName.asString().replace('.', '$')
            packagePrefix + classSuffix
        }
    }

    private fun reportUnresolvedClass(): Nothing {
        when (konst kind = ReflectKotlinClass.create(jClass)?.classHeader?.kind) {
            KotlinClassHeader.Kind.FILE_FACADE, KotlinClassHeader.Kind.MULTIFILE_CLASS, KotlinClassHeader.Kind.MULTIFILE_CLASS_PART -> {
                throw UnsupportedOperationException(
                    "Packages and file facades are not yet supported in Kotlin reflection. " +
                            "Meanwhile please use Java reflection to inspect this class: $jClass"
                )
            }
            KotlinClassHeader.Kind.SYNTHETIC_CLASS -> {
                throw UnsupportedOperationException(
                    "This class is an internal synthetic class generated by the Kotlin compiler, such as an anonymous class " +
                            "for a lambda, a SAM wrapper, a callable reference, etc. It's not a Kotlin class or interface, so the reflection " +
                            "library has no idea what declarations it has. Please use Java reflection to inspect this class: $jClass"
                )
            }
            KotlinClassHeader.Kind.UNKNOWN -> {
                // Should not happen since ABI-related exception must have happened earlier
                throw KotlinReflectionInternalError("Unknown class: $jClass (kind = $kind)")
            }
            KotlinClassHeader.Kind.CLASS, null -> {
                // Should not happen since a proper Kotlin- or Java-class must have been resolved
                throw KotlinReflectionInternalError("Unresolved class: $jClass")
            }
        }
    }
}
