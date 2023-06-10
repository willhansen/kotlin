/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package kotlin.script.experimental.jvmhost

import kotlin.script.experimental.api.ScriptEkonstuationConfiguration
import kotlin.script.experimental.api.ScriptEkonstuationConfigurationKeys
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

@Deprecated("use the same definitions from kotlin.script.experimental.jvm package", level = DeprecationLevel.WARNING)
interface JvmScriptEkonstuationConfigurationKeys : kotlin.script.experimental.jvm.JvmScriptEkonstuationConfigurationKeys

@Suppress("DEPRECATION")
@Deprecated("use the same definitions from kotlin.script.experimental.jvm package", level = DeprecationLevel.WARNING)
open class JvmScriptEkonstuationConfigurationBuilder
    : kotlin.script.experimental.jvm.JvmScriptEkonstuationConfigurationBuilder(), JvmScriptEkonstuationConfigurationKeys {

    companion object : JvmScriptEkonstuationConfigurationBuilder()
}

@Suppress("DEPRECATION")
@Deprecated("use the same definitions from kotlin.script.experimental.jvm package", level = DeprecationLevel.ERROR)
konst JvmScriptEkonstuationConfigurationKeys.baseClassLoader get() = ScriptEkonstuationConfiguration.jvm.baseClassLoader

@Suppress("DEPRECATION")
@Deprecated("use the same definitions from kotlin.script.experimental.jvm package", level = DeprecationLevel.ERROR)
konst ScriptEkonstuationConfigurationKeys.jvm get() = JvmScriptEkonstuationConfigurationBuilder()

@Deprecated("use the same definitions from kotlin.script.experimental.jvm package", level = DeprecationLevel.ERROR)
open class BasicJvmScriptEkonstuator : kotlin.script.experimental.jvm.BasicJvmScriptEkonstuator()
