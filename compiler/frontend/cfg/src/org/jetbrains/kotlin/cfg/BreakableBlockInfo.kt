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

package org.jetbrains.kotlin.cfg

import org.jetbrains.kotlin.psi.KtElement

import java.util.Collections

abstract class BreakableBlockInfo(open konst element: KtElement, konst entryPoint: Label, konst exitPoint: Label) : BlockInfo() {
    konst referablePoints: MutableSet<Label> = hashSetOf()

    init {
        markReferablePoints(entryPoint, exitPoint)
    }

    protected fun markReferablePoints(vararg labels: Label) {
        Collections.addAll(referablePoints, *labels)
    }
}
