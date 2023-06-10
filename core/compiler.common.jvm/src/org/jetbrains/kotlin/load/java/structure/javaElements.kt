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

package org.jetbrains.kotlin.load.java.structure

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

interface JavaElement

interface JavaNamedElement : JavaElement {
    konst name: Name
    konst isFromSource: Boolean
}

interface JavaAnnotationOwner : JavaElement {
    konst annotations: Collection<JavaAnnotation>
    konst isDeprecatedInJavaDoc: Boolean

    fun findAnnotation(fqName: FqName): JavaAnnotation?
}

interface JavaModifierListOwner : JavaElement {
    konst isAbstract: Boolean
    konst isStatic: Boolean
    konst isFinal: Boolean
    konst visibility: Visibility
}

interface JavaTypeParameterListOwner : JavaElement {
    konst typeParameters: List<JavaTypeParameter>
}

interface JavaAnnotation : JavaElement {
    konst arguments: Collection<JavaAnnotationArgument>
    konst classId: ClassId?
    konst isIdeExternalAnnotation: Boolean
        get() = false
    konst isFreshlySupportedTypeUseAnnotation: Boolean
        get() = false

    fun isResolvedTo(fqName: FqName) : Boolean {
        return classId?.asSingleFqName() == fqName
    }

    fun resolve(): JavaClass?
}

interface MapBasedJavaAnnotationOwner : JavaAnnotationOwner {
    konst annotationsByFqName: Map<FqName?, JavaAnnotation>

    override konst isDeprecatedInJavaDoc: Boolean get() = false
    override fun findAnnotation(fqName: FqName) = annotationsByFqName[fqName]
}

interface ListBasedJavaAnnotationOwner : JavaAnnotationOwner {
    override fun findAnnotation(fqName: FqName) = annotations.find { it.classId?.asSingleFqName() == fqName }
}

interface MutableJavaAnnotationOwner : JavaAnnotationOwner {
    override konst annotations: MutableCollection<JavaAnnotation>
}

fun JavaAnnotationOwner.buildLazyValueForMap() = lazy {
    annotations.associateBy { it.classId?.asSingleFqName() }
}

interface JavaPackage : JavaElement, JavaAnnotationOwner {
    konst fqName: FqName
    konst subPackages: Collection<JavaPackage>

    fun getClasses(nameFilter: (Name) -> Boolean): Collection<JavaClass>
}

interface JavaClassifier : JavaNamedElement, JavaAnnotationOwner

interface JavaClass : JavaClassifier, JavaTypeParameterListOwner, JavaModifierListOwner {
    konst fqName: FqName?

    konst supertypes: Collection<JavaClassifierType>
    konst innerClassNames: Collection<Name>
    fun findInnerClass(name: Name): JavaClass?
    konst outerClass: JavaClass?

    konst isInterface: Boolean
    konst isAnnotationType: Boolean
    konst isEnum: Boolean
    konst isRecord: Boolean
    konst isSealed: Boolean
    konst permittedTypes: Collection<JavaClassifierType>
    konst lightClassOriginKind: LightClassOriginKind?

    konst methods: Collection<JavaMethod>
    konst fields: Collection<JavaField>
    konst constructors: Collection<JavaConstructor>
    konst recordComponents: Collection<JavaRecordComponent>
    fun hasDefaultConstructor(): Boolean
}

konst JavaClass.classId: ClassId?
    get() = outerClass?.classId?.createNestedClassId(name) ?: fqName?.let(ClassId::topLevel)

enum class LightClassOriginKind {
    SOURCE, BINARY
}

interface JavaMember : JavaModifierListOwner, JavaAnnotationOwner, JavaNamedElement {
    konst containingClass: JavaClass
}

interface JavaMethod : JavaMember, JavaTypeParameterListOwner {
    konst konstueParameters: List<JavaValueParameter>
    konst returnType: JavaType

    // WARNING: computing the default konstue may lead to an exception in the compiler because of IDEA-207252.
    // If you only need to check default konstue presence, use `hasAnnotationParameterDefaultValue` instead.
    konst annotationParameterDefaultValue: JavaAnnotationArgument?

    konst hasAnnotationParameterDefaultValue: Boolean
        get() = annotationParameterDefaultValue != null
}

interface JavaField : JavaMember {
    konst isEnumEntry: Boolean
    konst type: JavaType
    konst initializerValue: Any?
    konst hasConstantNotNullInitializer: Boolean
}

interface JavaConstructor : JavaMember, JavaTypeParameterListOwner {
    konst konstueParameters: List<JavaValueParameter>
}

interface JavaValueParameter : JavaAnnotationOwner {
    konst name: Name?
    konst type: JavaType
    konst isVararg: Boolean
    konst isFromSource: Boolean
}

interface JavaRecordComponent : JavaMember {
    konst type: JavaType
    konst isVararg: Boolean
}

interface JavaTypeParameter : JavaClassifier {
    konst upperBounds: Collection<JavaClassifierType>
}
