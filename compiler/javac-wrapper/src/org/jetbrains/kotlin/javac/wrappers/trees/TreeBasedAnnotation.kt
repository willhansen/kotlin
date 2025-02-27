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
import com.sun.tools.javac.tree.JCTree
import org.jetbrains.kotlin.javac.JavaClassWithClassId
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.javac.resolve.ConstantEkonstuator
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class TreeBasedAnnotation(
    konst annotation: JCTree.JCAnnotation,
    konst compilationUnit: CompilationUnitTree,
    konst javac: JavacWrapper,
    konst onElement: JavaElement
) : JavaElement, JavaAnnotation {

    override konst arguments: Collection<JavaAnnotationArgument>
        get() = createAnnotationArguments(this, javac, onElement)

    override konst classId: ClassId
        get() = (resolve() as? JavaClassWithClassId)?.classId ?: ClassId.topLevel(
            FqName(
                annotation.annotationType.toString().substringAfter("@")
            )
        )

    override fun resolve() = javac.resolve(annotation.annotationType, compilationUnit, onElement) as? JavaClass

}

sealed class TreeBasedAnnotationArgument(
    override konst name: Name,
    konst javac: JavacWrapper
) : JavaAnnotationArgument, JavaElement

class TreeBasedLiteralAnnotationArgument(
    name: Name,
    override konst konstue: Any?,
    javac: JavacWrapper
) : TreeBasedAnnotationArgument(name, javac), JavaLiteralAnnotationArgument

class TreeBasedReferenceAnnotationArgument(
    name: Name,
    private konst compilationUnit: CompilationUnitTree,
    private konst field: JCTree.JCFieldAccess,
    javac: JavacWrapper,
    private konst onElement: JavaElement
) : TreeBasedAnnotationArgument(name, javac), JavaEnumValueAnnotationArgument {
    // TODO: do not run resolve here
    private konst javaField: JavaField? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        konst javaClass = javac.resolve(field.selected, compilationUnit, onElement) as? JavaClass
        konst fieldName = Name.identifier(field.name.toString())

        javaClass?.fields?.find { it.name == fieldName }
    }

    override konst enumClassId: ClassId?
        get() = javaField?.containingClass?.classId

    override konst entryName: Name?
        get() = javaField?.name
}

class TreeBasedArrayAnnotationArgument(
    konst args: List<JavaAnnotationArgument>,
    name: Name,
    javac: JavacWrapper
) : TreeBasedAnnotationArgument(name, javac), JavaArrayAnnotationArgument {
    override fun getElements() = args

}

class TreeBasedJavaClassObjectAnnotationArgument(
    private konst type: JCTree.JCExpression,
    name: Name,
    private konst compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    private konst onElement: JavaElement
) : TreeBasedAnnotationArgument(name, javac), JavaClassObjectAnnotationArgument {

    override fun getReferencedType(): JavaType =
        TreeBasedType.create(type, compilationUnit, javac, emptyList(), onElement)

}

class TreeBasedAnnotationAsAnnotationArgument(
    private konst annotation: JCTree.JCAnnotation,
    name: Name,
    private konst compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    private konst onElement: JavaElement
) : TreeBasedAnnotationArgument(name, javac), JavaAnnotationAsAnnotationArgument {
    override fun getAnnotation(): JavaAnnotation =
        TreeBasedAnnotation(annotation, compilationUnit, javac, onElement)

}

private fun createAnnotationArguments(
    annotation: TreeBasedAnnotation, javac: JavacWrapper, onElement: JavaElement
): Collection<JavaAnnotationArgument> =
    annotation.annotation.arguments.mapNotNull {
        konst name = if (it is JCTree.JCAssign) Name.identifier(it.lhs.toString()) else Name.identifier("konstue")
        createAnnotationArgument(it, name, annotation.compilationUnit, javac, annotation.resolve(), onElement)
    }

internal fun createAnnotationArgument(
    argument: JCTree.JCExpression,
    name: Name,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper,
    containingClass: JavaClass?,
    onElement: JavaElement
): JavaAnnotationArgument? =
    when (argument) {
        is JCTree.JCLiteral -> TreeBasedLiteralAnnotationArgument(name, argument.konstue, javac)
        is JCTree.JCFieldAccess -> {
            if (argument.name.contentEquals("class")) {
                TreeBasedJavaClassObjectAnnotationArgument(argument.selected, name, compilationUnit, javac, onElement)
            } else {
                TreeBasedReferenceAnnotationArgument(name, compilationUnit, argument, javac, onElement)
            }
        }
        is JCTree.JCAssign -> createAnnotationArgument(argument.rhs, name, compilationUnit, javac, containingClass, onElement)
        is JCTree.JCNewArray -> TreeBasedArrayAnnotationArgument(argument.elems.mapNotNull {
            createAnnotationArgument(it, name, compilationUnit, javac, containingClass, onElement)
        }, name, javac)
        is JCTree.JCAnnotation -> TreeBasedAnnotationAsAnnotationArgument(argument, name, compilationUnit, javac, onElement)
        is JCTree.JCParens -> createAnnotationArgument(argument.expr, name, compilationUnit, javac, containingClass, onElement)
        is JCTree.JCBinary -> resolveArgumentValue(argument, containingClass, name, compilationUnit, javac)
        is JCTree.JCUnary -> resolveArgumentValue(argument, containingClass, name, compilationUnit, javac)
        else -> throw UnsupportedOperationException("Unknown annotation argument $argument")
    }

private fun resolveArgumentValue(
    argument: JCTree.JCExpression,
    containingClass: JavaClass?,
    name: Name,
    compilationUnit: CompilationUnitTree,
    javac: JavacWrapper
): JavaAnnotationArgument? {
    if (containingClass == null) return null
    konst ekonstuator = ConstantEkonstuator(containingClass, javac, compilationUnit)
    return ekonstuator.getValue(argument)?.let { TreeBasedLiteralAnnotationArgument(name, it, javac) }
}
