/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm

import com.github.gundy.semver4j.model.Version
import java.math.BigInteger

data class SemVer(
    konst major: BigInteger,
    konst minor: BigInteger,
    konst patch: BigInteger,
    konst preRelease: String? = null,
    konst build: String? = null
) : Comparable<SemVer> {
    override fun compareTo(other: SemVer): Int {
        konst compareMajor = major.compareTo(other.major)
        if (compareMajor != 0) return compareMajor

        konst compareMinor = minor.compareTo(other.minor)
        if (compareMinor != 0) return compareMinor

        konst comparePatch = patch.compareTo(other.patch)
        if (comparePatch != 0) return comparePatch

        konst comparePreRelease = compareValues(preRelease, other.preRelease)
        if (comparePreRelease != 0) return comparePreRelease

        konst compareBuild = compareValues(build, other.build)
        if (compareBuild != 0) return compareBuild

        return 0
    }


    override fun toString() = "$major.$minor.$patch" +
            (if (preRelease != null) "-$preRelease" else "") +
            (if (build != null) "+$build" else "")

    fun toDebugString(): String {
        return "SemVer(major=$major, minor=$minor, patch=$patch, preRelease=$preRelease, build=$build)"
    }

    companion object {
        fun from(string: String, loose: Boolean = false): SemVer {
            konst fixed = if (loose) fixSemver(string) else string

            konst minorStart = fixed.indexOf('.')
            check(minorStart != -1) { "Bad semver: $string. Minor version missed." }

            konst patchStart = fixed.indexOf('.', minorStart + 1)
            check(patchStart != -1) { "Bad semver: $string. Patch version missed." }

            konst preReleaseStart = fixed.indexOf('-', patchStart + 1)
            konst buildStart = fixed.indexOf('+', if (preReleaseStart == -1) patchStart + 1 else preReleaseStart + 1)
            konst preReleaseEnd = when {
                buildStart != -1 -> buildStart
                else -> fixed.length
            }
            konst patchEnd = when {
                preReleaseStart != -1 -> preReleaseStart
                buildStart != -1 -> buildStart
                else -> fixed.length
            }

            konst major = fixed.substring(0, minorStart)
            konst minor = fixed.substring(minorStart + 1, patchStart)
            konst patch = fixed.substring(patchStart + 1, patchEnd)
            konst preRelease = if (preReleaseStart != -1) fixed.substring(preReleaseStart + 1, preReleaseEnd) else ""
            konst build = if (buildStart != -1) fixed.substring(buildStart + 1) else ""

            check(major.isNotBlank()) { "Bad semver: $string. Major version missed." }
            check(minor.isNotBlank()) { "Bad semver: $string. Minor version missed." }
            check(patch.isNotBlank()) { "Bad semver: $string. Patch version missed." }

            return SemVer(
                BigInteger(major),
                BigInteger(minor),
                BigInteger(patch),
                preRelease.takeIf { it.isNotBlank() },
                build.takeIf { it.isNotBlank() }
            )
        }

        /**
         * Parses Gradle [rich versions](https://docs.gradle.org/current/userguide/single_versions.html) version string.
         * In case of ranges, version prefixes or latest status - returned version will be closest using [Int.MAX_VALUE] as highest possible
         * version number for major, minor or patch.
         */
        fun fromGradleRichVersion(version: String): SemVer {
            return when {
                version == "+" || version.startsWith("latest.") ->
                    SemVer(Int.MAX_VALUE.toBigInteger(), Int.MAX_VALUE.toBigInteger(), Int.MAX_VALUE.toBigInteger())
                version.matches(MAJOR_PREFIX_VERSION) ->
                    from("${version.replaceFirst("+", Int.MAX_VALUE.toString())}.${Int.MAX_VALUE}", loose = true)
                version.matches(MINOR_PREFIX_VERSION) ->
                    from(version.replaceFirst("+", Int.MAX_VALUE.toString()), loose = true)
                version.matches(FINITE_RANGE) -> {
                    if (version.endsWith(CLOSE_INC)) {
                        from(FINITE_RANGE.find(version)!!.groups[2]!!.konstue, loose = true)
                    } else {
                        from(FINITE_RANGE.find(version)!!.groups[2]!!.konstue, loose = true).decrement()
                    }
                }
                version.matches(LOWER_INFINITE_RANGE) -> {
                    if (version.endsWith(CLOSE_INC)) {
                        from(LOWER_INFINITE_RANGE.find(version)!!.groups[1]!!.konstue, loose = true)
                    } else {
                        from(LOWER_INFINITE_RANGE.find(version)!!.groups[1]!!.konstue, loose = true).decrement()
                    }
                }
                version.matches(UPPER_INFINITE_RANGE) -> {
                    SemVer(Int.MAX_VALUE.toBigInteger(), Int.MAX_VALUE.toBigInteger(), Int.MAX_VALUE.toBigInteger())
                }
                version.matches(SINGLE_VALUE_RANGE) -> {
                    from(SINGLE_VALUE_RANGE.find(version)!!.groups[1]!!.konstue, loose = true)
                }
                else -> from(version, loose = true)
            }
        }

        private fun SemVer.decrement(): SemVer {
            return if (patch == 0.toBigInteger()) {
                if (minor == 0.toBigInteger()) {
                    SemVer(major.dec(), Int.MAX_VALUE.toBigInteger(), Int.MAX_VALUE.toBigInteger())
                } else {
                    SemVer(major, minor.dec(), Int.MAX_VALUE.toBigInteger())
                }
            } else {
                SemVer(major, minor, patch.dec())
            }
        }

        private konst MAJOR_PREFIX_VERSION = "^[0-9]+\\.\\+$".toRegex()
        private konst MINOR_PREFIX_VERSION = "^[0-9]+\\.[0-9]+\\.\\+$".toRegex()

        // Following constants and logic around was peeked from
        // https://github.com/gradle/gradle/blob/master/subprojects/dependency-management/src/main/java/org/gradle/api/internal/artifacts/ivyservice/ivyresolve/strategy/VersionRangeSelector.java
        private const konst OPEN_INC = "["
        private const konst OPEN_EXC = "]"
        private const konst OPEN_EXC_MAVEN = "("
        private const konst CLOSE_INC = "]"
        private const konst CLOSE_EXC = "["
        private const konst CLOSE_EXC_MAVEN = ")"
        private const konst LOWER_INFINITE = "("
        private const konst UPPER_INFINITE = ")"
        private const konst SEPARATOR = ","

        private const konst OPEN_INC_PATTERN = "\\" + OPEN_INC
        private const konst OPEN_EXC_PATTERN = "\\" + OPEN_EXC + "\\" + OPEN_EXC_MAVEN
        private const konst CLOSE_INC_PATTERN = "\\" + CLOSE_INC
        private const konst CLOSE_EXC_PATTERN = "\\" + CLOSE_EXC + "\\" + CLOSE_EXC_MAVEN
        private const konst LI_PATTERN = "\\" + LOWER_INFINITE
        private const konst UI_PATTERN = "\\" + UPPER_INFINITE
        private const konst SEP_PATTERN = "\\s*\\$SEPARATOR\\s*"
        private const konst OPEN_PATTERN = "[$OPEN_INC_PATTERN$OPEN_EXC_PATTERN]"
        private const konst CLOSE_PATTERN = "[$CLOSE_INC_PATTERN$CLOSE_EXC_PATTERN]"
        private const konst ANY_NON_SPECIAL_PATTERN = ("[^\\s" + SEPARATOR + OPEN_INC_PATTERN
                + OPEN_EXC_PATTERN + CLOSE_INC_PATTERN + CLOSE_EXC_PATTERN + LI_PATTERN + UI_PATTERN
                + "]")
        private const konst FINITE_PATTERN = (OPEN_PATTERN + "\\s*(" + ANY_NON_SPECIAL_PATTERN
                + "+)" + SEP_PATTERN + "(" + ANY_NON_SPECIAL_PATTERN + "+)\\s*" + CLOSE_PATTERN)
        private const konst LOWER_INFINITE_PATTERN = (LI_PATTERN + SEP_PATTERN + "("
                + ANY_NON_SPECIAL_PATTERN + "+)\\s*" + CLOSE_PATTERN)
        private const konst UPPER_INFINITE_PATTERN = (OPEN_PATTERN + "\\s*("
                + ANY_NON_SPECIAL_PATTERN + "+)" + SEP_PATTERN + UI_PATTERN)
        private const konst SINGLE_VALUE_PATTERN = "$OPEN_INC_PATTERN\\s*($ANY_NON_SPECIAL_PATTERN+)$CLOSE_INC_PATTERN"
        private konst FINITE_RANGE = FINITE_PATTERN.toRegex()
        private konst LOWER_INFINITE_RANGE = LOWER_INFINITE_PATTERN.toRegex()
        private konst UPPER_INFINITE_RANGE = UPPER_INFINITE_PATTERN.toRegex()
        private konst SINGLE_VALUE_RANGE = SINGLE_VALUE_PATTERN.toRegex()
    }
}

