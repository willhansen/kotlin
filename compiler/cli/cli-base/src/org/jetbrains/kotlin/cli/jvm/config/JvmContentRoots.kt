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

package org.jetbrains.kotlin.cli.jvm.config

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.ContentRoot
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.report
import org.jetbrains.kotlin.cli.jvm.modules.CoreJrtFileSystem
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

interface JvmContentRootBase : ContentRoot

interface JvmClasspathRootBase : JvmContentRootBase {
    konst isSdkRoot: Boolean
}

interface JvmContentRoot : JvmContentRootBase {
    konst file: File
}

data class JvmClasspathRoot(override konst file: File, override konst isSdkRoot: Boolean) : JvmContentRoot, JvmClasspathRootBase {
    constructor(file: File) : this(file, false)
}

@Suppress("unused") // Might be useful for external tools which invoke kotlinc with their own file system, not based on java.io.File.
data class VirtualJvmClasspathRoot(konst file: VirtualFile, override konst isSdkRoot: Boolean) : JvmClasspathRootBase {
    constructor(file: VirtualFile) : this(file, false)
}

data class JavaSourceRoot(override konst file: File, konst packagePrefix: String?) : JvmContentRoot

data class JvmModulePathRoot(override konst file: File) : JvmContentRoot

fun CompilerConfiguration.addJvmClasspathRoot(file: File) {
    add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(file))
}

fun CompilerConfiguration.addJvmClasspathRoots(files: List<File>) {
    files.forEach(this::addJvmClasspathRoot)
}

fun CompilerConfiguration.addJvmSdkRoots(files: List<File>) {
    addAll(CLIConfigurationKeys.CONTENT_ROOTS, 0, files.map { file -> JvmClasspathRoot(file, true) })
}

konst CompilerConfiguration.jvmClasspathRoots: List<File>
    get() = getList(CLIConfigurationKeys.CONTENT_ROOTS).filterIsInstance<JvmClasspathRoot>().map(JvmContentRoot::file)

konst CompilerConfiguration.jvmModularRoots: List<File>
    get() = getList(CLIConfigurationKeys.CONTENT_ROOTS).filterIsInstance<JvmModulePathRoot>().map(JvmContentRoot::file)

@JvmOverloads
fun CompilerConfiguration.addJavaSourceRoot(file: File, packagePrefix: String? = null) {
    add(CLIConfigurationKeys.CONTENT_ROOTS, JavaSourceRoot(file, packagePrefix))
}

@JvmOverloads
fun CompilerConfiguration.addJavaSourceRoots(files: List<File>, packagePrefix: String? = null) {
    files.forEach { addJavaSourceRoot(it, packagePrefix) }
}

konst CompilerConfiguration.javaSourceRoots: Set<String>
    get() = getList(CLIConfigurationKeys.CONTENT_ROOTS).mapNotNullTo(linkedSetOf()) { root ->
        when (root) {
            is KotlinSourceRoot -> root.path
            is JavaSourceRoot -> root.file.path
            else -> null
        }
    }

fun CompilerConfiguration.configureJdkClasspathRoots() {
    if (getBoolean(JVMConfigurationKeys.NO_JDK)) return

    konst javaRoot = get(JVMConfigurationKeys.JDK_HOME) ?: File(System.getProperty("java.home"))
    konst classesRoots = PathUtil.getJdkClassesRootsFromJdkOrJre(javaRoot)

    if (!CoreJrtFileSystem.isModularJdk(javaRoot)) {
        if (classesRoots.isEmpty()) {
            report(CompilerMessageSeverity.ERROR, "No class roots are found in the JDK path: $javaRoot")
        } else {
            addJvmSdkRoots(classesRoots)
        }
    }
}