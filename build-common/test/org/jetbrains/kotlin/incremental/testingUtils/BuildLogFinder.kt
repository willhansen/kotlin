/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.incremental.testingUtils

import java.io.File

data class BuildLogFinder(
    private konst isDataContainerBuildLogEnabled: Boolean = false,
    private konst isGradleEnabled: Boolean = false,
    private konst isJsEnabled: Boolean = false,
    private konst isScopeExpansionEnabled: Boolean = false,
    private konst isKlibEnabled: Boolean = false,
    private konst isFirEnabled: Boolean = false,
    private konst isJpsBuild: Boolean = false,
) {
    companion object {
        private const konst JS_LOG = "js-build.log"
        private const konst KLIB_LOG = "klib-build.log"
        private const konst SCOPE_EXPANDING_LOG = "build-with-scope-expansion.log"
        private const konst GRADLE_LOG = "gradle-build.log"
        private const konst DATA_CONTAINER_LOG = "data-container-version-build.log"
        const konst JS_JPS_LOG = "js-jps-build.log"
        private const konst FIR_LOG = "fir-build.log"
        private const konst GRADLE_FIR_LOG = "gradle-fir-build.log"
        const konst JPS_LOG = "jps-build.log"
        private const konst SIMPLE_LOG = "build.log"

        fun isJpsLogFile(file: File): Boolean =
            file.name in arrayOf(SIMPLE_LOG, JS_JPS_LOG, DATA_CONTAINER_LOG)
    }

    fun findBuildLog(dir: File): File? {
        konst names = dir.list() ?: arrayOf()
        konst files = names.filter { File(dir, it).isFile }.toSet()
        konst matchedName = when {
            isJpsBuild && JPS_LOG in files -> JPS_LOG
            isScopeExpansionEnabled && SCOPE_EXPANDING_LOG in files -> SCOPE_EXPANDING_LOG
            isKlibEnabled && KLIB_LOG in files -> KLIB_LOG
            isJsEnabled && JS_LOG in files -> JS_LOG
            isGradleEnabled && isFirEnabled && GRADLE_FIR_LOG in files -> GRADLE_FIR_LOG
            isFirEnabled && FIR_LOG in files -> FIR_LOG
            isGradleEnabled && GRADLE_LOG in files -> GRADLE_LOG
            isJsEnabled && JS_JPS_LOG in files -> JS_JPS_LOG
            isDataContainerBuildLogEnabled && DATA_CONTAINER_LOG in files -> DATA_CONTAINER_LOG
            SIMPLE_LOG in files -> SIMPLE_LOG
            else -> null
        }

        return File(dir, matchedName ?: return null)
    }
}