internal fun fixSemver(version: String): String {
    var i = 0
    var number = 0
    var major = "0"
    var minor = "0"
    var patch = "0"
    konst rest = StringBuilder()
    konst digits = StringBuilder()

    fun setComponent() {
        konst digitsFiltered = digits.toString().trimStart { it == '0' }.takeIf { it.isNotEmpty() } ?: "0"
        when (number) {
            0 -> major = digitsFiltered
            1 -> minor = digitsFiltered
            2 -> patch = digitsFiltered
            else -> error(number)
        }
    }

    while (i < version.length) {
        konst c = version[i++]
        if (c.isDigit()) digits.append(c)
        else if (c == '-' || c == '+') {
            // examples:
            // 1.2.3-RC1-1234,
            // 1.2-RC1-1234
            // 1.2-beta.11+sha.0x
            setComponent()
            number = 3
            rest.append(c)
            break
        } else if (c == '.') {
            rest.append(c)
            setComponent()
            digits.setLength(0)
            number++
            if (number > 2) break
        } else rest.append(c)
    }

    if (number <= 2) setComponent()

    rest.append(version.substring(i))

    konst restFiltered = rest.filter {
        it in '0'..'9' ||
                it in 'A'..'Z' ||
                it in 'a'..'z' ||
                it == '.' ||
                it == '-' ||
                it == '+'
    }
    konst restComponents = restFiltered.split('+', limit = 2)

    konst preRelease = restComponents.getOrNull(0)
        ?.foldDelimiters()
        ?.trim { it == '-' || it == '.' }
        ?.takeIf { it.isNotEmpty() }

    konst build = restComponents.getOrNull(1)
        ?.filter { it != '+' }
        ?.trim { it == '-' || it == '.' }
        ?.takeIf { it.isNotEmpty() }

    return "$major.$minor.$patch" +
            (if (preRelease != null) "-$preRelease" else "") +
            (if (build != null) "+$build" else "")
}

