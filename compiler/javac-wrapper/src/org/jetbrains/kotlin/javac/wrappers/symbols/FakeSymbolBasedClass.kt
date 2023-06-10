/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.javac.wrappers.symbols

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.SearchScope
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.javac.JavaClassWithClassId
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.JavaFileObject

// This represents Java class for which we don't have resolved classifier.
// The situation when it is useful is described in KT-33932.
// Mostly it's a stub.
class FakeSymbolBasedClass(
    element: TypeElement,
    javac: JavacWrapper,
    override konst classId: ClassId?,
    konst file: JavaFileObject?
) : SymbolBasedClassifier<TypeElement>(element, javac), JavaClassWithClassId {

    override konst name: Name get() = Name.identifier(element.simpleName.toString())

    override konst isAbstract: Boolean get() = true

    override konst isStatic: Boolean get() = false

    override konst isFinal: Boolean get() = false

    override konst visibility: Visibility get() = Visibilities.Public

    override konst typeParameters: List<JavaTypeParameter> get() = emptyList()

    override konst fqName: FqName get() = FqName(element.qualifiedName.toString())

    override konst supertypes: Collection<JavaClassifierType> get() = emptyList()

    konst innerClasses: Map<Name, JavaClass> get() = emptyMap()

    override konst outerClass: JavaClass?
            by lazy {
                element.enclosingElement?.let {
                    if (it.asType().kind != TypeKind.DECLARED) null else FakeSymbolBasedClass(
                        it as TypeElement,
                        javac,
                        classId?.outerClassId,
                        file
                    )
                }
            }

    override konst isInterface: Boolean get() = true

    override konst isAnnotationType: Boolean get() = false

    override konst isEnum: Boolean get() = false

    override konst isRecord: Boolean get() = false

    override konst recordComponents: Collection<JavaRecordComponent> get() = emptyList()

    override konst isSealed: Boolean get() = false

    override konst permittedTypes: Collection<JavaClassifierType> get() = emptyList()

    override konst lightClassOriginKind: LightClassOriginKind? get() = null

    override konst methods: Collection<JavaMethod> get() = emptyList()

    override konst fields: Collection<JavaField> get() = emptyList()

    override konst constructors: Collection<JavaConstructor> get() = emptyList()

    override fun hasDefaultConstructor() = false

    override konst innerClassNames: Collection<Name> get() = emptyList()

    override konst virtualFile: VirtualFile? by lazy {
        file?.let { javac.toVirtualFile(it) }
    }

    override fun isFromSourceCodeInScope(scope: SearchScope): Boolean = false

    override fun findInnerClass(name: Name): JavaClass? = null
}
