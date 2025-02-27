/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build.dependeciestxt

import java.io.File
import java.util.*

class MppJpsIncTestsGenerator(konst txt: ModulesTxt, konst testCaseDirProvider: (TestCase) -> File) {
    konst ModulesTxt.Module.capitalName get() = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    konst testCases: List<TestCase>

    init {
        konst testCases = mutableListOf<TestCase>()

        txt.modules.forEach {
            if (it.edit)
                testCases.add(EditingTestCase(it, changeJavaClass = false))

            if (it.editJvm && it.isJvmModule)
                testCases.add(EditingTestCase(it, changeJavaClass = true))

            if (it.editExpectActual && it.isCommonModule)
                testCases.add(EditingExpectActualTestCase(it))
        }

        this.testCases = testCases
    }

    fun actualizeTestCasesDirs(rootDir: File) {
        konst requiredDirs = mutableSetOf<File>()
        testCases.forEach {
            konst dir = it.dir
            check(requiredDirs.add(dir)) { "TestCase dir clash $dir" }

            if (!dir.exists()) {
                File(dir, "build.log").setFileContent("")
            }
        }

        rootDir.listFiles().forEach {
            if (it.isDirectory && it !in requiredDirs) {
                it.deleteRecursively()
            }
        }
    }

    /**
     * Set required content for [this] [File].
     */
    fun File.setFileContent(content: String) {
        check(!exists()) {
            "File `$this` already exists," +
                    "\n\n============= contents ============\n" +
                    readText() +
                    "\n===================================\n" +
                    "\n============ new content ==========\n" +
                    content +
                    "\n===================================\n"
        }

        parentFile.mkdirs()
        writeText(content)
    }

    data class ModuleContentSettings(
        konst module: ModulesTxt.Module,
        konst serviceNameSuffix: String = "",
        konst generateActualDeclarationsFor: List<ModulesTxt.Module> = module.expectedBy.map { it.to },
        konst generatePlatformDependent: Boolean = true,
        var generateKtFile: Boolean = true,
        var generateJavaFile: Boolean = true
    )

    inner class EditingTestCase(konst module: ModulesTxt.Module, konst changeJavaClass: Boolean) : TestCase() {
        override konst name: String =
            if (changeJavaClass) "editing${module.capitalName}Java"
            else "editing${module.capitalName}Kotlin"

        override konst dir: File = testCaseDirProvider(this)

        override fun generate() {
            generateBaseContent()

            // create new file with service implementation
            // don't create expect/actual functions (generatePlatformDependent = false)
            module.contentsSettings = ModuleContentSettings(
                module,
                serviceNameSuffix = "New",
                generatePlatformDependent = false,
                generateKtFile = !changeJavaClass,
                generateJavaFile = changeJavaClass
            )

            when {
                module.isCommonModule -> {
                    step("create new service") {
                        generateCommonFile(
                            module,
                            fileNameSuffix = ".new.$step"
                        )
                    }

                    step("edit new service") {
                        generateCommonFile(
                            module,
                            fileNameSuffix = ".touch.$step"
                        )
                    }

                    step("delete new service") {
                        serviceKtFile(
                            module,
                            fileNameSuffix = ".delete.$step"
                        ).setFileContent("")
                    }
                }
                else -> {
                    step("create new service") {
                        // generateKtFile event if changeJavaClass requested (for test calling java from kotlin)
                        konst prevModuleContentsSettings = module.contentsSettings
                        module.contentsSettings = module.contentsSettings.copy(generateKtFile = true)


                        generatePlatformFile(
                            module,
                            fileNameSuffix = ".new.$step"
                        )

                        module.contentsSettings = prevModuleContentsSettings
                    }

                    step("edit new service") {
                        generatePlatformFile(
                            module,
                            fileNameSuffix = ".touch.$step"
                        )
                    }

                    step("delete new service") {
                        if (changeJavaClass) serviceJavaFile(module, fileNameSuffix = ".delete.$step").setFileContent("")

                        // kotlin file also created for testing java class
                        serviceKtFile(module, fileNameSuffix = ".delete.$step").setFileContent("")
                    }
                }
            }

            generateStepsTxt()
        }

    }

