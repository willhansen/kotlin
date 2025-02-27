/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.definitions

import kotlin.script.experimental.api.ScriptEkonstuationConfiguration
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.host.ScriptingHostConfiguration

class ScriptEkonstuationConfigurationFromDefinition(
    konst hostConfiguration: ScriptingHostConfiguration,
    konst scriptDefinition: KotlinScriptDefinition
) : ScriptEkonstuationConfiguration(
    {
        hostConfiguration(hostConfiguration)
    }
)
