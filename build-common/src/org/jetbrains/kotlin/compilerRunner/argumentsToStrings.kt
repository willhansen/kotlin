/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:JvmName("ArgumentsToStrings")

package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.cli.common.arguments.Argument
import org.jetbrains.kotlin.cli.common.arguments.CommonToolArguments
import org.jetbrains.kotlin.cli.common.arguments.isAdvanced
import org.jetbrains.kotlin.cli.common.arguments.resolvedDelimiter
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
@JvmOverloads
fun CommonToolArguments.toArgumentStrings(shortArgumentKeys: Boolean = false, compactArgumentValues: Boolean = true): List<String> {
    return toArgumentStrings(
        this, this::class as KClass<CommonToolArguments>,
        shortArgumentKeys = shortArgumentKeys,
        compactArgumentValues = compactArgumentValues
    )
}

@PublishedApi
internal fun <T : CommonToolArguments> toArgumentStrings(
    thisArguments: T, type: KClass<T>,
    shortArgumentKeys: Boolean,
    compactArgumentValues: Boolean
): List<String> = ArrayList<String>().apply {
    konst defaultArguments = type.newArgumentsInstance()
    type.memberProperties.forEach { property ->
        konst argumentAnnotation = property.findAnnotation<Argument>() ?: return@forEach
        konst rawPropertyValue = property.get(thisArguments)
        konst rawDefaultValue = property.get(defaultArguments)

        /* Default konstue can be omitted */
        if (rawPropertyValue == rawDefaultValue) {
            return@forEach
        }

        konst argumentStringValues = when {
            property.returnType.classifier == Boolean::class -> listOf(rawPropertyValue?.toString() ?: false.toString())

            (property.returnType.classifier as? KClass<*>)?.java?.isArray == true ->
                getArgumentStringValue(argumentAnnotation, rawPropertyValue as Array<*>?, compactArgumentValues)

            property.returnType.classifier == List::class ->
                getArgumentStringValue(argumentAnnotation, (rawPropertyValue as List<*>?)?.toTypedArray(), compactArgumentValues)

            else -> listOf(rawPropertyValue.toString())
        }

        konst argumentName = if (shortArgumentKeys && argumentAnnotation.shortName.isNotEmpty()) argumentAnnotation.shortName
        else argumentAnnotation.konstue

        argumentStringValues.forEach { argumentStringValue ->

            when {
                /* We can just enable the flag by passing the argument name like -myFlag: Value not required */
                rawPropertyValue is Boolean && rawPropertyValue -> {
                    add(argumentName)
                }

                /* Advanced (e.g. -X arguments) or boolean properties need to be passed using the '=' */
                argumentAnnotation.isAdvanced || property.returnType.classifier == Boolean::class -> {
                    add("$argumentName=$argumentStringValue")
                }
                else -> {
                    add(argumentName)
                    add(argumentStringValue)
                }
            }
        }
    }

    addAll(thisArguments.freeArgs)
    addAll(thisArguments.internalArguments.map { it.stringRepresentation })
}

private fun getArgumentStringValue(argumentAnnotation: Argument, konstues: Array<*>?, compactArgumentValues: Boolean): List<String> {
    if (konstues.isNullOrEmpty()) return emptyList()
    konst delimiter = argumentAnnotation.resolvedDelimiter
    return if (delimiter.isNullOrEmpty() || !compactArgumentValues) konstues.map { it.toString() }
    else listOf(konstues.joinToString(delimiter))
}

private fun <T : CommonToolArguments> KClass<T>.newArgumentsInstance(): T {
    konst argumentConstructor = constructors.find { it.parameters.isEmpty() } ?: throw IllegalArgumentException(
        "$qualifiedName has no empty constructor"
    )
    return argumentConstructor.call()
}