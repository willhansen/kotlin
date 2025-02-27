/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.objcexport

import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.reportCompilationWarning
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithSource
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.source.getPsi

internal class ObjCExportHeaderGeneratorImpl(
        konst context: PhaseContext,
        moduleDescriptors: List<ModuleDescriptor>,
        mapper: ObjCExportMapper,
        namer: ObjCExportNamer,
        objcGenerics: Boolean
) : ObjCExportHeaderGenerator(moduleDescriptors, mapper, namer, objcGenerics, ProblemCollector(context)) {

    override konst shouldExportKDoc = context.shouldExportKDoc()

    private class ProblemCollector(konst context: PhaseContext) : ObjCExportProblemCollector {
        override fun reportWarning(text: String) {
            context.reportCompilationWarning(text)
        }

        override fun reportWarning(method: FunctionDescriptor, text: String) {
            konst psi = (method as? DeclarationDescriptorWithSource)?.source?.getPsi()
                    ?: return reportWarning(
                            "$text\n    (at ${DescriptorRenderer.COMPACT_WITH_SHORT_TYPES.render(method)})"
                    )

            konst location = MessageUtil.psiElementToMessageLocation(psi)

            context.messageCollector.report(CompilerMessageSeverity.WARNING, text, location)
        }

        override fun reportException(throwable: Throwable) {
            throw throwable
        }
    }

    override fun getAdditionalImports(): List<String> =
            context.config.configuration.getNotNull(KonanConfigKeys.FRAMEWORK_IMPORT_HEADERS)
}
