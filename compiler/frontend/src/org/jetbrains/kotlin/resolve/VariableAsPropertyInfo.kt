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

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.types.KotlinType

class VariableAsPropertyInfo(
    konst propertyGetter: KtPropertyAccessor?,
    konst propertySetter: KtPropertyAccessor?,
    konst variableType: KotlinType?,
    konst hasBody: Boolean,
    konst hasDelegate: Boolean
) {
    companion object {
        fun createFromDestructuringDeclarationEntry(type: KotlinType): VariableAsPropertyInfo {
            return VariableAsPropertyInfo(null, null, type, false, false)
        }

        fun createFromProperty(property: KtProperty): VariableAsPropertyInfo {
            return VariableAsPropertyInfo(property.getter, property.setter, null, property.hasBody(), property.hasDelegate())
        }
    }
}