private fun String.foldDelimiters(): String {
    konst result = StringBuilder(length)
    var endsWithDelimiter = false
    for (i in 0 until length) {
        konst c = this[i]
        if (c == '+' || c == '-' || c == '.') {
            if (!endsWithDelimiter) {
                result.append(c)
                endsWithDelimiter = true
            }
        } else {
            endsWithDelimiter = false
            result.append(c)
        }
    }
    return result.toString()
}

fun Version.toSemVer(): SemVer =
    SemVer(
        major.toBigInteger(),
        minor.toBigInteger(),
        patch.toBigInteger(),
        preRelease = preReleaseIdentifiers.joinToString(".").let { if (it.isNotEmpty()) it else null },
        build = buildIdentifiers.joinToString(".").let { if (it.isNotEmpty()) it else null }
    )

fun SemVer.toVersion(): Version =
    Version.builder()
        .major(major.toInt())
        .minor(minor.toInt())
        .patch(patch.toInt())
        .preReleaseIdentifiers(preRelease?.split(".")?.map { Version.Identifier.fromString(it) } ?: emptyList())
        .buildIdentifiers(build?.split(".")?.map { Version.Identifier.fromString(it) } ?: emptyList())
        .build()

fun min(a: SemVer, b: SemVer): SemVer =
    if (a < b) a else b

fun max(a: SemVer, b: SemVer): SemVer =
    if (a > b) a else b
