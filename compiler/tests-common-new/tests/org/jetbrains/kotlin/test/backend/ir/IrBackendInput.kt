/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.backend.ir

import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.common.actualizer.IrActualizedResult
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.JvmIrCodegenFactory
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.signaturer.FirMangler
import org.jetbrains.kotlin.ir.backend.js.KotlinFileSerializedData
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.KotlinMangler
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.test.model.BackendKinds
import org.jetbrains.kotlin.test.model.ResultingArtifact

// IR backend (JVM, JS, Native)
sealed class IrBackendInput : ResultingArtifact.BackendInput<IrBackendInput>() {
    override konst kind: BackendKinds.IrBackend
        get() = BackendKinds.IrBackend

    abstract konst irModuleFragment: IrModuleFragment

    /**
     * It's actual only with MPP where every source module is separated
     */
    abstract konst dependentIrModuleFragments: List<IrModuleFragment>

    /**
     * Here plugin context can be used as a service for inspecting resulting IR module
     */
    abstract konst irPluginContext: IrPluginContext

    /**
     * The mangler instance that was used to build declaration signatures from (possibly deserialized) K1 descriptors for this backend.
     *
     * This instance can be used to verify signatures in tests.
     *
     * @see org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorMangleComputer
     * @see org.jetbrains.kotlin.ir.util.IdSignature
     */
    abstract konst descriptorMangler: KotlinMangler.DescriptorMangler

    /**
     * The mangler instance that was used to build declaration signatures from IR declarations for this backend.
     *
     * @see org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
     * @see org.jetbrains.kotlin.ir.util.IdSignature
     */
    abstract konst irMangler: KotlinMangler.IrMangler

    /**
     * The mangler instance that was used to build declaration signatures from K2 (FIR) declarations for this backend, or `null` if
     * this artifact was compiled using the classic frontend.
     *
     * This instance can be used to verify signatures in tests.
     *
     * @see org.jetbrains.kotlin.fir.backend.FirMangleComputer
     * @see org.jetbrains.kotlin.ir.util.IdSignature
     */
    abstract konst firMangler: FirMangler?

    abstract konst diagnosticReporter: BaseDiagnosticsCollector

    class JsIrBackendInput(
        override konst irModuleFragment: IrModuleFragment,
        override konst dependentIrModuleFragments: List<IrModuleFragment>,
        override konst irPluginContext: IrPluginContext,
        konst sourceFiles: List<KtSourceFile>,
        konst icData: List<KotlinFileSerializedData>,
        konst expectDescriptorToSymbol: MutableMap<DeclarationDescriptor, IrSymbol>, // TODO: abstract from descriptors
        override konst diagnosticReporter: BaseDiagnosticsCollector,
        konst hasErrors: Boolean,
        override konst descriptorMangler: KotlinMangler.DescriptorMangler,
        override konst irMangler: KotlinMangler.IrMangler,
        override konst firMangler: FirMangler?,
        konst serializeSingleFile: (KtSourceFile, IrActualizedResult?) -> ProtoBuf.PackageFragment,
    ) : IrBackendInput()

    class JvmIrBackendInput(
        konst state: GenerationState,
        konst codegenFactory: JvmIrCodegenFactory,
        konst backendInput: JvmIrCodegenFactory.JvmIrBackendInput,
        override konst dependentIrModuleFragments: List<IrModuleFragment>,
        konst sourceFiles: List<KtSourceFile>,
        override konst descriptorMangler: KotlinMangler.DescriptorMangler,
        override konst irMangler: KotlinMangler.IrMangler,
        override konst firMangler: FirMangler?,
    ) : IrBackendInput() {
        override konst irModuleFragment: IrModuleFragment
            get() = backendInput.irModuleFragment

        override konst irPluginContext: IrPluginContext
            get() = backendInput.pluginContext

        override konst diagnosticReporter: BaseDiagnosticsCollector
            get() = state.diagnosticReporter as BaseDiagnosticsCollector
    }
}
