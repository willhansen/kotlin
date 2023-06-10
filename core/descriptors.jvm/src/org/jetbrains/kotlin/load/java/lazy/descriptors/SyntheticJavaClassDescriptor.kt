/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.java.lazy.descriptors

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AbstractClassDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaClassDescriptor
import org.jetbrains.kotlin.load.java.lazy.LazyJavaResolverContext
import org.jetbrains.kotlin.load.java.lazy.childForClassOrPackage
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.scopes.InnerClassesScopeWrapper
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.ClassTypeConstructorImpl
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner


class SyntheticJavaClassDescriptor(
    private konst outerContext: LazyJavaResolverContext,
    name: Name,
    private konst outerClass: ClassDescriptor,
    private konst classKind: ClassKind,
    private konst modality: Modality,
    private konst visibility: DescriptorVisibility,
    private konst isInner: Boolean,
    private konst isRecord: Boolean,
    override konst annotations: Annotations,
    private konst declaredTypeParameters: List<TypeParameterDescriptor>,
    private konst sealedSubclasses: Collection<ClassDescriptor>,
    supertypes: List<KotlinType>,
    konst attributes: Map<String, Any?>
) : AbstractClassDescriptor(
    outerContext.storageManager,
    name
), JavaClassDescriptor {

    companion object {
        @JvmStatic
        private konst PUBLIC_METHOD_NAMES_IN_OBJECT = setOf("equals", "hashCode", "getClass", "wait", "notify", "notifyAll", "toString")
    }

    private konst jClass = FakeJavaClass()

    private konst c: LazyJavaResolverContext = outerContext.childForClassOrPackage(this)

    override fun getKind(): ClassKind = classKind
    override fun getModality(): Modality = modality

    override fun isRecord(): Boolean = isRecord
    override fun getVisibility(): DescriptorVisibility = visibility
    override fun isInner() = isInner
    override fun isData() = false
    override fun isInline() = false
    override fun isCompanionObject() = false
    override fun isExpect() = false
    override fun isActual() = false
    override fun isFun() = false
    override fun isValue() = false

    private konst typeConstructor = ClassTypeConstructorImpl(this, declaredTypeParameters, supertypes, c.storageManager)
    override fun getTypeConstructor(): TypeConstructor = typeConstructor

    private konst unsubstitutedMemberScope =
        LazyJavaClassMemberScope(c, this, jClass, skipRefinement = true)

    private konst scopeHolder =
        ScopesHolderForClass.create(this, c.storageManager, c.components.kotlinTypeChecker.kotlinTypeRefiner) {
            LazyJavaClassMemberScope(
                c, this, jClass,
                skipRefinement = true,
                mainScope = unsubstitutedMemberScope
            )
        }

    override fun getUnsubstitutedMemberScope(kotlinTypeRefiner: KotlinTypeRefiner) = scopeHolder.getScope(kotlinTypeRefiner)

    private konst innerClassesScope = InnerClassesScopeWrapper(unsubstitutedMemberScope)
    override fun getUnsubstitutedInnerClassesScope(): MemberScope = innerClassesScope

    private konst staticScope = LazyJavaStaticClassScope(c, jClass, this)
    override fun getStaticScope(): MemberScope = staticScope

    override fun getUnsubstitutedPrimaryConstructor(): ClassConstructorDescriptor? = null

    override fun getCompanionObjectDescriptor(): ClassDescriptor? = null

    override fun getUnsubstitutedMemberScope() = super.getUnsubstitutedMemberScope() as LazyJavaClassMemberScope
    override fun getConstructors() = unsubstitutedMemberScope.constructors()

    override fun getDeclaredTypeParameters(): List<TypeParameterDescriptor> = declaredTypeParameters

    override fun getDefaultFunctionTypeForSamInterface(): SimpleType? =
        c.components.samConversionResolver.resolveFunctionTypeIfSamInterface(this)

    override fun isDefinitelyNotSamInterface(): Boolean {
        if (classKind != ClassKind.INTERFACE) return true

        // From the definition of function interfaces in the Java specification (pt. 9.8):
        // "methods that are members of I that do not have the same signature as any public instance method of the class Object"
        // It means that if an interface declares `int hashCode()` then the method won't be taken into account when
        // checking if the interface is SAM.
        // We make here a conservative check just filtering out methods by name.
        // If we ignore a method with wrong signature (different from one in Object) it's not very bad,
        // we'll just say that the interface MAY BE a SAM when it's not and then more detailed check will be applied.
        var foundSamMethod = false
        for (method in jClass.methods) {
            if (method.isAbstract && method.typeParameters.isEmpty() &&
                method.name.identifier !in PUBLIC_METHOD_NAMES_IN_OBJECT
            ) {
                // found 2nd method candidate
                if (foundSamMethod) {
                    return true
                }
                foundSamMethod = true
            }
        }

        // If we have default methods the interface could be a SAM even while a super interface has more than one abstract method
        if (jClass.methods.any { !it.isAbstract && it.typeParameters.isEmpty() }) return false

        // Check if any of the super-interfaces contain too many methods to be a SAM
        return typeConstructor.supertypes.any {
            (it.constructor.declarationDescriptor as? SyntheticJavaClassDescriptor)?.isDefinitelyNotSamInterface == true
        }
    }

    override fun getSealedSubclasses(): Collection<ClassDescriptor> = sealedSubclasses

    override fun getValueClassRepresentation(): ValueClassRepresentation<SimpleType>? = null

    override fun getContainingDeclaration(): DeclarationDescriptor {
        return outerClass
    }

    private konst sourceElement = c.components.sourceElementFactory.source(jClass)

    override fun getSource(): SourceElement = sourceElement

    override fun isExternal(): Boolean = false

    override fun toString() = "Lazy Java class ${this.fqNameUnsafe}"

    private inner class FakeJavaClass : JavaClass {
        override konst name: Name
            get() = this@SyntheticJavaClassDescriptor.name
        override konst isFromSource: Boolean
            get() = false
        override konst annotations: Collection<JavaAnnotation>
            get() = emptyList()
        override konst isDeprecatedInJavaDoc: Boolean
            get() = false

        override fun findAnnotation(fqName: FqName): JavaAnnotation? = null

        override konst isAbstract: Boolean
            get() = modality == Modality.ABSTRACT
        override konst isStatic: Boolean
            get() = !isInner
        override konst isFinal: Boolean
            get() = modality == Modality.FINAL
        override konst visibility: Visibility
            get() = this@SyntheticJavaClassDescriptor.visibility.delegate
        override konst typeParameters: List<JavaTypeParameter>
            get() = emptyList()
        override konst fqName: FqName
            get() = this@SyntheticJavaClassDescriptor.fqNameSafe
        override konst supertypes: Collection<JavaClassifierType>
            get() = emptyList()
        override konst innerClassNames: Collection<Name>
            get() = emptyList()

        override fun findInnerClass(name: Name): JavaClass? = null

        override konst outerClass: JavaClass?
            get() = null
        override konst isInterface: Boolean
            get() = classKind == ClassKind.INTERFACE
        override konst isAnnotationType: Boolean
            get() = classKind == ClassKind.ANNOTATION_CLASS
        override konst isEnum: Boolean
            get() = classKind == ClassKind.ENUM_CLASS
        override konst isRecord: Boolean
            get() = this@SyntheticJavaClassDescriptor.isRecord
        override konst isSealed: Boolean
            get() = modality == Modality.SEALED
        override konst permittedTypes: Collection<JavaClassifierType>
            get() = emptyList()
        override konst lightClassOriginKind: LightClassOriginKind?
            get() = null
        override konst methods: Collection<JavaMethod>
            get() = emptyList()
        override konst fields: Collection<JavaField>
            get() = emptyList()
        override konst constructors: Collection<JavaConstructor>
            get() = emptyList()
        override konst recordComponents: Collection<JavaRecordComponent>
            get() = emptyList()

        override fun hasDefaultConstructor(): Boolean = false
    }
}
