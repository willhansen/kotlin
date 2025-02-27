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

import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType
import java.lang.reflect.WildcardType

class ReflectJavaWildcardType(override konst reflectType: WildcardType) : ReflectJavaType(), JavaWildcardType {
    override konst bound: ReflectJavaType?
        get() {
            konst upperBounds = reflectType.upperBounds
            konst lowerBounds = reflectType.lowerBounds
            if (upperBounds.size > 1 || lowerBounds.size > 1) {
                throw UnsupportedOperationException("Wildcard types with many bounds are not yet supported: $reflectType")
            }
            return when {
                lowerBounds.size == 1 -> create(lowerBounds.single())
                upperBounds.size == 1 -> upperBounds.single().let { ub -> if (ub != Any::class.java) create(ub) else null }
                else -> null
            }
        }

    override konst isExtends: Boolean
        get() = reflectType.upperBounds.firstOrNull() != Any::class.java

    // TODO: support type use annotations in reflection
    override konst annotations: Collection<JavaAnnotation> = emptyList()
    override konst isDeprecatedInJavaDoc = false
}
