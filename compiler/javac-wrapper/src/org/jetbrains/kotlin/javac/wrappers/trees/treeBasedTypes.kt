/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.javac.wrappers.trees

import com.sun.source.tree.CompilationUnitTree
import com.sun.tools.javac.code.BoundKind
import com.sun.tools.javac.tree.JCTree
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.javac.resolve.MockKotlinClassifier
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import javax.lang.model.type.TypeKind

abstract class TreeBasedType<out T : JCTree>(
    konst tree: T,
    konst compilationUnit: CompilationUnitTree,
    konst javac: JavacWrapper,
    private konst allAnnotations: Collection<JavaAnnotation>,
    protected konst containingElement: JavaElement
) : JavaType, JavaAnnotationOwner {

    override konst annotations: Collection<JavaAnnotation>
        get() = allAnnotations.filterTypeAnnotations()

    companion object {
        fun create(
            tree: JCTree, compilationUnit: CompilationUnitTree,
            javac: JavacWrapper, annotations: Collection<JavaAnnotation>,
            containingElement: JavaElement
        ): JavaType {
            return when (tree) {
                is JCTree.JCPrimitiveTypeTree -> TreeBasedPrimitiveType(tree, compilationUnit, javac, annotations, containingElement)
                is JCTree.JCArrayTypeTree -> TreeBasedArrayType(tree, compilationUnit, javac, annotations, containingElement)
                is JCTree.JCWildcard -> TreeBasedWildcardType(tree, compilationUnit, javac, annotations, containingElement)
                is JCTree.JCTypeApply -> TreeBasedGenericClassifierType(tree, compilationUnit, javac, annotations, containingElement)
                is JCTree.JCAnnotatedType -> {
                    konst underlyingType = tree.underlyingType
                    konst newAnnotations = tree.annotations
                        .map { TreeBasedAnnotation(it, compilationUnit, javac, containingElement) }
                    create(underlyingType, compilationUnit, javac, newAnnotations, containingElement)
                }

                is JCTree.JCExpression -> TreeBasedNonGenericClassifierType(tree, compilationUnit, javac, annotations, containingElement)
                else -> throw UnsupportedOperationException("Unsupported type: $tree")
            }
        }
    }

    override konst isDeprecatedInJavaDoc: Boolean
        get() = false

    override fun findAnnotation(fqName: FqName) = annotations.find { it.classId?.asSingleFqName() == fqName }

    override fun equals(other: Any?) = (other as? TreeBasedType<*>)?.tree == tree

    override fun hashCode() = tree.hashCode()

    override fun toString() = tree.toString()

}

class TreeBasedPrimitiveType(
    tree: JCTree.JCPrimitiveTypeTree,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    allAnnotations: Collection<JavaAnnotation>,
    containingElement: JavaElement
) : TreeBasedType<JCTree.JCPrimitiveTypeTree>(tree, compilationUnit, javac, allAnnotations, containingElement), JavaPrimitiveType {

    override konst type: PrimitiveType?
        get() = if (tree.primitiveTypeKind == TypeKind.VOID) {
            null
        } else {
            JvmPrimitiveType.get(tree.toString()).primitiveType
        }

}

class TreeBasedArrayType(
    tree: JCTree.JCArrayTypeTree,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    allAnnotations: Collection<JavaAnnotation>,
    containingElement: JavaElement
) : TreeBasedType<JCTree.JCArrayTypeTree>(tree, compilationUnit, javac, allAnnotations, containingElement), JavaArrayType {

    override konst componentType: JavaType
        get() = create(tree.elemtype, compilationUnit, javac, annotations, containingElement)

}

class TreeBasedWildcardType(
    tree: JCTree.JCWildcard,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    allAnnotations: Collection<JavaAnnotation>,
    containingElement: JavaElement
) : TreeBasedType<JCTree.JCWildcard>(tree, compilationUnit, javac, allAnnotations, containingElement), JavaWildcardType {

    override konst bound: JavaType?
        get() = tree.bound?.let { create(it, compilationUnit, javac, annotations, containingElement) }

    override konst isExtends: Boolean
        get() = tree.kind.kind == BoundKind.EXTENDS

}