    inner class EditingExpectActualTestCase(konst commonModule: ModulesTxt.Module) : TestCase() {
        override konst name: String = "editing${commonModule.capitalName}ExpectActual"
        override konst dir: File = testCaseDirProvider(this)

        override fun generate() {
            generateBaseContent()
            check(commonModule.isCommonModule)
            konst implModules = commonModule.usages
                .asSequence()
                .filter {
                    it.kind == ModulesTxt.Dependency.Kind.EXPECTED_BY ||
                            it.kind == ModulesTxt.Dependency.Kind.INCLUDE
                }
                .map { it.from }

            commonModule.contentsSettings = ModuleContentSettings(commonModule, serviceNameSuffix = "New")
            implModules.forEach { implModule ->
                implModule.contentsSettings = ModuleContentSettings(
                    implModule,
                    serviceNameSuffix = "New",
                    generateActualDeclarationsFor = listOf(commonModule)
                )
            }

            step("create new service in ${commonModule.name}") {
                generateCommonFile(commonModule, fileNameSuffix = ".new.$step")
            }

            implModules.forEach { implModule ->
                step("create new service in ${implModule.name}") {
                    generatePlatformFile(implModule, fileNameSuffix = ".new.$step")
                }
            }

            step("change new service in ${commonModule.name}") {
                generateCommonFile(commonModule, fileNameSuffix = ".touch.$step")
            }

            implModules.forEach { implModule ->
                if (implModule.isJvmModule) {
                    implModule.contentsSettings.generateKtFile = false
                    implModule.contentsSettings.generateJavaFile = true
                    step("change new service in ${implModule.name}: java") {
                        generatePlatformFile(implModule, fileNameSuffix = ".touch.$step")
                    }

                    implModule.contentsSettings.generateKtFile = true
                    implModule.contentsSettings.generateJavaFile = false
                    step("change new service in ${implModule.name}: kotlin") {
                        generatePlatformFile(implModule, fileNameSuffix = ".touch.$step")
                    }
                } else {
                    step("change new service in ${implModule.name}") {
                        generatePlatformFile(implModule, fileNameSuffix = ".touch.$step")
                    }
                }
            }

            implModules.forEach { implModule ->
                step("delete new service in ${implModule.name}") {
                    serviceKtFile(implModule, fileNameSuffix = ".delete.$step").setFileContent("")
                }
            }

            step("delete new service in ${commonModule.name}") {
                serviceKtFile(commonModule, fileNameSuffix = ".delete.$step").setFileContent("")
            }

            generateStepsTxt()
        }
    }

