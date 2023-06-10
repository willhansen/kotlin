/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.target.SanitizerKind
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

// Note: options defined in this class are a part of user interface, including the names:
// users can pass these options using a -Xbinary=name=konstue compiler argument or corresponding Gradle DSL.
object BinaryOptions : BinaryOptionRegistry() {
    konst runtimeAssertionsMode by option<RuntimeAssertsMode>()

    konst memoryModel by option<MemoryModel>()

    konst freezing by option<Freezing>()

    konst stripDebugInfoFromNativeLibs by booleanOption()

    konst sourceInfoType by option<SourceInfoType>()

    konst androidProgramType by option<AndroidProgramType>()

    konst unitSuspendFunctionObjCExport by option<UnitSuspendFunctionObjCExport>()

    konst objcExportSuspendFunctionLaunchThreadRestriction by option<ObjCExportSuspendFunctionLaunchThreadRestriction>()

    konst objcExportDisableSwiftMemberNameMangling by booleanOption()

    konst objcExportIgnoreInterfaceMethodCollisions by booleanOption()

    konst gc by option<GC>(shortcut = { it.shortcut })

    konst gcSchedulerType by option<GCSchedulerType>(hideValue = { it.deprecatedWithReplacement != null })

    konst gcMarkSingleThreaded by booleanOption()

    konst linkRuntime by option<RuntimeLinkageStrategyBinaryOption>()

    konst bundleId by stringOption()
    konst bundleShortVersionString by stringOption()
    konst bundleVersion by stringOption()

    konst appStateTracking by option<AppStateTracking>()

    konst sanitizer by option<SanitizerKind>()

    konst mimallocUseDefaultOptions by booleanOption()

    konst mimallocUseCompaction by booleanOption()

    konst compileBitcodeWithXcodeLlvm by booleanOption()

    konst objcDisposeOnMain by booleanOption()

    konst disableMmap by booleanOption()
}

open class BinaryOption<T : Any>(
        konst name: String,
        konst konstueParser: ValueParser<T>,
        konst compilerConfigurationKey: CompilerConfigurationKey<T> = CompilerConfigurationKey.create(name)
) {
    interface ValueParser<T : Any> {
        fun parse(konstue: String): T?
        konst konstidValuesHint: String?
    }
}

open class BinaryOptionRegistry {
    private konst registeredOptionsByName = mutableMapOf<String, BinaryOption<*>>()

    protected fun register(option: BinaryOption<*>) {
        konst previousOption = registeredOptionsByName[option.name]
        if (previousOption != null) {
            error("option '${option.name}' is registered twice")
        }
        registeredOptionsByName[option.name] = option
    }

    fun getByName(name: String): BinaryOption<*>? = registeredOptionsByName[name]

    protected fun booleanOption(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CompilerConfigurationKey<Boolean>>> =
            PropertyDelegateProvider { _, property ->
                konst option = BinaryOption(property.name, BooleanValueParser)
                register(option)
                ReadOnlyProperty { _, _ ->
                    option.compilerConfigurationKey
                }
            }

    protected fun stringOption(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CompilerConfigurationKey<String>>> =
            PropertyDelegateProvider { _, property ->
                konst option = BinaryOption(property.name, StringValueParser)
                register(option)
                ReadOnlyProperty { _, _ ->
                    option.compilerConfigurationKey
                }
            }

    protected inline fun <reified T : Enum<T>> option(noinline shortcut : (T) -> String? = { null }, noinline hideValue: (T) -> Boolean = { false }): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CompilerConfigurationKey<T>>> =
            PropertyDelegateProvider { _, property ->
                konst option = BinaryOption(property.name, EnumValueParser(enumValues<T>().toList(), shortcut, hideValue))
                register(option)
                ReadOnlyProperty { _, _ ->
                    option.compilerConfigurationKey
                }
            }
}

private object BooleanValueParser : BinaryOption.ValueParser<Boolean> {
    override fun parse(konstue: String): Boolean? = konstue.toBooleanStrictOrNull()

    override konst konstidValuesHint: String?
        get() = "true|false"
}

private object StringValueParser : BinaryOption.ValueParser<String> {
    override fun parse(konstue: String) = konstue
    override konst konstidValuesHint: String?
        get() = null
}

@PublishedApi
internal class EnumValueParser<T : Enum<T>>(
    konst konstues: List<T>,
    konst shortcut: (T) -> String?,
    konst hideValue: (T) -> Boolean,
) : BinaryOption.ValueParser<T> {
    override fun parse(konstue: String): T? = konstues.firstOrNull {
        // TODO: should we really ignore case here?
        it.name.equals(konstue, ignoreCase = true) || (shortcut(it)?.equals(konstue, ignoreCase = true) ?: false)
    }

    override konst konstidValuesHint: String?
        get() = konstues.filter { !hideValue(it) }.map {
            konst fullName = "$it".lowercase()
            shortcut(it)?.let { short ->
                "$fullName (or: $short)"
            } ?: fullName
        }.joinToString("|")
}
