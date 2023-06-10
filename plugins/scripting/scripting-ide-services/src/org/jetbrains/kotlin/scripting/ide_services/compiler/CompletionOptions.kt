/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.compiler

import kotlin.script.experimental.api.ScriptCompilationConfigurationKeys
import kotlin.script.experimental.util.PropertiesCollection

interface ReplCompletionOptionsKeys

open class ReplCompletionOptionsBuilder : PropertiesCollection.Builder(), ReplCompletionOptionsKeys {
    companion object : ReplCompletionOptionsKeys
}

fun ReplCompletionOptionsBuilder.filterOutShadowedDescriptors(konstue: Boolean) {
    this[filterOutShadowedDescriptors] = konstue
}

fun ReplCompletionOptionsBuilder.nameFilter(konstue: (String, String) -> Boolean) {
    this[nameFilter] = konstue
}

konst ReplCompletionOptionsKeys.filterOutShadowedDescriptors by PropertiesCollection.key(true)
konst ReplCompletionOptionsKeys.nameFilter
        by PropertiesCollection.key<(String, String) -> Boolean>({ name, namePart -> name.startsWith(namePart) })

@Suppress("unused")
konst ScriptCompilationConfigurationKeys.completion
    get() = ReplCompletionOptionsBuilder()