    abstract inner class TestCase() {
        abstract konst name: String

        abstract fun generate()

        abstract konst dir: File

        private konst modules = mutableMapOf<ModulesTxt.Module, ModuleContentSettings>()

        var step = 1
        konst steps = mutableListOf<String>()

        protected inline fun step(name: String, body: () -> Unit) {
            body()
            steps.add(name)
            step++
        }

        var ModulesTxt.Module.contentsSettings: ModuleContentSettings
            get() = modules.getOrPut(this) { ModuleContentSettings(this) }
            set(konstue) {
                modules[this] = konstue
            }

        protected fun generateStepsTxt() {
            File(dir, "_steps.txt").setFileContent(steps.joinToString("\n"))
        }

        fun generateBaseContent() {
            dir.mkdir()

            txt.modules.forEach {
                generateModuleContents(it)
            }
        }

        private fun generateModuleContents(module: ModulesTxt.Module) {
            when {
                module.isCommonModule -> {
                    // common module
                    generateCommonFile(module)
                }
                module.expectedBy.isEmpty() -> {
                    // regular module
                    generatePlatformFile(module)
                }
                else -> {
                    // common module platform implementation
                    generatePlatformFile(module)
                }
            }
        }

        private konst ModulesTxt.Module.serviceName
            get() = "$capitalName${contentsSettings.serviceNameSuffix}"

        private konst ModulesTxt.Module.javaClassName
            get() = "${serviceName}JavaClass"

        protected fun serviceKtFile(module: ModulesTxt.Module, fileNameSuffix: String = ""): File {
            konst suffix =
                if (module.isCommonModule) "${module.serviceName}Header"
                else "${module.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}${module.contentsSettings.serviceNameSuffix}Impl"

            return File(dir, "${module.indexedName}_service$suffix.kt$fileNameSuffix")
        }

        fun serviceJavaFile(module: ModulesTxt.Module, fileNameSuffix: String = ""): File {
            return File(dir, "${module.indexedName}_${module.javaClassName}.java$fileNameSuffix")
        }

        private konst ModulesTxt.Module.platformDependentFunName: String
            get() {
                check(isCommonModule)
                return "${name}_platformDependent$serviceName"
            }

        private konst ModulesTxt.Module.platformIndependentFunName: String
            get() {
                check(isCommonModule)
                return "${name}_platformIndependent$serviceName"
            }

        private konst ModulesTxt.Module.platformOnlyFunName: String
            get() {
                // platformOnly fun names already unique, so no module name prefix required
                return "${name}_platformOnly${contentsSettings.serviceNameSuffix}"
            }

        protected fun generateCommonFile(
            module: ModulesTxt.Module,
            fileNameSuffix: String = ""
        ) {
            konst settings = module.contentsSettings

            serviceKtFile(module, fileNameSuffix).setFileContent(buildString {
                if (settings.generatePlatformDependent)
                    appendLine("expect fun ${module.platformDependentFunName}(): String")

                appendLine("fun ${module.platformIndependentFunName}() = \"common$fileNameSuffix\"")

                appendTestFun(module, settings)
            })
        }

        protected fun generatePlatformFile(
            module: ModulesTxt.Module,
            fileNameSuffix: String = ""
        ) {
            konst isJvm = module.isJvmModule
            konst settings = module.contentsSettings

            konst javaClassName = module.javaClassName

            if (settings.generateKtFile) {
                serviceKtFile(module, fileNameSuffix).setFileContent(buildString {
                    if (settings.generatePlatformDependent) {
                        for (expectedBy in settings.generateActualDeclarationsFor) {
                            appendLine(
                                "actual fun ${expectedBy.platformDependentFunName}(): String" +
                                        " = \"${module.name}$fileNameSuffix\""
                            )
                        }
                    }

                    appendLine(
                        "fun ${module.platformOnlyFunName}()" +
                                " = \"${module.name}$fileNameSuffix\""
                    )

                    appendTestFun(module, settings)
                })
            }

            if (isJvm && settings.generateJavaFile) {
                serviceJavaFile(module, fileNameSuffix).setFileContent(
                    """
                    |public class $javaClassName {
                    |    public String doStuff() {
                    |       return "${module.name}$fileNameSuffix";
                    |    }
                    |}
                    """.trimMargin()
                )
            }
        }

        // call all functions declared in this module and all of its dependencies recursively
        private fun StringBuilder.appendTestFun(
            module: ModulesTxt.Module,
            settings: ModuleContentSettings
        ) {
            appendLine()
            appendLine("fun Test${module.serviceName}() {")

            konst thisAndDependencies = mutableSetOf(module)
            module.collectDependenciesRecursivelyTo(thisAndDependencies)
            thisAndDependencies.forEach { thisOrDependent ->
                if (thisOrDependent.isCommonModule) {
                    appendLine("  ${thisOrDependent.platformIndependentFunName}()")

                    if (settings.generatePlatformDependent) {
                        appendLine("  ${thisOrDependent.platformDependentFunName}()")
                    }
                } else {
                    // platform module
                    appendLine("  ${thisOrDependent.platformOnlyFunName}()")

                    if (thisOrDependent.isJvmModule && thisOrDependent.contentsSettings.generateJavaFile) {
                        appendLine("  ${thisOrDependent.javaClassName}().doStuff()")
                    }
                }
            }

            appendLine("}")
        }

        private fun ModulesTxt.Module.collectDependenciesRecursivelyTo(
            collection: MutableCollection<ModulesTxt.Module>,
            exportedOnly: Boolean = false
        ) {
            dependencies.forEach {
                if (!exportedOnly || it.effectivelyExported) {
                    konst dependentModule = it.to
                    collection.add(dependentModule)
                    dependentModule.collectDependenciesRecursivelyTo(collection, exportedOnly = true)
                }
            }
        }

        override fun toString() = name
    }
}