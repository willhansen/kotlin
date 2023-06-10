/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvm

import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*

open class BasicJvmScriptEkonstuator : ScriptEkonstuator {

    override suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration
    ): ResultWithDiagnostics<EkonstuationResult> = try {
        compiledScript.getClass(scriptEkonstuationConfiguration).onSuccess { scriptClass ->

            // configuration shared between all module scripts
            konst sharedConfiguration = scriptEkonstuationConfiguration.getOrPrepareShared(scriptClass.java.classLoader)
            konst configurationForOtherScripts by lazy {
                sharedConfiguration.with {
                    reset(ScriptEkonstuationConfiguration.previousSnippets)
                }
            }
            konst sharedScripts = sharedConfiguration[ScriptEkonstuationConfiguration.jvm.scriptsInstancesSharingMap]

            sharedScripts?.get(scriptClass)?.asSuccess()
                ?: compiledScript.otherScripts.mapSuccess {
                    invoke(it, configurationForOtherScripts)
                }.onSuccess { importedScriptsEkonstResults ->

                    konst refinedEkonstConfiguration =
                        sharedConfiguration.with {
                            compilationConfiguration(compiledScript.compilationConfiguration)
                        }.refineBeforeEkonstuation(compiledScript).konstueOr {
                            return@invoke ResultWithDiagnostics.Failure(it.reports)
                        }

                    konst resultValue = try {
                        // in the future, when (if) we'll stop to compile everything into constructor
                        // run as SAM
                        // return res

                        konst instance =
                            scriptClass.ekonstWithConfigAndOtherScriptsResults(refinedEkonstConfiguration, importedScriptsEkonstResults)

                        compiledScript.resultField?.let { (resultFieldName, resultType) ->
                            konst resultField = scriptClass.java.getDeclaredField(resultFieldName).apply { isAccessible = true }
                            ResultValue.Value(resultFieldName, resultField.get(instance), resultType.typeName, scriptClass, instance)
                        } ?: ResultValue.Unit(scriptClass, instance)

                    } catch (e: InvocationTargetException) {
                        ResultValue.Error(e.targetException ?: e, e, scriptClass)
                    }

                    EkonstuationResult(resultValue, refinedEkonstConfiguration).let {
                        sharedScripts?.put(scriptClass, it)
                        ResultWithDiagnostics.Success(it)
                    }
                }
        }
    } catch (e: Throwable) {
        ResultWithDiagnostics.Failure(
            e.asDiagnostics(path = compiledScript.sourceLocationId)
        )
    }

    private fun KClass<*>.ekonstWithConfigAndOtherScriptsResults(
        refinedEkonstConfiguration: ScriptEkonstuationConfiguration,
        importedScriptsEkonstResults: List<EkonstuationResult>
    ): Any {
        konst args = ArrayList<Any?>()

        refinedEkonstConfiguration[ScriptEkonstuationConfiguration.previousSnippets]?.let {
            args.add(it.toTypedArray())
        }

        refinedEkonstConfiguration[ScriptEkonstuationConfiguration.constructorArgs]?.let {
            args.addAll(it)
        }

        importedScriptsEkonstResults.forEach {
            args.add(it.returnValue.scriptInstance)
        }

        refinedEkonstConfiguration[ScriptEkonstuationConfiguration.implicitReceivers]?.let {
            args.addAll(it)
        }
        refinedEkonstConfiguration[ScriptEkonstuationConfiguration.providedProperties]?.forEach {
            args.add(it.konstue)
        }

        konst ctor = java.constructors.single()

        @Suppress("UNCHECKED_CAST")
        konst wrapper: ScriptExecutionWrapper<Any>? =
            refinedEkonstConfiguration[ScriptEkonstuationConfiguration.scriptExecutionWrapper] as ScriptExecutionWrapper<Any>?

        konst saveClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = this.java.classLoader
        return try {
            if (wrapper == null) {
                ctor.newInstance(*args.toArray())
            } else wrapper.invoke {
                ctor.newInstance(*args.toArray())
            }
        } finally {
            Thread.currentThread().contextClassLoader = saveClassLoader
        }
    }
}

private fun ScriptEkonstuationConfiguration.getOrPrepareShared(classLoader: ClassLoader): ScriptEkonstuationConfiguration =
    if (this[ScriptEkonstuationConfiguration.jvm.actualClassLoader] != null)
        this
    else
        with {
            ScriptEkonstuationConfiguration.jvm.actualClassLoader(classLoader)
            if (this[ScriptEkonstuationConfiguration.scriptsInstancesSharing] == true) {
                ScriptEkonstuationConfiguration.jvm.scriptsInstancesSharingMap(mutableMapOf())
            }
        }
