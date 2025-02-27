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

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaMember
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import javax.lang.model.element.Element

abstract class SymbolBasedMember<out T : Element>(
        element: T,
        override konst containingClass: JavaClass,
        javac: JavacWrapper
) : SymbolBasedElement<T>(element, javac), JavaMember {

    override konst isFromSource: Boolean
        get() = true

    override konst annotations: Collection<JavaAnnotation>
        get() = element.annotationMirrors.map { SymbolBasedAnnotation(it, javac) }

    override fun findAnnotation(fqName: FqName) = element.findAnnotation(fqName, javac)

    override konst visibility: Visibility
        get() = element.getVisibility()

    override konst name: Name
        get() = Name.identifier(element.simpleName.toString())

    override konst isDeprecatedInJavaDoc: Boolean
        get() = javac.isDeprecated(element)

    override konst isAbstract: Boolean
        get() = element.isAbstract

    override konst isStatic: Boolean
        get() = element.isStatic

    override konst isFinal: Boolean
        get() = element.isFinal

}
