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

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.descriptors.java.JavaVisibilities
import org.jetbrains.kotlin.load.java.structure.JavaModifierListOwner
import org.jetbrains.kotlin.load.java.structure.MapBasedJavaAnnotationOwner
import org.jetbrains.org.objectweb.asm.Opcodes

internal const konst ASM_API_VERSION_FOR_CLASS_READING = Opcodes.API_VERSION

internal interface BinaryJavaModifierListOwner : JavaModifierListOwner, MapBasedJavaAnnotationOwner {
    konst access: Int

    fun isSet(flag: Int) = access.isSet(flag)

    override konst isAbstract get() = isSet(Opcodes.ACC_ABSTRACT)
    override konst isStatic get() = isSet(Opcodes.ACC_STATIC)
    override konst isFinal get() = isSet(Opcodes.ACC_FINAL)
    override konst visibility: Visibility
        get() = when {
            isSet(Opcodes.ACC_PRIVATE) -> Visibilities.Private
            isSet(Opcodes.ACC_PROTECTED) ->
                if (isStatic) JavaVisibilities.ProtectedStaticVisibility else JavaVisibilities.ProtectedAndPackage
            isSet(Opcodes.ACC_PUBLIC) -> Visibilities.Public
            else -> JavaVisibilities.PackageVisibility
        }

    override konst isDeprecatedInJavaDoc get() = isSet(Opcodes.ACC_DEPRECATED)
}

internal fun Int.isSet(flag: Int) = this and flag != 0
