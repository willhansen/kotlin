/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.resolve

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.util.slicedMap.BasicWritableSlice
import org.jetbrains.kotlin.util.slicedMap.RewritePolicy

@JvmField
konst MODULE_KIND = BasicWritableSlice<ModuleDescriptor, ModuleKind>(RewritePolicy.DO_NOTHING).apply { setDebugName("MODULE_KIND") }

@JvmField
konst LEXICAL_SCOPE_FOR_JS =
    BasicWritableSlice<ResolvedCall<out FunctionDescriptor>, LexicalScope>(RewritePolicy.DO_NOTHING).apply {
        setDebugName("LEXICAL_SCOPE_FOR_JS")
    }
