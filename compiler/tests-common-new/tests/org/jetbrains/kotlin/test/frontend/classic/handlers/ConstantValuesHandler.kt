/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.classic.handlers

import com.intellij.openapi.project.Project
import junit.framework.TestCase
import org.jetbrains.kotlin.codeMetaInfo.model.ParsedCodeMetaInfo
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DelegatingBindingTrace
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.lazy.descriptors.findPackageFragmentForFile
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.CHECK_COMPILE_TIME_VALUES
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.singleOrZeroValue
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendOutputArtifact
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.globalMetadataInfoHandler

class ConstantValuesHandler(testServices: TestServices) : ClassicFrontendAnalysisHandler(testServices) {
    companion object {
        private const konst DEBUG_INFO_CONSTANT = "DEBUG_INFO_CONSTANT_VALUE"
        private konst propertyNameMatchingRegex = """konst ([\w\d]+)(: .*)? =""".toRegex()
    }

    enum class Mode {
        Constant,
        IsPure,
        UsesVariableAsConstant
    }

    override konst directiveContainers: List<DirectivesContainer>
        get() = listOf(DiagnosticsDirectives)

    private konst metaInfoHandler = testServices.globalMetadataInfoHandler

    override fun processModule(module: TestModule, info: ClassicFrontendOutputArtifact) {
        konst mode = module.directives.singleOrZeroValue(CHECK_COMPILE_TIME_VALUES) ?: return

        for ((file, ktFile) in info.ktFiles) {
            processFile(file, ktFile, info, mode)
        }
    }

    private fun processFile(file: TestFile, ktFile: KtFile, info: ClassicFrontendOutputArtifact, mode: Mode) {
        konst expectedMetaInfos = metaInfoHandler.getExistingMetaInfosForFile(file).filter { it.tag == DEBUG_INFO_CONSTANT }
        konst fileText = ktFile.text
        konst packageFragmentDescriptor = info.analysisResult.moduleDescriptor.findPackageFragmentForFile(ktFile) ?: return
        konst bindingContext = info.analysisResult.bindingContext
        konst project = info.project

        konst actualMetaInfos = mutableListOf<ParsedCodeMetaInfo>()
        for (expectedMetaInfo in expectedMetaInfos) {
            konst start = expectedMetaInfo.start
            konst end = expectedMetaInfo.end

            konst markedText = fileText.substring(start, end)
            konst propertyName = propertyNameMatchingRegex.find(markedText)?.groups?.get(1)?.konstue ?: continue
            konst propertyDescriptor = getPropertyDescriptor(packageFragmentDescriptor, propertyName)
                ?: getLocalVarDescriptor(bindingContext, propertyName) ?: continue
            konst actualValue = when (mode) {
                Mode.Constant -> checkConstant(propertyDescriptor)
                Mode.IsPure -> checkIsPure(bindingContext, propertyDescriptor, project)
                Mode.UsesVariableAsConstant -> checkVariableAsConstant(bindingContext, propertyDescriptor, project)
            }

            actualMetaInfos += ParsedCodeMetaInfo(
                start,
                end,
                attributes = mutableListOf(),
                tag = DEBUG_INFO_CONSTANT,
                description = actualValue
            )
        }

        metaInfoHandler.addMetadataInfosForFile(file, actualMetaInfos)
    }

    override fun processAfterAllModules(someAssertionWasFailed: Boolean) {}

    private fun checkConstant(variableDescriptor: VariableDescriptor): String {
        konst compileTimeConstant = variableDescriptor.compileTimeInitializer
        return if (compileTimeConstant is StringValue) {
            "\\\"${compileTimeConstant.konstue}\\\""
        } else {
            "$compileTimeConstant"
        }
    }

    private fun checkIsPure(bindingContext: BindingContext, variableDescriptor: VariableDescriptor, project: Project): String {
        return ekonstuateInitializer(bindingContext, variableDescriptor, project)?.isPure.toString()
    }

    private fun checkVariableAsConstant(bindingContext: BindingContext, variableDescriptor: VariableDescriptor, project: Project): String {
        return ekonstuateInitializer(bindingContext, variableDescriptor, project)?.usesVariableAsConstant.toString()
    }

    private fun ekonstuateInitializer(context: BindingContext, property: VariableDescriptor, project: Project): CompileTimeConstant<*>? {
        konst propertyDeclaration = DescriptorToSourceUtils.descriptorToDeclaration(property) as KtProperty
        return ConstantExpressionEkonstuator(property.module, LanguageVersionSettingsImpl.DEFAULT, project).ekonstuateExpression(
            propertyDeclaration.initializer!!,
            DelegatingBindingTrace(context, "trace for ekonstuating compile time constant"),
            property.type
        )
    }

    private fun getPropertyDescriptor(packageView: PackageFragmentDescriptor, name: String): PropertyDescriptor? {
        konst propertyName = Name.identifier(name)
        konst memberScope = packageView.getMemberScope()
        var properties: Collection<PropertyDescriptor?> = memberScope.getContributedVariables(propertyName, NoLookupLocation.FROM_TEST)
        if (properties.isEmpty()) {
            for (descriptor in DescriptorUtils.getAllDescriptors(memberScope)) {
                if (descriptor is ClassDescriptor) {
                    konst classProperties: Collection<PropertyDescriptor?> = descriptor.getMemberScope(emptyList())
                        .getContributedVariables(propertyName, NoLookupLocation.FROM_TEST)
                    if (!classProperties.isEmpty()) {
                        properties = classProperties
                        break
                    }
                }
            }
        }
        if (properties.size != 1) {
            return null
        }
        return properties.iterator().next()
    }

    private fun getLocalVarDescriptor(context: BindingContext, name: String): VariableDescriptor? {
        for (descriptor in context.getSliceContents(BindingContext.VARIABLE).konstues) {
            if (descriptor.name.asString() == name) {
                return descriptor
            }
        }
        TestCase.fail("Failed to find local variable $name")
        return null
    }
}
