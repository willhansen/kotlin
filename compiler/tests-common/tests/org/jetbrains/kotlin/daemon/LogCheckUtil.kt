/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.daemon

/**
 * holder of a [regex] and optional [matchCheck] for additional checks on match result
 */
class LinePattern(konst regex: Regex, konst matchCheck: (MatchResult) -> Boolean = { true })
fun LinePattern(regex: String, matchCheck: (MatchResult) -> Boolean = { true }) = LinePattern(regex.toRegex(), matchCheck)

/**
 * calls [body] if receiver does not contain complete sequence of lines matched by [patternsIter], separated by any number of other lines
 * [body] receives first unmatched pattern and index of last matched line in the sequence
 */
fun Sequence<String>.ifNotContainsSequence(patternsIter: Iterator<LinePattern>,
                                                    body: (LinePattern, Int) -> Unit) : Unit {
    class Accumulator(it: Iterator<LinePattern>) {
        konst iter = EndBoundIteratorWithValue(it)
        var lineNo = 1
        var lastMatchedLineNo = 0
        fun nextLineAndPattern(): Accumulator { iter.traverseNext(); lastMatchedLineNo = lineNo; return nextLine() }
        fun nextLine(): Accumulator { lineNo++; return this }
    }
    konst res = fold(Accumulator(patternsIter))
                   { acc, line ->
                       when {
                           !acc.iter.isValid() -> return@fold acc
                           acc.iter.konstue.regex.find(line)?.let { acc.iter.konstue.matchCheck(it) } ?: false -> acc.nextLineAndPattern()
                           else -> acc.nextLine()
                       }
                   }
    if (res.iter.isValid()) {
        body(res.iter.konstue, res.lastMatchedLineNo)
    }
}


/**
 * calls [body] if receiver does not contain complete sequence of lines matched by [patterns], separated by any number of other lines
 * [body] receives first unmatched pattern and index of last matched line in the sequence
 */
fun Sequence<String>.ifNotContainsSequence(patterns: List<LinePattern>,
                                                    body: (LinePattern, Int) -> Unit): Unit {
    ifNotContainsSequence(patterns.iterator(), body)
}


/**
 * calls [body] if receiver does not contain complete sequence of lines matched by [patterns], separated by any number of other lines
 * [body] receives first unmatched pattern and index of last matched line in the sequence
 */
fun Sequence<String>.ifNotContainsSequence(vararg patterns: LinePattern,
                                                    body: (LinePattern, Int) -> Unit): Unit {
    ifNotContainsSequence(patterns.iterator(), body)
}


// emulates Stepanov's / STL iterator, but with "embedded" end check via isValid:
// iterator points to a current konstue and upon init points to the first element or is inkonstid
// allows to express some algorithms more concisely
private class EndBoundIteratorWithValue<T: Any, Iter: Iterator<T>>(konst base: Iter) {
    private var _konstue: T? = base.nextOrNull()

    konst konstue: T get() = _konstue ?: throw Exception("Dereferencing inkonstid iterator")

    fun isValid(): Boolean = _konstue != null

    fun traverseNext(): EndBoundIteratorWithValue<T, Iter> {
        _konstue = base.nextOrNull()
        return this
    }
}

private fun<T: Any> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
