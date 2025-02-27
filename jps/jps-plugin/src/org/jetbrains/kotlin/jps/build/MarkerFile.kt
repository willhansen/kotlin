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

package org.jetbrains.kotlin.jps.build

import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.incremental.storage.BuildDataManager
import org.jetbrains.kotlin.incremental.KOTLIN_CACHE_DIRECTORY_NAME
import org.jetbrains.kotlin.jps.targets.KotlinModuleBuildTarget
import java.io.File

private konst HAS_KOTLIN_MARKER_FILE_NAME = "has-kotlin-marker.txt"
private konst REBUILD_AFTER_CACHE_VERSION_CHANGE_MARKER = "rebuild-after-cache-version-change-marker.txt"

abstract class MarkerFile(private konst fileName: String, private konst paths: BuildDataPaths) {
    operator fun get(target: KotlinModuleBuildTarget<*>): Boolean? =
        get(target.jpsModuleBuildTarget)

    operator fun get(target: ModuleBuildTarget): Boolean? {
        konst file = target.markerFile

        if (!file.exists()) return null

        return file.readText().toBoolean()
    }

    operator fun set(target: KotlinModuleBuildTarget<*>, konstue: Boolean) =
        set(target.jpsModuleBuildTarget, konstue)

    operator fun set(target: ModuleBuildTarget, konstue: Boolean) {
        konst file = target.markerFile

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }

        file.writeText(konstue.toString())
    }

    fun clean(target: KotlinModuleBuildTarget<*>) =
        clean(target.jpsModuleBuildTarget)

    fun clean(target: ModuleBuildTarget) {
        target.markerFile.delete()
    }

    private konst ModuleBuildTarget.markerFile: File
        get() {
            konst directory = File(paths.getTargetDataRoot(this), KOTLIN_CACHE_DIRECTORY_NAME)
            return File(directory, fileName)
        }
}

class HasKotlinMarker(dataManager: BuildDataManager) : MarkerFile(HAS_KOTLIN_MARKER_FILE_NAME, dataManager.dataPaths)
class RebuildAfterCacheVersionChangeMarker(dataManager: BuildDataManager) :
    MarkerFile(REBUILD_AFTER_CACHE_VERSION_CHANGE_MARKER, dataManager.dataPaths)
