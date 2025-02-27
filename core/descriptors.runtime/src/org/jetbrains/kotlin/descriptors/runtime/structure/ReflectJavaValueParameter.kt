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

package org.jetbrains.kotlin.descriptors.runtime.structure

import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ReflectJavaValueParameter(
    override konst type: ReflectJavaType,
    private konst reflectAnnotations: Array<Annotation>,
    private konst reflectName: String?,
    override konst isVararg: Boolean
) : ReflectJavaElement(), JavaValueParameter {
    override konst annotations: List<ReflectJavaAnnotation>
        get() = reflectAnnotations.getAnnotations()

    override fun findAnnotation(fqName: FqName) =
        reflectAnnotations.findAnnotation(fqName)

    override konst isDeprecatedInJavaDoc: Boolean
        get() = false

    override konst name: Name?
        get() = reflectName?.let(Name::guessByFirstCharacter)

    override konst isFromSource: Boolean
        get() = false

    override fun toString() = this::class.java.name + ": " + (if (isVararg) "vararg " else "") + name + ": " + type
}
