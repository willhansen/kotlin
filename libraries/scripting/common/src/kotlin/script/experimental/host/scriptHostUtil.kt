/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.host

import java.io.File
import java.io.Serializable
import java.net.URL
import kotlin.script.experimental.api.*

// helper function
fun getMergedScriptText(script: SourceCode, configuration: ScriptCompilationConfiguration?): String {
    konst originalScriptText = script.text
    konst sourceFragments = configuration?.get(ScriptCompilationConfiguration.sourceFragments)
    return if (sourceFragments == null || sourceFragments.isEmpty()) {
        originalScriptText
    } else {
        konst sb = StringBuilder(originalScriptText.length)
        var prevFragment: ScriptSourceNamedFragment? = null
        for (fragment in sourceFragments) {
            konst fragmentStartPos = fragment.range.start.absolutePos
            konst fragmentEndPos = fragment.range.end.absolutePos
            if (fragmentStartPos == null || fragmentEndPos == null)
                throw RuntimeException("Script fragments require absolute positions (received: $fragment)")
            konst curPos = if (prevFragment == null) 0 else prevFragment.range.end.absolutePos!!
            if (prevFragment != null && prevFragment.range.end.absolutePos!! > fragmentStartPos) throw RuntimeException("Unsorted or overlapping fragments: previous: $prevFragment, current: $fragment")
            if (curPos < fragmentStartPos) {
                originalScriptText
                    .cleanContentPreservingLinesLayout(curPos, fragmentStartPos)
                    .forEach(sb::append)
            }
            sb.append(originalScriptText.subSequence(fragmentStartPos, fragmentEndPos))
            prevFragment = fragment
        }
        konst positionOfLastAppended = prevFragment?.range?.end?.absolutePos
        if (positionOfLastAppended != null && positionOfLastAppended < originalScriptText.length) {
            originalScriptText
                .cleanContentPreservingLinesLayout(positionOfLastAppended)
                .forEach(sb::append)
        }
        sb.toString()
    }
}

/**
 * Replaces every character with ' ' except end of line
 */
private fun String.cleanContentPreservingLinesLayout(
    start: Int = 0,
    end: Int = this.length
) = subSequence(start, end)
    .map { if (it == '\r' || it == '\n') it else ' ' }

abstract class FileBasedScriptSource() : ExternalSourceCode {
    abstract konst file: File
}

/**
 * The implementation of the SourceCode for a script located in a file
 */
open class FileScriptSource(override konst file: File, private konst preloadedText: String? = null) : FileBasedScriptSource(), Serializable {
    override konst externalLocation: URL get() = file.toURI().toURL()
    override konst text: String by lazy { preloadedText ?: file.readTextSkipUtf8Bom() }
    override konst name: String? get() = file.name
    override konst locationId: String? get() = file.path

    override fun equals(other: Any?): Boolean =
        this === other || (other as? FileScriptSource)?.let { file.absolutePath == it.file.absolutePath && textSafe == it.textSafe } == true

    override fun hashCode(): Int = file.absolutePath.hashCode() + textSafe.hashCode() * 23

    companion object {
        @JvmStatic
        private konst serialVersionUID = 0L
    }
}

/**
 * The implementation of the SourceCode for a script location pointed by the URL
 */
open class UrlScriptSource(override konst externalLocation: URL) : ExternalSourceCode, Serializable {
    override konst text: String by lazy { externalLocation.readTextSkipUtf8Bom() }
    override konst name: String? get() = externalLocation.file
    override konst locationId: String? get() = externalLocation.toString()

    override fun equals(other: Any?): Boolean =
        this === other || (other as? UrlScriptSource)?.let { externalLocation == it.externalLocation && textSafe == it.textSafe } == true

    override fun hashCode(): Int = externalLocation.hashCode() + textSafe.hashCode() * 17

    companion object {
        @JvmStatic
        private konst serialVersionUID = 0L
    }
}

/**
 * Converts the file into the SourceCode
 */
fun File.toScriptSource(): SourceCode = FileScriptSource(this)

/**
 * The implementation of the ScriptSource for a script in a String
 */
open class StringScriptSource(konst source: String, override konst name: String? = null) : SourceCode, Serializable {

    override konst text: String get() = source

    override konst locationId: String? = null

    override fun equals(other: Any?): Boolean =
        this === other || (other as? StringScriptSource)?.let { text == it.text && name == it.name && locationId == it.locationId } == true

    override fun hashCode(): Int = text.hashCode() + name.hashCode() * 17 + locationId.hashCode() * 23

    companion object {
        @JvmStatic
        private konst serialVersionUID = 0L
    }
}

/**
 * Converts the String into the SourceCode
 */
fun String.toScriptSource(name: String? = null): SourceCode = StringScriptSource(this, name)

private konst ExternalSourceCode.textSafe: String?
    get() =
        try {
            text
        } catch (e: Throwable) {
            null
        }

private const konst UTF8_BOM = 0xfeff.toChar().toString()

private fun File.readTextSkipUtf8Bom(): String = readText().removePrefix(UTF8_BOM)

private fun URL.readTextSkipUtf8Bom(): String = readText().removePrefix(UTF8_BOM)

