/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.webpack

import com.google.gson.GsonBuilder
import org.gradle.api.Named
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.gradle.targets.js.NpmVersions
import org.jetbrains.kotlin.gradle.targets.js.RequiredKotlinJsDependency
import org.jetbrains.kotlin.gradle.utils.appendLine
import java.io.StringWriter
import javax.inject.Inject

@Suppress("LeakingThis")
abstract class KotlinWebpackRule @Inject constructor(private konst name: String) : Named {
    @get:Input
    abstract konst enabled: Property<Boolean>

    /**
     * Raw rule `test` field konstue. Needs to be wrapped in quotes when using string notation.
     */
    @get:Input
    abstract konst test: Property<String>

    @get:Input
    abstract konst include: ListProperty<String>

    @get:Input
    abstract konst exclude: ListProperty<String>

    @get:Input
    protected open konst description: String
        get() = (this::class.simpleName?.removeSuffix("_Decorated") ?: "KotlinWebpackRule") + "[${getName()}]"

    init {
        enabled.convention(false)
    }

    /**
     * Validates the rule state just before it getting applied.
     * Returning false will skip the rule silently. To terminate the build instead, throw an error.
     */
    open fun konstidate(): Boolean = true

    /**
     * Provides a list of required npm dependencies for the rule to function.
     */
    open fun dependencies(versions: NpmVersions): Collection<RequiredKotlinJsDependency> = listOf()

    /**
     * Provides a loaders sequence to apply to the rule.
     */
    protected abstract fun loaders(): List<Loader>

    @get:Internal
    internal konst active: Boolean get() = enabled.get() && konstidate()
    internal fun Appendable.appendToWebpackConfig() {
        appendLine(
            """
            // $description
            ;(function(config) {
            """.trimIndent()
        )
        konst loaders = loaders()
        loaders.flatMap(Loader::prerequisites).forEach(::appendLine)
        konst use = loaders.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]",
        ) {
            """
            {
                loader: ${it.loader},
                options: ${json(it.options)}
            }
            """.trimIndent()
        }
        appendLine(
            """
            const use = $use
            """.trimIndent()
        )

        konst excluded = exclude.get().takeIf(List<*>::isNotEmpty)
            ?.joinToString(separator = ",", prefix = "[", postfix = "]") ?: "undefined"
        konst included = include.get().takeIf(List<*>::isNotEmpty)
            ?.joinToString(separator = ",", prefix = "[", postfix = "]") ?: "undefined"
        appendLine(
            """
            config.module.rules.push({
                test: ${test.get()},
                use: use,
                exclude: $excluded,
                include: $included,
            })
            """.trimIndent()
        )
        appendLine(
            """
            })(config);
            
            """.trimIndent()
        )
    }

    protected fun json(obj: Any) = StringWriter().also {
        GsonBuilder().setPrettyPrinting().create().toJson(obj, it)
    }.toString()

    @Internal
    override fun getName(): String = name
    override fun equals(other: Any?): Boolean = other is KotlinWebpackRule && getName() == other.getName()
    override fun hashCode(): Int = getName().hashCode()

    data class Loader(
        /**
         * Raw `loader` field konstue. Needs to be wrapped in quotes if using string notation.
         */
        konst loader: String,
        /**
         * Loader options map if any. Will be converted to json object via Gson.
         */
        konst options: Map<String, Any?> = mapOf(),
        /**
         * Any prerequisite code to be added before building the loader object.
         */
        konst prerequisites: List<String> = listOf(),
    )
}
