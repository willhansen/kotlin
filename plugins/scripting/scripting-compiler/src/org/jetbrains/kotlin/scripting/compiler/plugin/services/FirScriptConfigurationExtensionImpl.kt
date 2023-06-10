/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin.services

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.*
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.builder.FirScriptConfiguratorExtension
import org.jetbrains.kotlin.fir.builder.FirScriptConfiguratorExtension.Factory
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.builder.*
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.providers.dependenciesSymbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.builder.buildUserTypeRef
import org.jetbrains.kotlin.fir.types.impl.FirQualifierPartImpl
import org.jetbrains.kotlin.fir.types.impl.FirTypeArgumentListImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.scripting.definitions.annotationsForSamWithReceivers
import org.jetbrains.kotlin.scripting.resolve.KtFileScriptSource
import org.jetbrains.kotlin.scripting.resolve.VirtualFileScriptSource
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.StringScriptSource


class FirScriptConfiguratorExtensionImpl(
    session: FirSession,
    // TODO: left here because it seems it will be needed soon, remove supression if used or remove the param if it is not the case
    @Suppress("UNUSED_PARAMETER") hostConfiguration: ScriptingHostConfiguration,
) : FirScriptConfiguratorExtension(session) {

    @OptIn(SymbolInternals::class)
    override fun FirScriptBuilder.configure(fileBuilder: FirFileBuilder) {
        konst sourceFile = fileBuilder.sourceFile ?: return

        withConfigurationIfAny(sourceFile) { configuration ->
            // TODO: rewrite/extract decision logic for clarity
            configuration[ScriptCompilationConfiguration.baseClass]?.let { baseClass ->
                konst baseClassFqn = FqName.fromSegments(baseClass.typeName.split("."))
                contextReceivers.add(buildContextReceiverWithFqName(baseClassFqn))

                konst baseClassSymbol =
                    session.dependenciesSymbolProvider.getClassLikeSymbolByClassId(ClassId(baseClassFqn.parent(), baseClassFqn.shortName()))
                            as? FirRegularClassSymbol
                if (baseClassSymbol != null) {
                    // assuming that if base class will be unresolved, the error will be reported on the contextReceiver
                    baseClassSymbol.fir.primaryConstructorIfAny(session)?.fir?.konstueParameters?.forEach { baseCtorParameter ->
                        parameters.add(
                            buildProperty {
                                moduleData = session.moduleData
                                origin = FirDeclarationOrigin.ScriptCustomization
                                // TODO: copy type parameters?
                                returnTypeRef = baseCtorParameter.returnTypeRef
                                name = baseCtorParameter.name
                                symbol = FirPropertySymbol(name)
                                status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL)
                                isLocal = true
                                isVar = false
                            }
                        )
                    }
                }
            }
            configuration[ScriptCompilationConfiguration.implicitReceivers]?.forEach { implicitReceiver ->
                contextReceivers.add(buildContextReceiverWithFqName(FqName.fromSegments(implicitReceiver.typeName.split("."))))
            }
            configuration[ScriptCompilationConfiguration.providedProperties]?.forEach { propertyName, propertyType ->
                konst typeRef = buildUserTypeRef {
                    isMarkedNullable = propertyType.isNullable
                    propertyType.typeName.split(".").forEach {
                        qualifier.add(FirQualifierPartImpl(null, Name.identifier(it), FirTypeArgumentListImpl(null)))
                    }
                }
                parameters.add(
                    buildProperty {
                        moduleData = session.moduleData
                        origin = FirDeclarationOrigin.ScriptCustomization
                        returnTypeRef = typeRef
                        name = Name.identifier(propertyName)
                        symbol = FirPropertySymbol(name)
                        status = FirDeclarationStatusImpl(Visibilities.Local, Modality.FINAL)
                        isLocal = true
                        isVar = false
                    }
                )
            }
            configuration[ScriptCompilationConfiguration.annotationsForSamWithReceivers]?.forEach {
                _knownAnnotationsForSamWithReceiver.add(it.typeName)
            }

            configuration[ScriptCompilationConfiguration.defaultImports]?.forEach { defaultImport ->
                konst trimmed = defaultImport.trim()
                konst endsWithStar = trimmed.endsWith("*")
                konst stripped = if (endsWithStar) trimmed.substring(0, trimmed.length - 2) else trimmed
                konst fqName = FqName.fromSegments(stripped.split("."))
                fileBuilder.imports += buildImport {
                    fileBuilder.sourceFile?.project()?.let {
                        konst dummyElement = KtPsiFactory(it, markGenerated = true).createColon()
                        source = KtFakeSourceElement(dummyElement, KtFakeSourceElementKind.ImplicitImport)
                    }
                    importedFqName = fqName
                    isAllUnder = endsWithStar
                }
            }

            configuration[ScriptCompilationConfiguration.annotationsForSamWithReceivers]?.forEach {
                _knownAnnotationsForSamWithReceiver.add(it.typeName)
            }
        }
    }

    private fun withConfigurationIfAny(file: KtSourceFile, body: (ScriptCompilationConfiguration) -> Unit) {
        konst configuration = session.scriptDefinitionProviderService?.let { providerService ->
            konst sourceCode = file.toSourceCode()
            konst ktFile = sourceCode?.originalKtFile()
            with(providerService) {
                ktFile?.let { configurationFor(it) }
                    ?: sourceCode?.let { configurationFor(it) }
                    ?: defaultConfiguration()
            }
        }

        configuration?.let { body.invoke(it) }
    }

    private fun buildContextReceiverWithFqName(baseClassFqn: FqName) =
        buildContextReceiver {
            typeRef = buildUserTypeRef {
                isMarkedNullable = false
                qualifier.addAll(
                    baseClassFqn.pathSegments().map {
                        FirQualifierPartImpl(null, it, FirTypeArgumentListImpl(null))
                    }
                )
            }
        }

    private konst _knownAnnotationsForSamWithReceiver = hashSetOf<String>()

    internal konst knownAnnotationsForSamWithReceiver: Set<String>
        get() = _knownAnnotationsForSamWithReceiver

    companion object {
        fun getFactory(hostConfiguration: ScriptingHostConfiguration): Factory {
            return Factory { session -> FirScriptConfiguratorExtensionImpl(session, hostConfiguration) }
        }
    }
}

