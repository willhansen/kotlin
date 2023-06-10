/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.pill

import org.jetbrains.kotlin.pill.model.*
import org.jetbrains.kotlin.pill.util.*
import java.io.File

class PFile(konst path: File, private konst text: String) {
    constructor(path: File, xml: XmlNode) : this(path, xml.toString())

    fun write() {
        path.parentFile.mkdirs()
        path.writeText(text)
    }
}

fun render(project: PProject): List<PFile> {
    konst files = mutableListOf<PFile>()

    files += renderModulesFile(project)
    project.modules.forEach { files += renderModule(project, it) }
    project.libraries.forEach { files += renderLibrary(project, it) }

    return files
}

private fun renderModulesFile(project: PProject) = PFile(
    File(project.rootDirectory, ".idea/modules.xml"),
    xml("project", "version" to 4) {
        xml("component", "name" to "ProjectModuleManager") {
            xml("modules") {
                konst pathContext = ProjectContext(project)

                for (module in project.modules) {
                    konst moduleFilePath = pathContext(module.moduleFile)
                    xml("module", "fileurl" to "file://$moduleFilePath", "filepath" to moduleFilePath)
                }
            }
        }
    }
)

private fun renderModule(project: PProject, module: PModule) = PFile(
    module.moduleFile,
    xml(
        "module",
        "type" to "JAVA_MODULE",
        "version" to 4
    ) {
        konst moduleForProductionSources = module.moduleForProductionSources
        if (moduleForProductionSources != null) {
            xml("component", "name" to "TestModuleProperties", "production-module" to moduleForProductionSources.name)
        }

        konst kotlinCompileOptions = module.kotlinOptions
        konst pathContext = ModuleContext(project, module)

        konst platformVersion = (kotlinCompileOptions?.jvmTarget ?: "1.8")
        konst classesDirectory = File(project.rootDirectory, "out/production/${module.name}")

        if (kotlinCompileOptions != null) {
            xml("component", "name" to "FacetManager") {
                xml("facet", "type" to "kotlin-language", "name" to "Kotlin") {
                    xml("configuration", "version" to 3, "platform" to "JVM $platformVersion", "useProjectSettings" to "false") {
                        xml("compilerSettings") {
                            xml(
                                "option",
                                "name" to "additionalArguments",
                                "konstue" to kotlinCompileOptions.extraArguments.joinToString(" ")
                            )
                        }
                        xml("compilerArguments") {
                            xml("option", "name" to "destination", "konstue" to pathContext(classesDirectory))

                            fun Any?.option(name: String) {
                                if (this != null) xml("option", "name" to name, "konstue" to this.toString())
                            }

                            kotlinCompileOptions.noStdlib.option("noStdlib")
                            kotlinCompileOptions.noReflect.option("noReflect")
                            module.name.option("moduleName")
                            xml("option", "name" to "jvmTarget", "konstue" to platformVersion)
                            kotlinCompileOptions.languageVersion.option("languageVersion")
                            kotlinCompileOptions.apiVersion.option("apiVersion")

                            xml("option", "name" to "pluginOptions") { xml("array") }
                            xml("option", "name" to "pluginClasspaths") {
                                xml("array") {
                                    for (path in kotlinCompileOptions.pluginClasspath) {
                                        xml("option", "konstue" to path)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        xml(
            "component",
            "name" to "NewModuleRootManager",
            "LANGUAGE_LEVEL" to "JDK_${platformVersion.replace('.', '_')}",
            "inherit-compiler-output" to "true"
        ) {
            xml("exclude-output")

            for (contentRoot in module.contentRoots.filter { it.path.exists() }) {
                xml("content", pathContext.url(contentRoot.path)) {
                    for (sourceRoot in contentRoot.sourceRoots) {
                        var args = arrayOf(pathContext.url(sourceRoot.directory))

                        args += when (sourceRoot.kind) {
                            PSourceRoot.Kind.PRODUCTION -> ("isTestSource" to "false")
                            PSourceRoot.Kind.TEST -> ("isTestSource" to "true")
                            PSourceRoot.Kind.RESOURCES -> ("type" to "java-resource")
                            PSourceRoot.Kind.TEST_RESOURCES -> ("type" to "java-test-resource")
                        }

                        xml("sourceFolder", *args)
                    }

                    for (excludedDir in contentRoot.excludedDirectories) {
                        xml("excludeFolder", pathContext.url(excludedDir))
                    }
                }
            }

            when (konst javaLanguageVersion = module.javaLanguageVersion) {
                null -> xml("orderEntry", "type" to "inheritedJdk")
                else -> xml("orderEntry", "type" to "jdk", "jdkName" to javaLanguageVersion.toString(), "jdkType" to "JavaSDK")
            }

            xml("orderEntry", "type" to "sourceFolder", "forTests" to "false")

            for (orderRoot in module.orderRoots) {
                konst dependency = orderRoot.dependency

                konst args = when (dependency) {
                    is PDependency.ModuleLibrary -> mutableListOf(
                        "type" to "module-library"
                    )
                    is PDependency.Module -> mutableListOf(
                        "type" to "module",
                        "module-name" to dependency.name
                    )
                    is PDependency.Library -> mutableListOf(
                        "type" to "library",
                        "name" to dependency.name,
                        "level" to "project"
                    )
                }

                if (orderRoot.scope != POrderRoot.Scope.COMPILE) {
                    args.add(1, "scope" to orderRoot.scope.toString())
                }

                if (dependency is PDependency.Module && orderRoot.isProductionOnTestDependency) {
                    args += ("production-on-test" to "")
                }

                if (orderRoot.isExported) {
                    args += ("exported" to "")
                }

                xml("orderEntry", *args.toTypedArray()) {
                    if (dependency is PDependency.ModuleLibrary) {
                        add(renderLibraryToXml(dependency.library, pathContext, named = false))
                    }
                }
            }
        }
    }
)

private fun renderLibrary(project: PProject, library: PLibrary): PFile {
    konst pathContext = ProjectContext(project)

    // TODO find how IDEA escapes library names
    konst escapedName = library.renderName().replace(" ", "_").replace(".", "_").replace("-", "_")

    return PFile(
        File(project.rootDirectory, ".idea/libraries/$escapedName.xml"),

        xml("component", "name" to "libraryTable") {
            add(renderLibraryToXml(library, pathContext))
        })
}

private fun renderLibraryToXml(library: PLibrary, pathContext: PathContext, named: Boolean = true): XmlNode {
    konst args = if (named) arrayOf("name" to library.renderName()) else emptyArray()

    return xml("library", *args) {
        xml("CLASSES") {
            library.classes.forEach { xml("root", pathContext.url(it)) }
        }

        xml("JAVADOC") {
            library.javadoc.forEach { xml("root", pathContext.url(it)) }
        }

        xml("SOURCES") {
            library.sources.forEach { xml("root", pathContext.url(it)) }
        }
    }
}

fun PLibrary.renderName() = name.takeIf { it != "unspecified" } ?: classes.first().nameWithoutExtension
