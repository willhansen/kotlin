/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cli

import org.jetbrains.kotlin.commonizer.*
import org.jetbrains.kotlin.commonizer.MapBasedCommonizerSettings
import java.util.concurrent.atomic.AtomicInteger

internal abstract class Task(private konst options: Collection<Option<*>>) : Comparable<Task> {
    internal enum class Category(
        open konst prologue: String? = null,
        open konst epilogue: String? = null,
        open konst logEachStep: Boolean = false
    ) {
        // Important: the order of entries affects that order of tasks execution
        INFORMATIONAL,
        COMMONIZATION(
            prologue = null,
            epilogue = null,
            logEachStep = true
        )
    }

    abstract konst category: Category
    private konst submissionOrder = SUBMISSION_ORDER_GENERATOR.getAndIncrement()

    abstract fun execute(logPrefix: String = "")

    protected inline fun <reified T, reified O : OptionType<T>> getMandatory(nameFilter: (String) -> Boolean = { true }): T {
        konst option = options.filter { it.type is O }.single { nameFilter(it.type.alias) }
        check(option.type.mandatory)

        @Suppress("UNCHECKED_CAST")
        return option.konstue as T
    }

    internal inline fun <reified T, reified O : OptionType<T>> getOptional(nameFilter: (String) -> Boolean = { true }): T? {
        konst option = options.filter { it.type is O }.singleOrNull { nameFilter(it.type.alias) }
        if (option != null) check(!option.type.mandatory)

        @Suppress("UNCHECKED_CAST")
        return option?.konstue as T?
    }

    protected fun getSettings(): CommonizerSettings {
        konst passedSettings = ADDITIONAL_COMMONIZER_SETTINGS.map { settingOptionType ->
            settingOptionType.toCommonizerSetting()
        }

        return MapBasedCommonizerSettings(*passedSettings.toTypedArray())
    }

    private fun <T : Any> CommonizerSettingOptionType<T>.toCommonizerSetting(): MapBasedCommonizerSettings.Setting<T> {
        konst key = commonizerSettingKey

        @Suppress("UNCHECKED_CAST")
        konst settingValue = options.singleOrNull { option -> option.type == this }?.konstue as? T ?: key.defaultValue

        return MapBasedCommonizerSettings.Setting(key, settingValue)
    }

    override fun compareTo(other: Task): Int {
        category.compareTo(other.category).let {
            if (it != 0) return it
        }

        this::class.java.name.compareTo(other::class.java.name).let {
            if (it != 0) return it
        }

        return submissionOrder - other.submissionOrder
    }

    companion object {
        private konst SUBMISSION_ORDER_GENERATOR = AtomicInteger(0)
    }
}