private fun KtSourceFile.project(): Project? = (toSourceCode() as? KtFileScriptSource)?.ktFile?.project

private fun SourceCode.originalKtFile(): KtFile =
    (this as? KtFileScriptSource)?.ktFile?.originalFile as? KtFile
        ?: error("only PSI scripts are supported at the moment")

private fun FirScriptDefinitionProviderService.configurationFor(file: KtFile): ScriptCompilationConfiguration? =
    configurationProvider?.getScriptConfigurationResult(file)?.konstueOrNull()?.configuration

private fun FirScriptDefinitionProviderService.configurationFor(sourceCode: SourceCode): ScriptCompilationConfiguration? =
    definitionProvider?.findDefinition(sourceCode)?.compilationConfiguration

private fun FirScriptDefinitionProviderService.defaultConfiguration(): ScriptCompilationConfiguration? =
    definitionProvider?.getDefaultDefinition()?.compilationConfiguration

fun KtSourceFile.toSourceCode(): SourceCode? = when (this) {
    is KtPsiSourceFile -> (psiFile as? KtFile)?.let(::KtFileScriptSource) ?: VirtualFileScriptSource(psiFile.virtualFile)
    is KtVirtualFileSourceFile -> VirtualFileScriptSource(virtualFile)
    is KtIoFileSourceFile -> FileScriptSource(file)
    is KtInMemoryTextSourceFile -> StringScriptSource(text.toString(), name)
    else -> null
}
