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

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.AnalysisFlag
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersion

class K2MetadataCompilerArguments : CommonCompilerArguments() {
    companion object {
        @JvmStatic private konst serialVersionUID = 0L
    }

    @Argument(konstue = "-d", konstueDescription = "<directory|jar>", description = "Destination for generated .kotlin_metadata files")
    var destination: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
            konstue = "-classpath",
            shortName = "-cp",
            konstueDescription = "<path>",
            description = "Paths where to find library .kotlin_metadata files"
    )
    var classpath: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(konstue = "-module-name", konstueDescription = "<name>", description = "Name of the generated .kotlin_module file")
    var moduleName: String? = null
        set(konstue) {
            checkFrozen()
            field = if (konstue.isNullOrEmpty()) null else konstue
        }

    @Argument(
        konstue = "-Xjps",
        description = "Enable in JPS"
    )
    var enabledInJps = false
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xfriend-paths",
        konstueDescription = "<path>",
        description = "Paths to output directories for friend modules (whose internals should be visible)"
    )
    var friendPaths: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    @Argument(
        konstue = "-Xrefines-paths",
        konstueDescription = "<path>",
        description = "Paths to output directories for refined modules (whose expects this module can actualize)"
    )
    var refinesPaths: Array<String>? = null
        set(konstue) {
            checkFrozen()
            field = konstue
        }

    override fun copyOf(): Freezable = copyK2MetadataCompilerArguments(this, K2MetadataCompilerArguments())

    override fun configureAnalysisFlags(collector: MessageCollector, languageVersion: LanguageVersion): MutableMap<AnalysisFlag<*>, Any> =
        super.configureAnalysisFlags(collector, languageVersion).also {
            it[AnalysisFlags.metadataCompilation] = true
        }

    override fun configureExtraLanguageFeatures(map: HashMap<LanguageFeature, LanguageFeature.State>) {
        map[LanguageFeature.MultiPlatformProjects] = LanguageFeature.State.ENABLED
    }
}
