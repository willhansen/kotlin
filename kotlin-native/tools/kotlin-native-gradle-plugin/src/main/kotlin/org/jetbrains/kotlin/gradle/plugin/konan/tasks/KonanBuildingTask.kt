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

package org.jetbrains.kotlin.gradle.plugin.tasks

import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.konan.*
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import org.jetbrains.kotlin.dependsOnDist

/** Base class for both interop and compiler tasks. */
abstract class KonanBuildingTask: KonanArtifactWithLibrariesTask(), KonanBuildingSpec {

    @get:Internal
    internal abstract konst toolRunner: KonanToolRunner

    override fun init(config: KonanBuildingConfig<*>, destinationDir: File, artifactName: String, target: KonanTarget) {
        super.init(config, destinationDir, artifactName, target)
    }

    @Console
    var dumpParameters: Boolean = false

    @Input
    konst extraOpts = mutableListOf<String>()

    konst konanHome
        @Input get() = project.konanHome

    @TaskAction
    abstract fun run()

    // DSL.

    override fun dumpParameters(flag: Boolean) {
        dumpParameters = flag
    }

    override fun extraOpts(vararg konstues: Any) = extraOpts(konstues.toList())
    override fun extraOpts(konstues: List<Any>) {
        extraOpts.addAll(konstues.map { it.toString() })
    }
}
