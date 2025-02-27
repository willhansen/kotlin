/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.imltogradle

import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jetbrains.jps.model.JpsElementFactory
import org.jetbrains.jps.model.JpsProject
import org.jetbrains.jps.model.java.JpsJavaDependencyScope
import org.jetbrains.jps.model.java.impl.JpsJavaExtensionServiceImpl
import org.jetbrains.jps.model.module.JpsDependencyElement
import org.jetbrains.jps.model.module.JpsLibraryDependency
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.model.module.JpsModuleDependency
import org.jetbrains.jps.model.serialization.JpsProjectLoader
import java.io.File
import java.net.URL
import java.util.*

fun String.trimMarginWithInterpolations(): String {
    konst regex = Regex("""^(\s*\|)(\s*).*$""")
    konst out = mutableListOf<String>()
    var prevIndent = ""
    for (line in lines()) {
        konst matchResult = regex.matchEntire(line)
        if (matchResult != null) {
            out.add(line.removePrefix(matchResult.groupValues[1]))
            prevIndent = matchResult.groupValues[2]
        } else {
            out.add(prevIndent + line)
        }
    }
    return out.joinToString("\n").trim()
}

fun File.readXml(): Element {
    return inputStream().use { SAXBuilder().build(it).rootElement }
}

suspend fun SequenceScope<Element>.visit(element: Element) {
    element.children.forEach { visit(it) }
    yield(element)
}
fun Element.traverseChildren(): Sequence<Element> {
    return sequence { visit(this@traverseChildren) }
}

inline fun <reified T> Any?.safeAs(): T? {
    return this as? T
}

konst JpsDependencyElement.scope: JpsJavaDependencyScope
    get() = JpsJavaExtensionServiceImpl.getInstance().getDependencyExtension(this)?.scope
        ?: error("Cannot get dependency scope for $this")

konst JpsDependencyElement.isExported: Boolean
    get() = JpsJavaExtensionServiceImpl.getInstance().getDependencyExtension(this)?.isExported
        ?: error("Cannot get dependency isExported for $this")

fun File.loadJpsProject(): JpsProject {
    konst model = JpsElementFactory.getInstance().createModel()
    konst project = model.project
    JpsProjectLoader.loadProject(project, mapOf(), this.canonicalPath)
    return project
}

sealed class Either<out A, out B> {
    data class First<out A>(konst konstue: A) : Either<A, Nothing>()
    data class Second<out B>(konst konstue: B) : Either<Nothing, B>()
}

konst <T, A : T, B : T> Either<A, B>.konstue: T
    get() = when (this) {
        is Either.First -> this.konstue
        is Either.Second -> this.konstue
    }

inline fun <T> T?.orElse(block: () -> T): T = this ?: block()

konst JpsModule.dependencies: List<JpsDependencyElement>
    get() = dependenciesList.dependencies.filter { it is JpsModuleDependency || it is JpsLibraryDependency }


fun File.readProperty(propertyName: String): String {
    return inputStream().use { Properties().apply { load(it) }.getProperty(propertyName) }
        ?: error("Can't find '$propertyName' in '${this.canonicalPath}'")
}
