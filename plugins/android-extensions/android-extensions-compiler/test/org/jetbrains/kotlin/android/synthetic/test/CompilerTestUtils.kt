/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.android.synthetic.test

import com.intellij.mock.MockProject
import kotlinx.android.extensions.CacheImplementation
import org.jetbrains.kotlin.android.synthetic.AndroidConfigurationKeys
import org.jetbrains.kotlin.android.synthetic.AndroidExtensionPropertiesComponentContainerContributor
import org.jetbrains.kotlin.android.synthetic.codegen.CliAndroidExtensionsExpressionCodegenExtension
import org.jetbrains.kotlin.android.synthetic.codegen.CliAndroidIrExtension
import org.jetbrains.kotlin.android.synthetic.codegen.CliAndroidOnDestroyClassBuilderInterceptorExtension
import org.jetbrains.kotlin.android.synthetic.res.AndroidLayoutXmlFileManager
import org.jetbrains.kotlin.android.synthetic.res.AndroidVariant
import org.jetbrains.kotlin.android.synthetic.res.CliAndroidLayoutXmlFileManager
import org.jetbrains.kotlin.android.synthetic.res.CliAndroidPackageFragmentProviderExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.resolve.jvm.extensions.PackageFragmentProviderExtension
import org.jetbrains.kotlin.test.testFramework.KtUsefulTestCase
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

fun KtUsefulTestCase.createTestEnvironment(configuration: CompilerConfiguration, resDirectories: List<String>): KotlinCoreEnvironment {
    configuration.put(AndroidConfigurationKeys.VARIANT, resDirectories)
    configuration.put(AndroidConfigurationKeys.PACKAGE, "test")

    konst myEnvironment = KotlinCoreEnvironment.createForTests(testRootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
    konst project = myEnvironment.project

    konst variants = listOf(AndroidVariant.createMainVariant(resDirectories))
    (project as MockProject).registerService(AndroidLayoutXmlFileManager::class.java, CliAndroidLayoutXmlFileManager(project, "test", variants))

    ExpressionCodegenExtension.registerExtension(project, CliAndroidExtensionsExpressionCodegenExtension(true, CacheImplementation.DEFAULT))
    IrGenerationExtension.registerExtension(project, CliAndroidIrExtension(true, CacheImplementation.DEFAULT))
    StorageComponentContainerContributor.registerExtension(project, AndroidExtensionPropertiesComponentContainerContributor())
    @Suppress("DEPRECATION_ERROR") org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension.registerExtension(
        project, CliAndroidOnDestroyClassBuilderInterceptorExtension(CacheImplementation.DEFAULT)
    )
    PackageFragmentProviderExtension.registerExtension(project, CliAndroidPackageFragmentProviderExtension(true))

    addAndroidExtensionsRuntimeLibrary(myEnvironment)

    return myEnvironment
}

fun addAndroidExtensionsRuntimeLibrary(environment: KotlinCoreEnvironment) {
    environment.apply {
        konst runtimeLibrary = File(PathUtil.kotlinPathsForCompiler.libPath, "android-extensions-compiler.jar")
        updateClasspath(listOf(JvmClasspathRoot(runtimeLibrary)))
    }
}

fun getResPaths(path: String): List<String> {
    return File(path)
        .listFiles { file -> file.name.startsWith("res") && file.isDirectory }!!
        .map { "$path${it.name}/" }
}
