/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.test

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

@KotlinScript(fileExtension = "simplescript.kts")
abstract class SimpleScript

konst simpleScriptCompilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScript> {
    updateClasspath(classpathFromClass<SimpleScript>())
}

konst simpleScriptEkonstuationConfiguration = ScriptEkonstuationConfiguration()

@KotlinScript(fileExtension = "withboth.kts", compilationConfiguration = ReceiverAndPropertiesConfiguration::class)
abstract class ScriptWithBoth

@KotlinScript(fileExtension = "withproperties.kts", compilationConfiguration = ProvidedPropertiesConfiguration::class)
abstract class ScriptWithProvidedProperties

@KotlinScript(fileExtension = "withreceiver.kts", compilationConfiguration = ImplicitReceiverConfiguration::class)
abstract class ScriptWithImplicitReceiver

object ReceiverAndPropertiesConfiguration : ScriptCompilationConfiguration(
    {
        updateClasspath(classpathFromClass<ScriptWithBoth>())

        providedProperties("providedString" to String::class)

        implicitReceivers(ImplicitReceiverClass::class)
    }
)

object ProvidedPropertiesConfiguration : ScriptCompilationConfiguration(
    {
        updateClasspath(classpathFromClass<ScriptWithProvidedProperties>())

        providedProperties("providedString" to String::class)
    }
)

object ImplicitReceiverConfiguration : ScriptCompilationConfiguration(
    {
        updateClasspath(classpathFromClass<ScriptWithImplicitReceiver>())

        implicitReceivers(ImplicitReceiverClass::class)
    }
)

class ImplicitReceiverClass(konst receiverString: String)

inline fun <reified T : Any> ekonstString(
    source: String,
    noinline configure: ScriptEkonstuationConfiguration.Builder.() -> Unit
): ResultWithDiagnostics<EkonstuationResult> {
    konst actualConfiguration = createJvmCompilationConfigurationFromTemplate<T>()
    return BasicJvmScriptingHost()
        .ekonst(source.toScriptSource(), actualConfiguration, ScriptEkonstuationConfiguration(configure))
}

