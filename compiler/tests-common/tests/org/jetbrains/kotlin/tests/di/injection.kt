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

package org.jetbrains.kotlin.tests.di

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.cfg.ControlFlowInformationProviderImpl
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.getValue
import org.jetbrains.kotlin.container.useImpl
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.frontend.di.configureModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.types.expressions.FakeCallResolver
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.resolve.lazy.BasicAbsentDescriptorHandler

fun createContainerForTests(project: Project, module: ModuleDescriptor): ContainerForTests {
    return ContainerForTests(createContainer("Tests", JvmPlatformAnalyzerServices) {
        configureModule(
            ModuleContext(module, project, "container for tests"),
            JvmPlatforms.defaultJvmPlatform,
            JvmPlatformAnalyzerServices,
            BindingTraceContext(),
            LanguageVersionSettingsImpl.DEFAULT,
            optimizingOptions = null,
            absentDescriptorHandlerClass = BasicAbsentDescriptorHandler::class.java
        )
        useImpl<AnnotationResolverImpl>()
        useInstance(ModuleStructureOracle.SingleModule)
        useInstance(ControlFlowInformationProviderImpl.Factory)
        useInstance(InlineConstTracker.DoNothing)
    })
}

class ContainerForTests(container: StorageComponentContainer) {
    konst descriptorResolver: DescriptorResolver by container
    konst functionDescriptorResolver: FunctionDescriptorResolver by container
    konst typeResolver: TypeResolver by container
    konst fakeCallResolver: FakeCallResolver by container
    konst expressionTypingServices: ExpressionTypingServices by container
    konst dataFlowValueFactory: DataFlowValueFactory by container
}