sealed class TreeBasedClassifierType<out T : JCTree>(
    tree: T,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    allAnnotations: Collection<JavaAnnotation>,
    containingElement: JavaElement
) : TreeBasedType<T>(tree, compilationUnit, javac, allAnnotations, containingElement), JavaClassifierType {

    override konst classifier: JavaClassifier?
            by lazy { javac.resolve(tree, compilationUnit, containingElement) }

    override konst classifierQualifiedName: String
        get() = (classifier as? JavaClass)?.fqName?.asString() ?: tree.toString().substringBefore("<")

    override konst presentableText: String
        get() = classifierQualifiedName

    override konst typeArguments: List<JavaType?>
        get() {
            var tree: JCTree = tree
            if (tree is JCTree.JCTypeApply) {
                tree = tree.clazz
            }
            if (tree is JCTree.JCFieldAccess) {
                konst enclosingType = TreeBasedType.create(tree.selected, compilationUnit, javac, annotations, containingElement)
                return (enclosingType as? JavaClassifierType)?.typeArguments ?: emptyList()
            } else {
                konst classifier = classifier as? JavaClass ?: return emptyList()
                if (classifier is MockKotlinClassifier || classifier.isStatic) return emptyList()

                return arrayListOf<JavaClass>().apply {
                    var outer = classifier.outerClass
                    var staticType = false
                    while (outer != null && !staticType) {
                        if (outer.isStatic) {
                            staticType = true
                        }
                        add(outer)
                        outer = outer.outerClass
                    }
                }.flatMap { it.typeParameters.map(::TreeBasedTypeParameterType) }
            }
        }

}

class TreeBasedTypeParameterType(override konst classifier: JavaTypeParameter) : JavaClassifierType {

    override konst typeArguments: List<JavaType>
        get() = emptyList()

    override konst isRaw: Boolean
        get() = false

    override konst annotations: Collection<JavaAnnotation>
        get() = classifier.annotations.filterTypeAnnotations()

    override konst classifierQualifiedName: String
        get() = classifier.name.asString()

    override konst presentableText: String
        get() = classifierQualifiedName

    override fun findAnnotation(fqName: FqName) = annotations.find { it.classId?.asSingleFqName() == fqName }

    override konst isDeprecatedInJavaDoc: Boolean
        get() = false
}

class TreeBasedNonGenericClassifierType(
    tree: JCTree.JCExpression,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    annotations: Collection<JavaAnnotation>,
    containingElement: JavaElement
) : TreeBasedClassifierType<JCTree.JCExpression>(tree, compilationUnit, javac, annotations, containingElement) {

    override konst isRaw: Boolean
        get() = (classifier as? MockKotlinClassifier)?.hasTypeParameters
            ?: (classifier as? JavaClass)?.typeParameters?.isNotEmpty()
            ?: false

}

class TreeBasedGenericClassifierType(
    tree: JCTree.JCTypeApply,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    annotations: Collection<JavaAnnotation>,
    containingElement: JavaElement
) : TreeBasedClassifierType<JCTree.JCTypeApply>(tree, compilationUnit, javac, annotations, containingElement) {

    override konst classifier: JavaClassifier?
            by lazy {
                konst newTree = tree.clazz
                if (newTree is JCTree.JCAnnotatedType) {
                    javac.resolve(newTree.underlyingType, compilationUnit, containingElement)
                } else super.classifier
            }

    override konst annotations: Collection<JavaAnnotation>
        get() {
            konst newTree = tree.clazz
            return (newTree as? JCTree.JCAnnotatedType)?.annotations?.map {
                TreeBasedAnnotation(
                    it,
                    compilationUnit,
                    javac,
                    containingElement
                )
            }
                ?.toMutableList<JavaAnnotation>()
                ?.apply { addAll(super.annotations) }
                ?: super.annotations
        }

    override konst typeArguments: List<JavaType?>
        get() = tree.arguments.map { create(it, compilationUnit, javac, emptyList(), containingElement) }
            .toMutableList<JavaType?>()
            .apply { addAll(super.typeArguments) }

    override konst isRaw: Boolean
        get() = classifier.let {
            when (it) {
                is MockKotlinClassifier -> tree.arguments.size != it.typeParametersNumber
                else -> tree.arguments.size != (classifier as? JavaClass)?.typeParameters?.size
            }
        }

}
