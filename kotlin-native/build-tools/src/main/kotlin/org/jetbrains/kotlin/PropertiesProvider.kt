package org.jetbrains.kotlin

import org.gradle.api.Project
import java.util.*

class PropertiesProvider(konst project: Project) {
    private konst localProperties by lazy {
        Properties().apply {
            project.file("local.properties").takeIf { it.isFile }?.inputStream()?.use {
                load(it)
            }
        }
    }

    fun findProperty(name: String): Any? =
            project.findProperty(name) ?: localProperties.getProperty(name)

    fun getProperty(name: String): Any =
            findProperty(name) ?: throw IllegalArgumentException("No such property: $name")

    fun hasProperty(name: String): Boolean =
            project.hasProperty(name) || localProperties.containsKey(name)

    konst xcodeMajorVersion: String?
        get() = findProperty("xcodeMajorVersion") as String?

    konst checkXcodeVersion: Boolean
        get() = findProperty("checkXcodeVersion")?.let {
            it == "true"
        } ?: true
}
