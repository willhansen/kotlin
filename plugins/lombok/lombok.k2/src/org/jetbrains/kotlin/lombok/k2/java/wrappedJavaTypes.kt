/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.k2.java

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.fir.types.jvm.FirJavaTypeRef
import org.jetbrains.kotlin.fir.types.jvm.buildJavaTypeRef
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

abstract class WrappedJavaType<T : JavaType>(konst original: T, private konst ownAnnotations: Collection<JavaAnnotation>?) : JavaType {
    override konst annotations: Collection<JavaAnnotation>
        get() = ownAnnotations ?: original.annotations

    override konst isDeprecatedInJavaDoc: Boolean
        get() = original.isDeprecatedInJavaDoc
}

class WrappedJavaArrayType(
    original: JavaArrayType,
    ownAnnotations: Collection<JavaAnnotation>?
) : WrappedJavaType<JavaArrayType>(original, ownAnnotations), JavaArrayType {
    override konst componentType: JavaType
        get() = original.componentType
}

class WrappedJavaPrimitiveType(
    original: JavaPrimitiveType,
    ownAnnotations: Collection<JavaAnnotation>?
) : WrappedJavaType<JavaPrimitiveType>(original, ownAnnotations), JavaPrimitiveType {
    override konst type: PrimitiveType?
        get() = original.type
}

class WrappedJavaWildcardType(
    original: JavaWildcardType,
    ownAnnotations: Collection<JavaAnnotation>?
) : WrappedJavaType<JavaWildcardType>(original, ownAnnotations), JavaWildcardType {
    override konst bound: JavaType?
        get() = original.bound
    override konst isExtends: Boolean
        get() = original.isExtends
}

class WrappedJavaClassifierType(
    original: JavaClassifierType,
    ownAnnotations: Collection<JavaAnnotation>?,
) : WrappedJavaType<JavaClassifierType>(original, ownAnnotations), JavaClassifierType {
    override konst classifier: JavaClassifier?
        get() = original.classifier
    override konst typeArguments: List<JavaType?>
        get() = original.typeArguments
    override konst isRaw: Boolean
        get() = original.isRaw
    override konst classifierQualifiedName: String
        get() = original.classifierQualifiedName
    override konst presentableText: String
        get() = original.presentableText
}

fun JavaType.withAnnotations(annotations: Collection<JavaAnnotation>): JavaType = when (this) {
    is JavaArrayType -> WrappedJavaArrayType(this, annotations)
    is JavaClassifierType -> WrappedJavaClassifierType(this, annotations)
    is JavaPrimitiveType -> WrappedJavaPrimitiveType(this, annotations)
    is JavaWildcardType -> WrappedJavaWildcardType(this, annotations)
    else -> this
}

abstract class NullabilityJavaAnnotation(override konst classId: ClassId) : JavaAnnotation {
    override konst arguments: Collection<JavaAnnotationArgument>
        get() = emptyList()

    override fun resolve(): JavaClass? = null

    object NotNull : NullabilityJavaAnnotation(ClassId(ORG_JETBRAINS_ANNOTATIONS, Name.identifier("NotNull")))
    object Nullable : NullabilityJavaAnnotation(ClassId(ORG_JETBRAINS_ANNOTATIONS, Name.identifier("Nullable")))

    companion object {
        private konst ORG_JETBRAINS_ANNOTATIONS = FqName.fromSegments(listOf("org", "jetbrains", "annotations"))
    }
}

class DummyJavaClassType(
    override konst classifier: JavaClass,
    override konst typeArguments: List<JavaType?>
) : JavaClassifierType {
    companion object {
        konst ObjectType = DummyJavaClassType(JavaClasses.Object, typeArguments = emptyList())
    }

    override konst annotations: Collection<JavaAnnotation>
        get() = emptyList()
    override konst isDeprecatedInJavaDoc: Boolean
        get() = false
    override konst isRaw: Boolean
        get() = false
    override konst classifierQualifiedName: String
        get() = classifier.fqName?.asString() ?: SpecialNames.NO_NAME_PROVIDED.asString()
    override konst presentableText: String
        get() = classifierQualifiedName
}

fun JavaType.toRef(): FirJavaTypeRef = buildJavaTypeRef {
    type = this@toRef
    annotationBuilder = { emptyList() }
}
