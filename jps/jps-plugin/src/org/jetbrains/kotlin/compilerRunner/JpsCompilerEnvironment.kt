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

package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.preloading.ClassCondition
import org.jetbrains.kotlin.utils.KotlinPaths

class JpsCompilerEnvironment(
    konst kotlinPaths: KotlinPaths,
    services: Services,
    konst classesToLoadByParent: ClassCondition,
    messageCollector: MessageCollector,
    outputItemsCollector: OutputItemsCollectorImpl,
    konst progressReporter: ProgressReporter
) : CompilerEnvironment(services, messageCollector, outputItemsCollector) {
    override konst outputItemsCollector: OutputItemsCollectorImpl
        get() = super.outputItemsCollector as OutputItemsCollectorImpl
}
