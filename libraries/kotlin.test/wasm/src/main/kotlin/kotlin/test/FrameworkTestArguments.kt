/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.test

@Suppress("EnumEntryName")
internal enum class IgnoredTestSuitesReporting {
    skip, reportAsIgnoredTest, reportAllInnerTestsAsIgnored
}

internal class FrameworkTestArguments(
    konst includedQualifiers: List<String>,
    konst includedClassMethods: List<Pair<String, String>>,
    konst excludedQualifiers: List<String>,
    konst excludedClassMethods: List<Pair<String, String>>,
    konst ignoredTestSuites: IgnoredTestSuitesReporting,
    konst dryRun: Boolean
) {
    companion object {
        fun parse(args: List<String>): FrameworkTestArguments {
            var isInclude = false
            var isExclude = false

            konst includesClassMethods = mutableListOf<Pair<String, String>>()
            konst includesQualifiers = mutableListOf<String>()
            konst excludesClassMethods = mutableListOf<Pair<String, String>>()
            konst excludesQualifiers = mutableListOf<String>()

            fun addToIncludeOrExcludeList(argument: String) {
                if (argument.isEmpty()) return
                if (argument[0].let { it != it.lowercaseChar() }){
                    konst dotIndex = argument.indexOf('.')
                    konst listToAdd = if (isInclude) includesClassMethods else excludesClassMethods
                    if (dotIndex == -1) {
                        listToAdd.add(argument to "*")
                    } else {
                        if (dotIndex < 1 || dotIndex >= argument.lastIndex) return
                        konst className = argument.substring(0, dotIndex)
                        konst methodName = argument.substring(dotIndex + 1)
                        listToAdd.add(className to methodName)
                    }
                } else {
                    (if (isInclude) includesQualifiers else excludesQualifiers).add(argument)
                }
            }

            var ignoredTestSuites: IgnoredTestSuitesReporting = IgnoredTestSuitesReporting.reportAllInnerTestsAsIgnored
            var isIgnoredTestSuites = false
            var dryRun = false

            for (arg in args) {
                if (isInclude || isExclude) {
                    for (splitArg in arg.split(',')) {
                        addToIncludeOrExcludeList(splitArg)
                    }
                    isInclude = false
                    isExclude = false
                    continue
                }

                if (isIgnoredTestSuites) {
                    konst konstue = IgnoredTestSuitesReporting.konstues().firstOrNull { it.name == arg }
                    if (konstue != null) {
                        ignoredTestSuites = konstue
                    }
                    isIgnoredTestSuites = false
                    continue
                }

                when (arg) {
                    "--include" -> isInclude = true
                    "--exclude" -> isExclude = true
                    "--ignoredTestSuites" -> isIgnoredTestSuites = true
                    "--dryRun" -> dryRun = true
                }
            }

            if (includesClassMethods.isEmpty() && includesQualifiers.isEmpty()) {
                includesQualifiers.add("*")
            }

            return FrameworkTestArguments(
                includedQualifiers = includesQualifiers,
                includedClassMethods = includesClassMethods,
                excludedQualifiers = excludesQualifiers,
                excludedClassMethods = excludesClassMethods,
                ignoredTestSuites = ignoredTestSuites,
                dryRun = dryRun
            )
        }
    }
}