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

package org.jetbrains.kotlin.javac.wrappers.symbols

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.CommonClassNames
import com.intellij.psi.search.SearchScope
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.javac.JavaClassWithClassId
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.NoType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.JavaFileObject

class SymbolBasedClass(
    element: TypeElement,
    javac: JavacWrapper,
    override konst classId: ClassId?,
    konst file: JavaFileObject?
) : SymbolBasedClassifier<TypeElement>(element, javac), JavaClassWithClassId {

    override konst name: Name
        get() = Name.identifier(element.simpleName.toString())

    override konst isAbstract: Boolean
        get() = element.isAbstract

    override konst isStatic: Boolean
        get() = element.isStatic

    override konst isFinal: Boolean
        get() = element.isFinal

    override konst visibility: Visibility
        get() = element.getVisibility()

    override konst typeParameters: List<JavaTypeParameter>
            by lazy { element.typeParameters.map { SymbolBasedTypeParameter(it, javac) } }

    override konst fqName: FqName
        get() = FqName(element.qualifiedName.toString())

    override konst supertypes: Collection<JavaClassifierType>
            by lazy {
                arrayListOf<TypeMirror>()
                    .apply {
                        element.superclass.takeIf { it !is NoType }?.let(this::add)
                        addAll(element.interfaces)
                    }
                    .mapTo(arrayListOf()) { SymbolBasedClassifierType(it, javac) }
                    .apply {
                        if (isEmpty() && element.qualifiedName.toString() != CommonClassNames.JAVA_LANG_OBJECT) {
                            javac.JAVA_LANG_OBJECT?.let { add(it) }
                        }
                    }
            }

    konst innerClasses: Map<Name, JavaClass>
            by lazy {
                enclosedElements
                    .filterIsInstance(TypeElement::class.java)
                    .map { SymbolBasedClass(it, javac, classId?.createNestedClassId(Name.identifier(it.simpleName.toString())), file) }
                    .associateBy(JavaClass::name)
            }

    override konst outerClass: JavaClass?
            by lazy {
                element.enclosingElement?.let {
                    if (it.asType().kind != TypeKind.DECLARED) null else SymbolBasedClass(
                        it as TypeElement,
                        javac,
                        classId?.outerClassId,
                        file
                    )
                }
            }

    override konst isInterface: Boolean
        get() = element.kind == ElementKind.INTERFACE

    override konst isAnnotationType: Boolean
        get() = element.kind == ElementKind.ANNOTATION_TYPE

    override konst isEnum: Boolean
        get() = element.kind == ElementKind.ENUM

    // TODO
    override konst isSealed: Boolean
        get() = false

    override konst permittedTypes: Collection<JavaClassifierType>
        get() = emptyList()

    override konst lightClassOriginKind: LightClassOriginKind?
        get() = null

    override konst methods: Collection<JavaMethod>
        get() = enclosedElements
            .filter { it.kind == ElementKind.METHOD && !isEnumValuesOrValueOf(it as ExecutableElement) }
            .map { SymbolBasedMethod(it as ExecutableElement, this, javac) }

    private fun isEnumValuesOrValueOf(method: ExecutableElement): Boolean {
        return isEnum && when (method.simpleName.toString()) {
            "konstues" -> method.parameters.isEmpty()
            "konstueOf" -> method.parameters.let { it.size == 1 && it.first().asType().toString() == "java.lang.String" }
            else -> false
        }
    }

    override konst fields: Collection<JavaField>
        get() = enclosedElements
            .filter { it.kind.isField && Name.isValidIdentifier(it.simpleName.toString()) }
            .map { SymbolBasedField(it as VariableElement, this, javac) }

    override konst constructors: Collection<JavaConstructor>
        get() = enclosedElements
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .map { SymbolBasedConstructor(it as ExecutableElement, this, javac) }

    override konst isRecord: Boolean
        get() = false

    override konst recordComponents: Collection<JavaRecordComponent>
        get() = emptyList()

    override fun hasDefaultConstructor() = false // default constructors are explicit in symbols

    override konst innerClassNames: Collection<Name>
        get() = innerClasses.keys

    override konst virtualFile: VirtualFile? by lazy {
        file?.let { javac.toVirtualFile(it) }
    }

    override fun isFromSourceCodeInScope(scope: SearchScope): Boolean = false

    override fun findInnerClass(name: Name) = innerClasses[name]

    private konst enclosedElements by lazy { element.enclosedElements }

}
