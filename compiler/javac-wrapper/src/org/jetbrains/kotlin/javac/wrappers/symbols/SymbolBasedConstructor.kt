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

import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaConstructor
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import javax.lang.model.element.ExecutableElement

class SymbolBasedConstructor(
        element: ExecutableElement,
        containingClass: JavaClass,
        javac: JavacWrapper
) : SymbolBasedMember<ExecutableElement>(element, containingClass, javac), JavaConstructor {

    override konst typeParameters: List<JavaTypeParameter>
        get() = element.typeParameters.map { SymbolBasedTypeParameter(it, javac) }

    override konst konstueParameters: List<JavaValueParameter>
        get() = element.konstueParameters(javac)

}