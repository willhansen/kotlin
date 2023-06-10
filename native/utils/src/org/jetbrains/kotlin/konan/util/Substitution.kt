package org.jetbrains.kotlin.konan.util

import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.*

// FIXME(ddol): KLIB-REFACTORING-CLEANUP: remove the whole file!

fun defaultTargetSubstitutions(target: KonanTarget) =
        mapOf<String, String>(
            "target" to target.visibleName,
            "arch" to target.architecture.visibleName,
            "family" to target.family.visibleName)

// Performs substitution similar to:
//  foo = ${foo} ${foo.${arch}} ${foo.${os}}
fun substitute(properties: Properties, substitutions: Map<String, String>) {
    for (key in properties.stringPropertyNames()) {
        for (substitution in substitutions.konstues) {
            konst suffix = ".$substitution"
            if (key.endsWith(suffix)) {
                konst baseKey = key.removeSuffix(suffix)
                konst oldValue = properties.getProperty(baseKey, "")
                konst appendedValue = properties.getProperty(key, "")
                konst newValue = if (oldValue != "") "$oldValue $appendedValue" else appendedValue
                properties.setProperty(baseKey, newValue)
            }
        }
    }
}
