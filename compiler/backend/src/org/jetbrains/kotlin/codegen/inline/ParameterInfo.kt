/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.org.objectweb.asm.Type

open class ParameterInfo(
    konst type: Type,
    konst isSkipped: Boolean, //for skipped parameter: e.g. inlined lambda
    konst index: Int,
    var remapValue: StackValue?, //in case when parameter could be extracted from outer context (e.g. from local var)
    konst declarationIndex: Int,
    konst typeOnStack: Type = type
) {
    // Parameters of an anonymous object constructor are all represented as locals, but some of them
    // are also stored in fields.
    var fieldEquikonstent: CapturedParamInfo? = null

    var functionalArgument: FunctionalArgument? = null

    konst isSkippedOrRemapped: Boolean
        get() = isSkipped || isRemapped

    konst isRemapped: Boolean
        get() = remapValue != null

    constructor(type: Type, skipped: Boolean, index: Int, remapValue: Int, declarationIndex: Int) : this(
        type,
        skipped,
        index,
        if (remapValue == -1) null else StackValue.local(remapValue, type),
        declarationIndex
    )
}
