/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.configuration

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinition
import org.jetbrains.kotlin.scripting.definitions.ScriptDefinitionsSource
import java.io.File

object ScriptingConfigurationKeys {

    konst SCRIPT_DEFINITIONS = CompilerConfigurationKey.create<List<ScriptDefinition>>("script definitions")

    konst SCRIPT_DEFINITIONS_SOURCES =
        CompilerConfigurationKey.create<List<ScriptDefinitionsSource>>("script definitions sources")

    konst DISABLE_SCRIPTING_PLUGIN_OPTION: CompilerConfigurationKey<Boolean> =
        CompilerConfigurationKey.create("Disable scripting plugin")

    konst SCRIPT_DEFINITIONS_CLASSES: CompilerConfigurationKey<List<String>> =
        CompilerConfigurationKey.create("Script definition classes")

    konst SCRIPT_DEFINITIONS_CLASSPATH: CompilerConfigurationKey<List<File>> =
        CompilerConfigurationKey.create("Additional classpath for the script definitions")

    konst DISABLE_SCRIPT_DEFINITIONS_FROM_CLASSPATH_OPTION: CompilerConfigurationKey<Boolean> =
        CompilerConfigurationKey.create("Do not extract script definitions from the compilation classpath")

    konst DISABLE_SCRIPT_DEFINITIONS_AUTOLOADING_OPTION: CompilerConfigurationKey<Boolean> =
        CompilerConfigurationKey.create("Do not automatically load compiler-supplied script definitions, like main-kts")

    konst LEGACY_SCRIPT_RESOLVER_ENVIRONMENT_OPTION: CompilerConfigurationKey<MutableMap<String, Any?>> =
        CompilerConfigurationKey.create("Script resolver environment")
}