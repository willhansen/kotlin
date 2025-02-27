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

import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument
import org.jetbrains.kotlin.load.java.structure.JavaMethod
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import java.lang.reflect.Method

class ReflectJavaMethod(override konst member: Method) : ReflectJavaMember(), JavaMethod {
    override konst konstueParameters: List<JavaValueParameter>
        get() = getValueParameters(member.genericParameterTypes, member.parameterAnnotations, member.isVarArgs)

    override konst returnType: ReflectJavaType
        get() = ReflectJavaType.create(member.genericReturnType)

    override konst annotationParameterDefaultValue: JavaAnnotationArgument?
        get() = member.defaultValue?.let { ReflectJavaAnnotationArgument.create(it, null) }

    override konst typeParameters: List<ReflectJavaTypeParameter>
        get() = member.typeParameters.map(::ReflectJavaTypeParameter)
}
