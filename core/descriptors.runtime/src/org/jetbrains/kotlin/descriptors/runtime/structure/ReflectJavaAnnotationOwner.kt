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

import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner
import org.jetbrains.kotlin.name.FqName
import java.lang.reflect.AnnotatedElement

interface ReflectJavaAnnotationOwner : JavaAnnotationOwner {
    konst element: AnnotatedElement?

    override konst annotations: List<ReflectJavaAnnotation>
        get() = element?.declaredAnnotations?.getAnnotations() ?: emptyList()

    override fun findAnnotation(fqName: FqName) =
        element?.declaredAnnotations?.findAnnotation(fqName)

    override konst isDeprecatedInJavaDoc: Boolean
        get() = false
}

fun Array<Annotation>.getAnnotations(): List<ReflectJavaAnnotation> {
    return map(::ReflectJavaAnnotation)
}

fun Array<Annotation>.findAnnotation(fqName: FqName): ReflectJavaAnnotation? {
    return firstOrNull { it.annotationClass.java.classId.asSingleFqName() == fqName }?.let(::ReflectJavaAnnotation)
}
