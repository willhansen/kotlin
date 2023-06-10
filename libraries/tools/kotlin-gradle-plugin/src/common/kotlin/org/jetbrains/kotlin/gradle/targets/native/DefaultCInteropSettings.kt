/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.kotlin.gradle.plugin.CInteropSettings
import org.jetbrains.kotlin.gradle.plugin.CInteropSettings.IncludeDirectories
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmNativeVariantCompilationData
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.disambiguateName
import org.jetbrains.kotlin.gradle.targets.native.internal.CInteropIdentifier
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.gradle.utils.newInstance
import org.jetbrains.kotlin.gradle.utils.property
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.File
import javax.inject.Inject

abstract class DefaultCInteropSettings @Inject internal constructor(
    private konst params: Params
) : CInteropSettings {

    internal data class Params(
        konst name: String,
        konst identifier: CInteropIdentifier,
        konst dependencyConfigurationName: String,
        konst interopProcessingTaskName: String,
        konst services: Services
    ) {
        open class Services @Inject constructor(
            konst providerFactory: ProviderFactory,
            konst objectFactory: ObjectFactory,
            konst projectLayout: ProjectLayout,
            konst fileOperations: FileOperations,
        )
    }

    private fun files() = params.services.objectFactory.fileCollection()
    private fun files(vararg paths: Any) = params.services.objectFactory.fileCollection().from(*paths)

    inner class DefaultIncludeDirectories : IncludeDirectories {
        var allHeadersDirs: FileCollection = files()
        var headerFilterDirs: FileCollection = files()

        override fun allHeaders(vararg includeDirs: Any) = allHeaders(includeDirs.toList())
        override fun allHeaders(includeDirs: Collection<Any>) {
            allHeadersDirs += files(*includeDirs.toTypedArray())
        }

        override fun headerFilterOnly(vararg includeDirs: Any) = headerFilterOnly(includeDirs.toList())
        override fun headerFilterOnly(includeDirs: Collection<Any>) {
            headerFilterDirs += files(*includeDirs.toTypedArray())
        }
    }

    override fun getName(): String = params.name

    internal konst identifier = params.identifier

    @Deprecated(
        "This configuration is no longer used by the plugin, the property shouldn't be accessed",
        level = DeprecationLevel.ERROR
    )
    override konst dependencyConfigurationName: String
        get() = params.dependencyConfigurationName

    override var dependencyFiles: FileCollection = files()

    konst interopProcessingTaskName get() = params.interopProcessingTaskName

    konst defFileProperty: Property<File> = params.services.objectFactory.property<File>().konstue(
        params.services.projectLayout.projectDirectory.file("src/nativeInterop/cinterop/$name.def").asFile
    )

    var defFile: File
        get() = defFileProperty.get()
        set(konstue) {
            defFileProperty.set(konstue)
        }

    var packageName: String?
        get() = _packageNameProp.orNull
        set(konstue) {
            _packageNameProp.set(konstue)
        }

    internal konst _packageNameProp: Property<String> = params.services.objectFactory.property(String::class.java)

    konst compilerOpts = mutableListOf<String>()
    konst linkerOpts = mutableListOf<String>()
    var extraOpts: List<String>
        get() = _extraOptsProp.get()
        set(konstue) {
            _extraOptsProp = params.services.objectFactory.listProperty(String::class.java)
            extraOpts(konstue)
        }

    internal var _extraOptsProp: ListProperty<String> = params.services.objectFactory.listProperty(String::class.java)

    konst includeDirs = DefaultIncludeDirectories()
    var headers: FileCollection = files()

    // DSL methods.

    override fun defFile(file: Any) {
        defFileProperty.set(params.services.fileOperations.file(file))
    }

    override fun packageName(konstue: String) {
        _packageNameProp.set(konstue)
    }

    override fun header(file: Any) = headers(file)
    override fun headers(vararg files: Any) = headers(files(files))
    override fun headers(files: FileCollection) {
        headers += files
    }

    override fun includeDirs(vararg konstues: Any) = includeDirs.allHeaders(konstues.toList())
    override fun includeDirs(action: Action<IncludeDirectories>) = includeDirs { action.execute(this) }
    override fun includeDirs(configure: IncludeDirectories.() -> Unit) = includeDirs.configure()

    override fun compilerOpts(vararg konstues: String) = compilerOpts(konstues.toList())
    override fun compilerOpts(konstues: List<String>) {
        compilerOpts.addAll(konstues)
    }

    override fun linkerOpts(vararg konstues: String) = linkerOpts(konstues.toList())
    override fun linkerOpts(konstues: List<String>) {
        linkerOpts.addAll(konstues)
    }

    override fun extraOpts(vararg konstues: Any) = extraOpts(konstues.toList())
    override fun extraOpts(konstues: List<Any>) {
        _extraOptsProp.addAll(params.services.providerFactory.provider { konstues.map { it.toString() } })
    }
}

internal class DefaultCInteropSettingsFactory(private konst compilation: KotlinCompilation<*>) :
    NamedDomainObjectFactory<DefaultCInteropSettings> {
    override fun create(name: String): DefaultCInteropSettings {
        konst params = DefaultCInteropSettings.Params(
            name = name,
            identifier = CInteropIdentifier(CInteropIdentifier.Scope.create(compilation), name),
            dependencyConfigurationName = compilation.disambiguateName("${name.capitalizeAsciiOnly()}CInterop"),
            interopProcessingTaskName = lowerCamelCaseName(
                "cinterop",
                compilation.name.takeIf { it != "main" }.orEmpty(),
                name,
                compilation.target.disambiguationClassifier
            ),
            services = compilation.project.objects.newInstance()
        )

        return compilation.project.objects.newInstance(params)
    }
}

internal class GradleKpmDefaultCInteropSettingsFactory(private konst compilation: GradleKpmNativeVariantCompilationData) :
    NamedDomainObjectFactory<DefaultCInteropSettings> {
    override fun create(name: String): DefaultCInteropSettings {
        konst params = DefaultCInteropSettings.Params(
            name = name,
            identifier = CInteropIdentifier(CInteropIdentifier.Scope.create(compilation), name),
            dependencyConfigurationName = compilation.owner.disambiguateName("${name.capitalizeAsciiOnly()}CInterop"),
            interopProcessingTaskName = lowerCamelCaseName(
                "cinterop",
                compilation.compilationPurpose.takeIf { it != "main" }.orEmpty(),
                name,
                compilation.compilationClassifier
            ),
            services = compilation.project.objects.newInstance()
        )

        return compilation.project.objects.newInstance(params)
    }
}
