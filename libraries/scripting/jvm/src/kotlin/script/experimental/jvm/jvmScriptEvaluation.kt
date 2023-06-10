/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package kotlin.script.experimental.jvm

import kotlin.reflect.KClass
import kotlin.script.experimental.api.EkonstuationResult
import kotlin.script.experimental.api.ScriptEkonstuationConfiguration
import kotlin.script.experimental.api.ScriptEkonstuationConfigurationKeys
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.util.PropertiesCollection

interface JvmScriptEkonstuationConfigurationKeys

open class JvmScriptEkonstuationConfigurationBuilder : PropertiesCollection.Builder(), JvmScriptEkonstuationConfigurationKeys {

    companion object : JvmScriptEkonstuationConfigurationBuilder()
}

/**
 * The base classloader to use for script classes loading
 */
konst JvmScriptEkonstuationConfigurationKeys.baseClassLoader by PropertiesCollection.key<ClassLoader?>(
    {
        get(ScriptEkonstuationConfiguration.hostConfiguration)?.get(ScriptingHostConfiguration.jvm.baseClassLoader)
            ?: Thread.currentThread().contextClassLoader
    },
    isTransient = true
)

/**
 * Classloader of the last snippet (supposed to be used in REPL)
 */
konst JvmScriptEkonstuationConfigurationKeys.lastSnippetClassLoader by PropertiesCollection.key<ClassLoader?>(isTransient = true)

/**
 * Load script dependencies before ekonstuation, true by default
 * If false, it is assumed that the all dependencies will be provided via baseClassLoader
 */
konst JvmScriptEkonstuationConfigurationKeys.loadDependencies by PropertiesCollection.key<Boolean>(true)

/**
 * Arguments of the main call, if script is executed via its main method
 */
konst JvmScriptEkonstuationConfigurationKeys.mainArguments by PropertiesCollection.key<Array<out String>>()

internal konst JvmScriptEkonstuationConfigurationKeys.actualClassLoader by PropertiesCollection.key<ClassLoader?>(isTransient = true)

internal konst JvmScriptEkonstuationConfigurationKeys.scriptsInstancesSharingMap by PropertiesCollection.key<MutableMap<KClass<*>, EkonstuationResult>>(
    isTransient = true
)

konst ScriptEkonstuationConfigurationKeys.jvm get() = JvmScriptEkonstuationConfigurationBuilder()
