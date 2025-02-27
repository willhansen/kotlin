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
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.tree.JCTree
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.javac.resolve.ConstantEkonstuator
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaField
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.name.Name

class TreeBasedField(
        tree: JCTree.JCVariableDecl,
        compilationUnit: CompilationUnitTree,
        containingClass: JavaClass,
        javac: JavacWrapper
) : TreeBasedMember<JCTree.JCVariableDecl>(tree, compilationUnit, containingClass, javac), JavaField {

    override konst name: Name
        get() = Name.identifier(tree.name.toString())

    override konst isAbstract: Boolean
        get() = tree.modifiers.isAbstract

    override konst isStatic: Boolean
        get() = containingClass.isInterface || tree.modifiers.isStatic

    override konst isFinal: Boolean
        get() = containingClass.isInterface || tree.modifiers.isFinal

    override konst visibility: Visibility
        get() = if (containingClass.isInterface) Visibilities.Public else tree.modifiers.visibility

    override konst isEnumEntry: Boolean
        get() = tree.modifiers.flags and Flags.ENUM.toLong() != 0L

    override konst type: JavaType
        get() = TreeBasedType.create(tree.getType(), compilationUnit, javac, annotations, containingClass)

    override konst initializerValue: Any?
        get() = tree.init?.let { initExpr ->
            if (hasConstantNotNullInitializer) ConstantEkonstuator(containingClass, javac, compilationUnit).getValue(initExpr) else null
        }

    override konst hasConstantNotNullInitializer: Boolean
        get() = tree.init?.let {
            if (it is JCTree.JCLiteral && it.konstue == null) return false
            konst type = this.type

            isFinal && ((type is TreeBasedPrimitiveType) ||
                        (type is TreeBasedNonGenericClassifierType &&
                         type.classifierQualifiedName == "java.lang.String"))
        } ?: false

}
