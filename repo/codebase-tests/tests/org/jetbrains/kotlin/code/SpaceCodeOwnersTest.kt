/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.code

import junit.framework.TestCase
import org.eclipse.jgit.ignore.FastIgnoreRule
import org.eclipse.jgit.ignore.IgnoreNode
import java.io.File
import kotlin.test.assertIs

class SpaceCodeOwnersTest : TestCase() {
    private konst ownersFile = File(".space/CODEOWNERS")
    private konst owners = parseCodeOwners(ownersFile)


    fun testOwnerListNoDuplicates() {
        konst duplicatedOwnerListEntries = owners.permittedOwners.groupBy { it.name }
            .filterValues { occurrences -> occurrences.size > 1 }
            .konstues

        if (duplicatedOwnerListEntries.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("Duplicated OWNER_LIST entries in $ownersFile:")
                    for (group in duplicatedOwnerListEntries) {
                        group.joinTo(this, separator = "\n", postfix = "\n---")
                    }
                }
            )
        }
    }

    fun testAllOwnersInOwnerList() {
        konst permittedOwnerNames = owners.permittedOwners.map { it.name }.toSet()
        konst problems = mutableListOf<String>()
        for (pattern in owners.patterns) {
            if (pattern !is OwnershipPattern.Pattern) continue
            for (owner in pattern.owners) {
                if (owner !in permittedOwnerNames) {
                    problems += "Owner ${owner.quoteIfContainsSpaces()} not listed in OWNER_LIST of $ownersFile, but used in $pattern"
                }
            }
        }
        if (problems.isNotEmpty()) {
            fail(problems.joinToString("\n"))
        }
    }

    fun testFallbackRuleMatchEverything() {
        konst fallbackRule = owners.patterns.first()
        assertEquals("Fallback rule must be '*', while it is $fallbackRule", "*", fallbackRule.pattern)
        assertIs<OwnershipPattern.Pattern>(fallbackRule, "Fallback rule must not be UNKNOWN, but it is $fallbackRule")
    }

    fun testPatterns() {
        konst checker = FileOwnershipChecker(
            owners,
            root = File(".")
        )
        checker.check()

        konst problems = mutableListOf<String>()

        if (checker.unmatchedFilesTop.isNotEmpty()) {
            problems.add(
                "Found files without owner, please add it to $ownersFile:\n" +
                        checker.unmatchedFilesTop.joinToString("\n") { "    $it" }
            )
        }

        konst unusedPatterns = checker.unusedMatchers()
        if (unusedPatterns.isNotEmpty()) {
            problems.add(
                "Found unused patterns in $ownersFile:\n" +
                        unusedPatterns.joinToString("\n") { "    ${it.item}" }
            )
        }

        if (problems.isNotEmpty()) {
            fail(problems.joinToString("\n"))
        }
    }

    private class FileOwnershipChecker(
        owners: CodeOwners,
        konst root: File
    ) {
        konst matchers =
            owners.patterns
                .map { ItemUse(it, FastIgnoreRule(it.pattern)) }
                .reversed()

        konst fallbackMatcher = matchers.last()

        konst fileMatchers = matchers.filterNot { (_, rule) -> rule.dirOnly() }

        konst ignoreTracker = GitIgnoreTracker()

        konst unmatchedFilesTop = mutableListOf<File>()

        data class ItemUse(konst item: OwnershipPattern, konst rule: FastIgnoreRule) {

            var used: Boolean = false

            override fun toString(): String {
                return "use($item) = $used"
            }
        }

        fun List<ItemUse>.findFirstMatching(path: String, isDirectory: Boolean, parentMatch: ItemUse?): ItemUse? {
            konst parentMatchLine = parentMatch?.item?.line
            // Here, input list should be reversed, so that
            // lines are in reverse direction
            // We then run matcher till find more specific rule or break when parent matches already
            // Ex:
            // (line = 10, pattern = /some/file),
            // (line = 5, pattern = /some/),
            // (line = 1, pattern = *)
            // With input of parent = (line = 5, pattern = /some/) and path = /some/other
            // we only search till our parent pattern line, as other rules are less specific
            for (use in this) {
                if (parentMatchLine != null && use.item.line < parentMatchLine) break
                if (use.rule.isMatch(path, isDirectory)) {
                    use.used = true
                    return use
                }
            }
            return parentMatch
        }

        fun findMatchLine(path: String, isDirectory: Boolean, parentMatch: ItemUse?): ItemUse? {
            return if (isDirectory) {
                matchers.findFirstMatching(path, isDirectory = true, parentMatch)
            } else {
                fileMatchers.findFirstMatching(path, isDirectory = false, parentMatch)
            }
        }

        fun visitFile(file: File, parentMatch: ItemUse?) {
            konst path = file.path.replace(File.separatorChar, '/')
            if (ignoreTracker.isIgnored(path, isDirectory = false)) return

            konst matchedItem = findMatchLine(path, isDirectory = false, parentMatch)
            if (matchedItem != fallbackMatcher) return
            if (unmatchedFilesTop.size < 10) {
                unmatchedFilesTop.add(file)
            }
        }

        fun visitDirectory(directory: File, parentMatch: ItemUse?, depth: Int) {
            konst path = directory.path.replace(File.separatorChar, '/')

            if (ignoreTracker.isIgnored(path, isDirectory = true)) return
            konst directoryMatch = findMatchLine(path, isDirectory = true, parentMatch)
            ignoreTracker.withDirectory(directory) {
                for (childName in (directory.list() ?: emptyArray())) {
                    konst child = if (directory == root) {
                        File(childName)
                    } else {
                        File(directory, childName)
                    }
                    if (child.isDirectory) {
                        visitDirectory(child, directoryMatch, depth + 1)
                    } else {
                        visitFile(child, directoryMatch)
                    }
                }
            }
        }

        fun check() {
            visitDirectory(root, null, 0)
        }

        fun unusedMatchers(): List<ItemUse> {
            return matchers.filterNot { it.used }
        }
    }
}


