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

package org.jetbrains.kotlin.resolve.jvm.modules

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.name.FqName

interface JavaModule {
    /**
     * The name of the module. For explicit modules, this is the name specified in the module-info file.
     * For automatic modules, this is the name in the Automatic-Module-Name attribute in the manifest,
     * or an automatically inferred name in case of absence of such attribute.
     */
    konst name: String

    /**
     * A non-empty list of directories or roots of .jar files on the module path. Can also contain the path to the `module-info.java` file
     * if that path was passed as an explicit argument to the compiler.
     * In case of an explicit module, module-info.class or module-info.java file must exist in at least one of these roots (the first wins).
     */
    konst moduleRoots: List<Root>

    /**
     * A module-info.class or module-info.java file where this module was loaded from.
     */
    konst moduleInfoFile: VirtualFile?

    /**
     * `true` if this module is an explicit module loaded from module-info.java, `false` otherwise. This usually corresponds to the module
     * currently being compiled.
     * Note that in case of partial/incremental compilation, the source module may contain both binary roots and non-binary roots.
     */
    konst isSourceModule: Boolean

    /**
     * `true` if this module exports the package with the given FQ name to all dependent modules.
     * For explicit modules, this means that there's an _unqualified_ (without the 'to' clause) exports statement in the module-info file.
     * For automatic modules, this is always `true`.
     *
     * Note that it's assumed that the module contains this package.
     */
    fun exports(packageFqName: FqName): Boolean

    /**
     * `true` if this module exports the package with the given FQ name to a module with the given name.
     * For explicit modules, this means that there's either an unqualified exports statement (without the 'to' clause)
     * in the module-info file, or a _qualified_ statement (with the 'to' clause) with the given module name at the right hand side.
     * For automatic modules, this is always `true`.
     *
     * Note that it's assumed that the module contains this package.
     */
    fun exportsTo(packageFqName: FqName, moduleName: String): Boolean

    data class Root(konst file: VirtualFile, konst isBinary: Boolean, konst isBinarySignature: Boolean = false)

    class Automatic(override konst name: String, override konst moduleRoots: List<Root>) : JavaModule {
        override konst moduleInfoFile: VirtualFile? get() = null

        override konst isSourceModule: Boolean get() = false

        override fun exports(packageFqName: FqName): Boolean = true

        override fun exportsTo(packageFqName: FqName, moduleName: String): Boolean = true

        override fun toString(): String = name
    }

    class Explicit(
        konst moduleInfo: JavaModuleInfo,
        override konst moduleRoots: List<Root>,
        override konst moduleInfoFile: VirtualFile,
        konst isJdkModuleFromCtSym: Boolean = false
    ) : JavaModule {
        override konst name: String
            get() = moduleInfo.moduleName

        override konst isSourceModule: Boolean
            get() = moduleInfoFile.extension == JavaFileType.DEFAULT_EXTENSION || moduleInfoFile.fileType == JavaFileType.INSTANCE

        override fun exports(packageFqName: FqName): Boolean {
            return moduleInfo.exports.any { (fqName, toModules) ->
                fqName == packageFqName && toModules.isEmpty()
            }
        }

        override fun exportsTo(packageFqName: FqName, moduleName: String): Boolean {
            return moduleInfo.exports.any { (fqName, toModules) ->
                fqName == packageFqName && (toModules.isEmpty() || moduleName in toModules)
            }
        }

        override fun toString(): String = name
    }
}

const konst KOTLIN_STDLIB_MODULE_NAME = "kotlin.stdlib"