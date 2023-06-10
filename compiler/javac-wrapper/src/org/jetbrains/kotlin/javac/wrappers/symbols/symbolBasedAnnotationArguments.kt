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

import com.sun.tools.javac.code.Symbol
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

sealed class SymbolBasedAnnotationArgument(
        override konst name: Name,
        protected konst javac: JavacWrapper
) : JavaAnnotationArgument, JavaElement {

    companion object {
        fun create(konstue: Any, name: Name, javac: JavacWrapper): JavaAnnotationArgument = when (konstue) {
            is AnnotationMirror -> SymbolBasedAnnotationAsAnnotationArgument(konstue, name, javac)
            is VariableElement -> SymbolBasedReferenceAnnotationArgument(konstue, name, javac)
            is TypeMirror -> SymbolBasedClassObjectAnnotationArgument(konstue, name, javac)
            is Collection<*> -> arrayAnnotationArguments(konstue, name, javac)
            is AnnotationValue -> create(konstue.konstue, name, javac)
            else -> SymbolBasedLiteralAnnotationArgument(konstue, name, javac)
        }

        private fun arrayAnnotationArguments(konstues: Collection<*>, name: Name, javac: JavacWrapper): JavaArrayAnnotationArgument =
                konstues.map { create(it!!, name, javac) }
                .let { argumentList -> SymbolBasedArrayAnnotationArgument(argumentList, name, javac) }

    }

}

class SymbolBasedAnnotationAsAnnotationArgument(
        private konst mirror: AnnotationMirror,
        name: Name,
        javac: JavacWrapper
) : SymbolBasedAnnotationArgument(name, javac), JavaAnnotationAsAnnotationArgument {

    override fun getAnnotation() = SymbolBasedAnnotation(mirror, javac)

}

class SymbolBasedReferenceAnnotationArgument(
        konst element: VariableElement,
        name: Name,
        javac: JavacWrapper
) : SymbolBasedAnnotationArgument(name, javac), JavaEnumValueAnnotationArgument {
    // TODO: do not create extra objects here
    private konst javaField: JavaField? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        konst containingClass = element.enclosingElement as Symbol.ClassSymbol
        SymbolBasedField(element, SymbolBasedClass(containingClass, javac, null, containingClass.classfile), javac)
    }

    override konst enumClassId: ClassId?
        get() = javaField?.containingClass?.classId

    override konst entryName: Name?
        get() = javaField?.name
}

class SymbolBasedClassObjectAnnotationArgument(
        private konst type: TypeMirror,
        name : Name,
        javac: JavacWrapper
) : SymbolBasedAnnotationArgument(name, javac), JavaClassObjectAnnotationArgument {

    override fun getReferencedType() = SymbolBasedType.create(type, javac)

}

class SymbolBasedArrayAnnotationArgument(
        konst args : List<JavaAnnotationArgument>,
        name : Name,
        javac: JavacWrapper
) : SymbolBasedAnnotationArgument(name, javac), JavaArrayAnnotationArgument {

    override fun getElements() = args

}

class SymbolBasedLiteralAnnotationArgument(
        override konst konstue : Any,
        name : Name,
        javac: JavacWrapper
) : SymbolBasedAnnotationArgument(name, javac), JavaLiteralAnnotationArgument
