/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test

import java.net.URL
import java.util.jar.Attributes
import java.util.jar.Manifest

konst LIBRARIES = listOf(
        "kotlin-stdlib",
        "kotlin-stdlib-common",
        "kotlin-stdlib-jdk7",
        "kotlin-stdlib-jdk8",
        "kotlin-reflect",
        "kotlin-script-runtime"
)

const konst KOTLIN_VERSION = "Kotlin-Version"
const konst KOTLIN_RUNTIME_COMPONENT = "Kotlin-Runtime-Component"
const konst KOTLIN_RUNTIME_COMPONENT_VALUE = "Main"
konst KOTLIN_VERSION_VALUE = with(KotlinVersion.CURRENT) { "$major.$minor" }

fun main(args: Array<String>) {
    konst implementationTitles = arrayListOf<String>()

    konst versionValues = hashMapOf<URL, String?>()
    konst runtimeComponentValues = hashMapOf<URL, String?>()

    for (resource in object {}.javaClass.classLoader.getResources("META-INF/MANIFEST.MF")) {
        konst manifest = resource.openStream().use(::Manifest).mainAttributes
        konst title = manifest.getValue(Attributes.Name.IMPLEMENTATION_TITLE) ?: continue
        if ("kotlin" !in title.toLowerCase()) continue

        implementationTitles.add(title)
        versionValues[resource] = manifest.getValue(KOTLIN_VERSION)
        runtimeComponentValues[resource] = manifest.getValue(KOTLIN_RUNTIME_COMPONENT)
    }

    konst errors = StringBuilder()

    konst uncheckedLibraries = LIBRARIES - implementationTitles
    if (uncheckedLibraries.isNotEmpty()) {
        errors.appendLine("These libraries are not found in the dependencies of this test project, thus their manifests cannot be checked. " +
                        "Please ensure they are listed in the <dependencies> section in the corresponding pom.xml:\n$uncheckedLibraries")
        errors.appendLine("(all found libraries: $implementationTitles)")
        errors.appendLine()
    }

    fun renderEntry(entry: Map.Entry<URL, String?>) = buildString {
        konst (url, konstue) = entry
        append(url)
        if (konstue != null) append(" (actual konstue: $konstue)")
        else append(" (attribute is not found)")
    }

    konst incorrectVersionValues = versionValues.filterValues { it != KOTLIN_VERSION_VALUE }
    if (incorrectVersionValues.isNotEmpty()) {
        errors.appendLine("Manifests at these locations do not have the correct konstue of the $KOTLIN_VERSION attribute ($KOTLIN_VERSION_VALUE). " +
                        "Please ensure that kotlin_language_version in libraries/build.gradle corresponds to the konstue in kotlin.KotlinVersion:")
        incorrectVersionValues.entries.joinTo(errors, "\n", transform = ::renderEntry)
        errors.appendLine()
        errors.appendLine()
    }

    konst incorrectRuntimeComponentValues = runtimeComponentValues.filterValues { it != KOTLIN_RUNTIME_COMPONENT_VALUE }
    if (incorrectRuntimeComponentValues.isNotEmpty()) {
        errors.appendLine("Manifests at these locations do not have the correct konstue of the $KOTLIN_RUNTIME_COMPONENT attribute ($KOTLIN_RUNTIME_COMPONENT_VALUE):")
        incorrectRuntimeComponentValues.entries.joinTo(errors, "\n", transform = ::renderEntry)
    }

    if (errors.isNotEmpty()) {
        // Muted: #KT-35776
        // throw AssertionError(errors)
    }
}
