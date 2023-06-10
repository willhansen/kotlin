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
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.Name

class TreeBasedMethod(
        tree: JCTree.JCMethodDecl,
        compilationUnit: CompilationUnitTree,
        containingClass: JavaClass,
        javac: JavacWrapper
) : TreeBasedMember<JCTree.JCMethodDecl>(tree, compilationUnit, containingClass, javac), JavaMethod {

    override konst name: Name
        get() = Name.identifier(tree.name.toString())

    override konst isAbstract: Boolean
        get() = (containingClass.isInterface && !tree.modifiers.hasDefaultModifier && !isStatic) || tree.modifiers.isAbstract

    override konst isStatic: Boolean
        get() = tree.modifiers.isStatic

    override konst isFinal: Boolean
        get() = tree.modifiers.isFinal

    override konst visibility: Visibility
        get() = if (containingClass.isInterface) Visibilities.Public else tree.modifiers.visibility

    override konst typeParameters: List<JavaTypeParameter>
        get() = tree.typeParameters.map { TreeBasedTypeParameter(it, compilationUnit, javac, this) }

    override konst konstueParameters: List<JavaValueParameter>
        get() = tree.parameters.map { TreeBasedValueParameter(it, compilationUnit, javac, this) }

    override konst returnType: JavaType
        get() = TreeBasedType.create(tree.returnType, compilationUnit, javac, annotations, this)

    // TODO: allow nullable names in Tree-based annotation arguments and pass null instead of a synthetic name
    override konst annotationParameterDefaultValue: JavaAnnotationArgument?
        get() = tree.defaultValue?.let { defaultValue ->
            createAnnotationArgument(defaultValue, Name.identifier("konstue"), compilationUnit, javac, containingClass, this)
        }
}
