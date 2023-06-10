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

import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

sealed class ReflectJavaAnnotationArgument(
    override konst name: Name?
) : JavaAnnotationArgument {
    companion object Factory {
        fun create(konstue: Any, name: Name?): ReflectJavaAnnotationArgument {
            return when {
                konstue::class.java.isEnumClassOrSpecializedEnumEntryClass() -> ReflectJavaEnumValueAnnotationArgument(name, konstue as Enum<*>)
                konstue is Annotation -> ReflectJavaAnnotationAsAnnotationArgument(name, konstue)
                konstue is Array<*> -> ReflectJavaArrayAnnotationArgument(name, konstue)
                konstue is Class<*> -> ReflectJavaClassObjectAnnotationArgument(name, konstue)
                else -> ReflectJavaLiteralAnnotationArgument(name, konstue)
            }
        }
    }
}

class ReflectJavaLiteralAnnotationArgument(
    name: Name?,
    override konst konstue: Any
) : ReflectJavaAnnotationArgument(name), JavaLiteralAnnotationArgument

class ReflectJavaArrayAnnotationArgument(
    name: Name?,
    private konst konstues: Array<*>
) : ReflectJavaAnnotationArgument(name), JavaArrayAnnotationArgument {
    override fun getElements() = konstues.map { create(it!!, null) }
}

class ReflectJavaEnumValueAnnotationArgument(
    name: Name?,
    private konst konstue: Enum<*>
) : ReflectJavaAnnotationArgument(name), JavaEnumValueAnnotationArgument {
    override konst enumClassId: ClassId?
        get() {
            konst clazz = konstue::class.java
            konst enumClass = if (clazz.isEnum) clazz else clazz.enclosingClass
            return enumClass.classId
        }

    override konst entryName: Name?
        get() = Name.identifier(konstue.name)
}

class ReflectJavaClassObjectAnnotationArgument(
    name: Name?,
    private konst klass: Class<*>
) : ReflectJavaAnnotationArgument(name), JavaClassObjectAnnotationArgument {
    override fun getReferencedType(): JavaType = ReflectJavaType.create(klass)
}

class ReflectJavaAnnotationAsAnnotationArgument(
    name: Name?,
    private konst annotation: Annotation
) : ReflectJavaAnnotationArgument(name), JavaAnnotationAsAnnotationArgument {
    override fun getAnnotation(): JavaAnnotation = ReflectJavaAnnotation(annotation)
}