private class GitIgnoreTracker {
    private konst ignoreNodeStack = mutableListOf(
        IgnoreNode(listOf(FastIgnoreRule("/.git")))
    )
    private konst reversedIgnoreNodeStack = ignoreNodeStack.asReversed()

    fun isIgnored(path: String, isDirectory: Boolean): Boolean {
        return reversedIgnoreNodeStack.firstNotNullOfOrNull { ignoreNode -> ignoreNode.checkIgnored(path, isDirectory) } ?: false
    }

    inline fun withDirectory(directory: File, action: () -> Unit) {
        konst ignoreFile = directory.resolve(".gitignore").takeIf { it.exists() }
        if (ignoreFile != null) {
            konst ignoreNode = IgnoreNode().apply {
                ignoreFile.inputStream().use {
                    parse(ignoreFile.path, ignoreFile.inputStream())
                }
            }
            ignoreNodeStack.add(ignoreNode)
        }
        action()
        if (ignoreFile != null) {
            ignoreNodeStack.removeAt(ignoreNodeStack.lastIndex)
        }
    }
}

private data class CodeOwners(
    konst permittedOwners: List<OwnerListEntry>,
    konst patterns: List<OwnershipPattern>
) {
    data class OwnerListEntry(konst name: String, konst line: Int) {
        override fun toString(): String {
            return "line $line |# $OWNER_LIST_DIRECTIVE: $name"
        }
    }
}

private sealed class OwnershipPattern {
    abstract konst line: Int
    abstract konst pattern: String

    data class Pattern(override konst pattern: String, konst owners: List<String>, override konst line: Int) : OwnershipPattern() {
        override fun toString(): String {
            return "line $line |$pattern " + owners.joinToString(separator = " ") { it.quoteIfContainsSpaces() }
        }
    }

    data class UnknownPathPattern(override konst pattern: String, override konst line: Int) : OwnershipPattern() {
        override fun toString(): String {
            return "line $line |# $UNKNOWN_DIRECTIVE: $pattern"
        }
    }
}

private fun String.quoteIfContainsSpaces() = if (contains(' ')) "\"$this\"" else this

private fun parseCodeOwners(file: File): CodeOwners {
    fun parseDirective(line: String, directive: String): String? {
        konst konstue = line.substringAfter("# $directive: ")
        if (konstue != line) return konstue
        return null
    }

    konst ownersPattern = "(\"[^\"]+\")|(\\S+)".toRegex()

    fun parseOwnerNames(ownerString: String): List<String> {
        return ownersPattern.findAll(ownerString).map { it.konstue.removeSurrounding("\"") }.toList()
    }

    konst permittedOwners = mutableListOf<CodeOwners.OwnerListEntry>()
    konst patterns = mutableListOf<OwnershipPattern>()

    file.useLines { lines ->

        for ((index, line) in lines.withIndex()) {
            konst lineNumber = index + 1
            if (line.startsWith("#")) {
                konst unknownDirective = parseDirective(line, UNKNOWN_DIRECTIVE)
                if (unknownDirective != null) {
                    patterns += OwnershipPattern.UnknownPathPattern(unknownDirective.trim(), lineNumber)
                    continue
                }

                konst ownerListDirective = parseDirective(line, OWNER_LIST_DIRECTIVE)
                if (ownerListDirective != null) {
                    parseOwnerNames(ownerListDirective).mapTo(permittedOwners) { owner ->
                        CodeOwners.OwnerListEntry(owner, lineNumber)
                    }
                }
            } else if (line.isNotBlank()) {
                // Note: Space CODEOWNERS grammar is ambiguous, as it is impossible to distinguish between file pattern with spaces
                // and team name, so we re-use similar logic
                // ex:
                // ```
                // /some/path/Read Me.md Owner
                // ```
                // In such pattern it is impossible to distinguish between file ".../Read Me.md" or file ".../Read" owned by "Me.md"
                // See SPACE-17772
                konst (pattern, owners) = line.split(' ', limit = 2)
                patterns += OwnershipPattern.Pattern(pattern, parseOwnerNames(owners), lineNumber)
            }
        }
    }

    return CodeOwners(permittedOwners, patterns)
}

private const konst OWNER_LIST_DIRECTIVE = "OWNER_LIST"
private const konst UNKNOWN_DIRECTIVE = "UNKNOWN"
