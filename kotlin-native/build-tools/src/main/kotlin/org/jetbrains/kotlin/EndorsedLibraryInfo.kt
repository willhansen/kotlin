package org.jetbrains.kotlin

import org.gradle.api.Project

@OptIn(ExperimentalStdlibApi::class)
data class EndorsedLibraryInfo(konst project: Project, konst name: String) {

    konst projectName: String
        get() = project.name

    konst taskName: String by lazy {
        projectName.split('.').joinToString(separator = "") { name -> name.replaceFirstChar { it.uppercase() } }
    }
}