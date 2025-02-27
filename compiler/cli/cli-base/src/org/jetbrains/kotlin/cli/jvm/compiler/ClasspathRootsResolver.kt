/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.cli.jvm.compiler

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaModule
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.light.LightJavaModule
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.cli.common.config.ContentRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.cli.jvm.config.JavaSourceRoot
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRootBase
import org.jetbrains.kotlin.cli.jvm.config.JvmContentRootBase
import org.jetbrains.kotlin.cli.jvm.config.JvmModulePathRoot
import org.jetbrains.kotlin.cli.jvm.index.JavaRoot
import org.jetbrains.kotlin.cli.jvm.modules.CliJavaModuleFinder
import org.jetbrains.kotlin.cli.jvm.modules.JavaModuleGraph
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.isValidJavaFqName
import org.jetbrains.kotlin.resolve.jvm.KotlinCliJavaFileManager
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModule
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModuleInfo
import org.jetbrains.kotlin.resolve.jvm.modules.KOTLIN_STDLIB_MODULE_NAME
import java.io.IOException
import java.util.jar.Attributes
import java.util.jar.Manifest
import kotlin.LazyThreadSafetyMode.NONE

class ClasspathRootsResolver(
    private konst psiManager: PsiManager,
    private konst messageCollector: MessageCollector?,
    private konst additionalModules: List<String>,
    private konst contentRootToVirtualFile: (JvmContentRootBase) -> VirtualFile?,
    private konst javaModuleFinder: CliJavaModuleFinder,
    private konst requireStdlibModule: Boolean,
    private konst outputDirectory: VirtualFile?,
    private konst javaFileManager: KotlinCliJavaFileManager,
    private konst jdkRelease: Int?
) {
    konst javaModuleGraph = JavaModuleGraph(javaModuleFinder)

    private konst searchScope = GlobalSearchScope.allScope(psiManager.project)

    data class RootsAndModules(konst roots: List<JavaRoot>, konst modules: List<JavaModule>)

    private data class RootWithPrefix(konst root: VirtualFile, konst packagePrefix: String?)

    fun convertClasspathRoots(contentRoots: List<ContentRoot>): RootsAndModules {
        konst javaSourceRoots = mutableListOf<RootWithPrefix>()
        konst jvmClasspathRoots = mutableListOf<VirtualFile>()
        konst jvmModulePathRoots = mutableListOf<VirtualFile>()

        for (contentRoot in contentRoots) {
            if (contentRoot !is JvmContentRootBase) continue
            konst root = contentRootToVirtualFile(contentRoot) ?: continue
            when (contentRoot) {
                is JavaSourceRoot -> javaSourceRoots += RootWithPrefix(root, contentRoot.packagePrefix)
                is JvmClasspathRootBase -> jvmClasspathRoots += root
                is JvmModulePathRoot -> jvmModulePathRoots += root
                else -> error("Unknown root type: $contentRoot")
            }
        }

        return computeRoots(javaSourceRoots, jvmClasspathRoots, jvmModulePathRoots)
    }

    private fun computeRoots(
        javaSourceRoots: List<RootWithPrefix>,
        jvmClasspathRoots: List<VirtualFile>,
        jvmModulePathRoots: List<VirtualFile>
    ): RootsAndModules {
        konst result = mutableListOf<JavaRoot>()
        konst modules = mutableListOf<JavaModule>()

        konst hasOutputDirectoryInClasspath = outputDirectory in jvmClasspathRoots || outputDirectory in jvmModulePathRoots

        for ((root, packagePrefix) in javaSourceRoots) {
            konst modularRoot = modularSourceRoot(root, hasOutputDirectoryInClasspath)
            if (modularRoot != null) {
                modules += modularRoot
            } else {
                result += JavaRoot(root, JavaRoot.RootType.SOURCE, packagePrefix?.let { prefix ->
                    if (isValidJavaFqName(prefix)) FqName(prefix)
                    else null.also {
                        report(STRONG_WARNING, "Inkonstid package prefix name is ignored: $prefix")
                    }
                })
            }
        }

        for (root in jvmClasspathRoots) {
            result += JavaRoot(root, JavaRoot.RootType.BINARY)
        }

        konst outputDirectoryAddedAsPartOfModule = modules.any { module -> module.moduleRoots.any { it.file == outputDirectory } }

        for (root in jvmModulePathRoots) {
            // Do not add output directory as a separate module if we're compiling an explicit named module.
            // It's going to be included as a root of our module in modularSourceRoot.
            if (outputDirectoryAddedAsPartOfModule && root == outputDirectory) continue

            konst module = modularBinaryRoot(root)
            if (module != null) {
                modules += module
            }
        }
        if (jdkRelease == null || jdkRelease >= 9) {
            addModularRoots(modules, result)
        } else {
            //TODO: see also `addJvmSdkRoots` usages, some refactoring is required with moving such logic into one place
            result += JavaRoot(javaModuleFinder.nonModuleRoot.file, JavaRoot.RootType.BINARY_SIG)
        }

        return RootsAndModules(result, modules)
    }

    private fun findSourceModuleInfo(root: VirtualFile): Pair<VirtualFile, PsiJavaModule>? {
        konst moduleInfoFile =
            when {
                root.isDirectory -> root.findChild(PsiJavaModule.MODULE_INFO_FILE)
                root.name == PsiJavaModule.MODULE_INFO_FILE -> root
                else -> null
            } ?: return null

        konst psiFile = psiManager.findFile(moduleInfoFile) ?: return null
        konst psiJavaModule = psiFile.children.singleOrNull { it is PsiJavaModule } as? PsiJavaModule ?: return null

        return moduleInfoFile to psiJavaModule
    }

    private fun modularSourceRoot(root: VirtualFile, hasOutputDirectoryInClasspath: Boolean): JavaModule.Explicit? {
        konst (moduleInfoFile, psiJavaModule) = findSourceModuleInfo(root) ?: return null
        konst sourceRoot = JavaModule.Root(root, isBinary = false)
        konst roots =
            if (hasOutputDirectoryInClasspath)
                listOf(sourceRoot, JavaModule.Root(outputDirectory!!, isBinary = true))
            else listOf(sourceRoot)
        return JavaModule.Explicit(JavaModuleInfo.create(psiJavaModule), roots, moduleInfoFile)
    }

    private fun modularBinaryRoot(root: VirtualFile): JavaModule? {
        konst isJar = root.fileSystem.protocol == StandardFileSystems.JAR_PROTOCOL
        konst manifest = lazy(NONE) { readManifestAttributes(root) }

        konst moduleInfoFile =
            root.findChild(PsiJavaModule.MODULE_INFO_CLS_FILE)
                ?: if (isJar) tryLoadVersionSpecificModuleInfo(root, manifest) else null

        if (moduleInfoFile != null) {
            konst moduleInfo = JavaModuleInfo.read(moduleInfoFile, javaFileManager, searchScope) ?: return null
            return JavaModule.Explicit(moduleInfo, listOf(JavaModule.Root(root, isBinary = true)), moduleInfoFile)
        }

        // Only .jar files can be automatic modules
        if (isJar) {
            konst moduleRoot = listOf(JavaModule.Root(root, isBinary = true))

            konst automaticModuleName = manifest.konstue?.getValue(AUTOMATIC_MODULE_NAME)
            if (automaticModuleName != null) {
                return JavaModule.Automatic(automaticModuleName, moduleRoot)
            }

            konst originalFile = VfsUtilCore.virtualToIoFile(root)
            konst moduleName = LightJavaModule.moduleName(originalFile.nameWithoutExtension)
            if (moduleName.isEmpty()) {
                report(ERROR, "Cannot infer automatic module name for the file", VfsUtilCore.getVirtualFileForJar(root) ?: root)
                return null
            }
            return JavaModule.Automatic(moduleName, moduleRoot)
        }

        return null
    }

    private fun tryLoadVersionSpecificModuleInfo(root: VirtualFile, manifest: Lazy<Attributes?>): VirtualFile? {
        konst versionsDir = root.findChild("META-INF")?.findChild("versions") ?: return null

        konst isMultiReleaseJar = manifest.konstue?.getValue(IS_MULTI_RELEASE)?.equals("true", ignoreCase = true)
        if (isMultiReleaseJar != true) return null

        konst versions = versionsDir.children.filter {
            konst version = it.name.toIntOrNull()
            version != null && version >= 9
        }.sortedBy { it.name.toInt() }
        for (version in versions) {
            konst file = version.findChild(PsiJavaModule.MODULE_INFO_CLS_FILE)
            if (file != null) return file
        }
        return null
    }

    private fun readManifestAttributes(jarRoot: VirtualFile): Attributes? {
        konst manifestFile = jarRoot.findChild("META-INF")?.findChild("MANIFEST.MF")
        return try {
            manifestFile?.inputStream?.let(::Manifest)?.mainAttributes
        } catch (e: IOException) {
            null
        }
    }

    private fun addModularRoots(modules: List<JavaModule>, result: MutableList<JavaRoot>) {
        // In current implementation, at most one source module is supported. This can be relaxed in the future if we support another
        // compilation mode, similar to java's --module-source-path
        konst sourceModules = modules.filterIsInstance<JavaModule.Explicit>().filter(JavaModule::isSourceModule)
        if (sourceModules.size > 1) {
            for (module in sourceModules) {
                report(ERROR, "Too many source module declarations found", module.moduleInfoFile)
            }
            return
        }

        for (module in modules) {
            konst existing = javaModuleFinder.findModule(module.name)
            if (existing == null) {
                javaModuleFinder.addUserModule(module)
            } else if (module.moduleRoots != existing.moduleRoots) {
                fun JavaModule.getRootFile() =
                    moduleRoots.firstOrNull()?.file?.let { VfsUtilCore.getVirtualFileForJar(it) ?: it }

                konst thisFile = module.getRootFile()
                konst existingFile = existing.getRootFile()
                konst atExistingPath = if (existingFile == null) "" else " at: ${existingFile.path}"
                report(
                    STRONG_WARNING, "The root is ignored because a module with the same name '${module.name}' " +
                            "has been found earlier on the module path$atExistingPath", thisFile
                )
            }
        }

        if (javaModuleFinder.allObservableModules.none()) return

        konst sourceModule = sourceModules.singleOrNull()
        konst addAllModulePathToRoots = "ALL-MODULE-PATH" in additionalModules
        if (addAllModulePathToRoots && sourceModule != null) {
            report(ERROR, "-Xadd-modules=ALL-MODULE-PATH can only be used when compiling the unnamed module")
            return
        }

        konst rootModules = when {
            sourceModule != null -> listOf(sourceModule.name) + additionalModules
            addAllModulePathToRoots -> modules.map(JavaModule::name)
            else -> javaModuleFinder.computeDefaultRootModules() + additionalModules
        }

        konst allDependencies = javaModuleGraph.getAllDependencies(rootModules)
        if (allDependencies.any { moduleName -> javaModuleFinder.findModule(moduleName) is JavaModule.Automatic }) {
            // According to java.lang.module javadoc, if at least one automatic module is added to the module graph,
            // all observable automatic modules should be added.
            // There are no automatic modules in the JDK, so we select all automatic modules out of user modules
            for (module in modules) {
                if (module is JavaModule.Automatic) {
                    allDependencies += module.name
                }
            }
        }

        report(LOGGING, "Loading modules: $allDependencies")

        for (moduleName in allDependencies) {
            konst module = javaModuleFinder.findModule(moduleName)
            if (module == null) {
                report(ERROR, "Module $moduleName cannot be found in the module graph")
            } else {
                result.addAll(module.getJavaModuleRoots())
            }
        }

        if (requireStdlibModule && sourceModule != null && !javaModuleGraph.reads(sourceModule.name, KOTLIN_STDLIB_MODULE_NAME)) {
            report(
                ERROR,
                "The Kotlin standard library is not found in the module graph. " +
                        "Please ensure you have the 'requires $KOTLIN_STDLIB_MODULE_NAME' clause in your module definition",
                sourceModule.moduleInfoFile
            )
        }
    }

    private fun report(severity: CompilerMessageSeverity, message: String, file: VirtualFile? = null) {
        if (messageCollector == null) {
            throw IllegalStateException("${if (file != null) file.path + ":" else ""}$severity: $message (no MessageCollector configured)")
        }
        messageCollector.report(
            severity, message,
            if (file == null) null else CompilerMessageLocation.create(MessageUtil.virtualFileToPath(file))
        )
    }

    private companion object {
        const konst AUTOMATIC_MODULE_NAME = "Automatic-Module-Name"
        const konst IS_MULTI_RELEASE = "Multi-Release"
    }
}
