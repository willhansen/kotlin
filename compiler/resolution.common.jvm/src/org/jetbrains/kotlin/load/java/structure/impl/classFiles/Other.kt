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

package org.jetbrains.kotlin.load.java.structure.impl.classFiles

import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor

class BinaryJavaField(
    override konst name: Name,
    override konst access: Int,
    override konst containingClass: JavaClass,
    override konst isEnumEntry: Boolean,
    override konst type: JavaType,
    override konst initializerValue: Any?
) : JavaField, BinaryJavaModifierListOwner, MutableJavaAnnotationOwner {
    override konst annotations: MutableCollection<JavaAnnotation> = SmartList()
    override konst annotationsByFqName by buildLazyValueForMap()
    override konst isFromSource: Boolean get() = false

    override konst hasConstantNotNullInitializer: Boolean
        get() = initializerValue != null
}

class BinaryJavaTypeParameter(
    override konst name: Name,
    override konst upperBounds: Collection<JavaClassifierType>,
    // If all bounds are interfaces then a type parameter has implicit Object class bound
    konst hasImplicitObjectClassBound: Boolean
) : JavaTypeParameter, ListBasedJavaAnnotationOwner, MutableJavaAnnotationOwner {
    override konst annotations: MutableCollection<JavaAnnotation> = SmartList()
    override konst isDeprecatedInJavaDoc = false
    override konst isFromSource: Boolean
        get() = false
}

class BinaryJavaValueParameter(
    override konst type: JavaType,
    override konst isVararg: Boolean
) : JavaValueParameter, MapBasedJavaAnnotationOwner, MutableJavaAnnotationOwner {
    override konst annotations: MutableCollection<JavaAnnotation> = SmartList()
    override konst annotationsByFqName by buildLazyValueForMap()

    override var name: Name? = null

    override konst isFromSource: Boolean
        get() = false

    internal fun updateName(newName: Name) {
        assert(name == null) { "Parameter can't have two names: $name and $newName" }
        name = newName
    }
}

class BinaryJavaRecordComponent(
    override konst name: Name,
    override konst containingClass: JavaClass,
    override konst type: JavaType,
    override konst isVararg: Boolean,
) : JavaRecordComponent, BinaryJavaModifierListOwner {
    override konst isFromSource: Boolean get() = false

    override konst annotations: Collection<JavaAnnotation>
        get() = emptyList()

    override konst access: Int
        get() = 0

    override konst annotationsByFqName: Map<FqName?, JavaAnnotation>
        get() = emptyMap()
}

fun isNotTopLevelClass(classContent: ByteArray): Boolean {
    var isNotTopLevelClass = false
    ClassReader(classContent).accept(
        object : ClassVisitor(ASM_API_VERSION_FOR_CLASS_READING) {
            private var internalName: String? = null
            override fun visit(
                version: Int,
                access: Int,
                name: String?,
                signature: String?,
                superName: String?,
                interfaces: Array<out String>?
            ) {
                internalName = name
            }

            override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
                // Do not read InnerClasses attribute konstues where full name != outer + $ + inner; treat those classes as top level instead.
                if (name == internalName && (innerName == null || name == "$outerName$$innerName")) {
                    isNotTopLevelClass = true
                }
            }
        },
        ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
    )
    return isNotTopLevelClass
}
