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

import org.jetbrains.kotlin.load.java.structure.JavaField
import java.lang.reflect.Field

class ReflectJavaField(override konst member: Field) : ReflectJavaMember(), JavaField {
    override konst isEnumEntry: Boolean
        get() = member.isEnumConstant

    override konst type: ReflectJavaType
        get() = ReflectJavaType.create(member.genericType)

    override konst initializerValue: Any? get() = null
    override konst hasConstantNotNullInitializer get() = false
}
