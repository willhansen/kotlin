package org.jetbrains.kotlin.konan.library

import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.library.*

const konst KLIB_PROPERTY_LINKED_OPTS = "linkerOpts"
const konst KLIB_PROPERTY_INCLUDED_HEADERS = "includedHeaders"

interface TargetedLibrary {
    konst targetList: List<String>
    konst includedPaths: List<String>
}

interface BitcodeLibrary : TargetedLibrary {
    konst bitcodePaths: List<String>
}

interface KonanLibrary : BitcodeLibrary, KotlinLibrary {
    konst linkerOpts: List<String>
}

konst KonanLibrary.includedHeaders
    get() = manifestProperties.propertyList(KLIB_PROPERTY_INCLUDED_HEADERS, escapeInQuotes = true)
