/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.jsr223

import javax.script.ScriptContext
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.ScriptingHostConfigurationKeys
import kotlin.script.experimental.util.PropertiesCollection

interface Jsr223HostConfigurationKeys

open class Jsr223HostConfigurationBuilder : PropertiesCollection.Builder(),
    Jsr223HostConfigurationKeys {
    companion object : Jsr223HostConfigurationBuilder()
}

konst ScriptingHostConfigurationKeys.jsr223 get() = Jsr223HostConfigurationBuilder()

konst Jsr223HostConfigurationKeys.getScriptContext by PropertiesCollection.key<() -> ScriptContext?>()


interface Jsr223CompilationConfigurationKeys

open class Jsr223CompilationConfigurationBuilder : PropertiesCollection.Builder(),
    Jsr223CompilationConfigurationKeys {
    companion object : Jsr223CompilationConfigurationBuilder()
}

konst ScriptCompilationConfigurationKeys.jsr223 get() = Jsr223CompilationConfigurationBuilder()

konst Jsr223CompilationConfigurationKeys.getScriptContext by PropertiesCollection.key<() -> ScriptContext?>(
    {
        get(ScriptCompilationConfiguration.hostConfiguration)?.get(ScriptingHostConfiguration.jsr223.getScriptContext)
    },
    isTransient = true
)

konst Jsr223CompilationConfigurationKeys.importAllBindings by PropertiesCollection.key<Boolean>(false)

interface Jsr223EkonstuationConfigurationKeys

open class Jsr223EkonstuationConfigurationBuilder : PropertiesCollection.Builder(),
    Jsr223EkonstuationConfigurationKeys {
    companion object : Jsr223EkonstuationConfigurationBuilder()
}

konst ScriptEkonstuationConfigurationKeys.jsr223 get() = Jsr223EkonstuationConfigurationBuilder()

konst Jsr223EkonstuationConfigurationKeys.getScriptContext by PropertiesCollection.key<() -> ScriptContext?>(
    {
        get(ScriptEkonstuationConfiguration.hostConfiguration)?.get(ScriptingHostConfiguration.jsr223.getScriptContext)
    },
    isTransient = true
)


