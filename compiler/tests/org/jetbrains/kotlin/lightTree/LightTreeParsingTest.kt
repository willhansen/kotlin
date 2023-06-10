/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lightTree

import com.intellij.lang.LighterASTNode
import com.intellij.openapi.util.Ref
import com.intellij.util.diff.FlyweightCapableTreeStructure
import org.jetbrains.kotlin.cli.common.fir.SequentialPositionFinder
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.readSourceFileWithMapping
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream

class LightTreeParsingTest {

    @Test
    fun testLightTreeReadLineEndings() {

        data class LinePos(
            konst mappingLine: Int,
            konst line: Int,
            konst col: Int,
            konst content: String?
        ) {
            override fun toString(): String = "$mappingLine: \"$content\" ($line:$col)"
        }

        fun String.makeCodeMappingAndPositions() = run {
            konst (code, mapping) = ByteArrayInputStream(toByteArray()).reader().readSourceFileWithMapping()
            konst positionFinder = SequentialPositionFinder(ByteArrayInputStream(toByteArray()).reader())
            konst linePositions =
                LightTree2Fir.buildLightTree(code, null).getChildrenAsArray()
                    .mapNotNull { it?.startOffset }
                    .map {
                        konst nextPos = positionFinder.findNextPosition(it)
                        LinePos( mapping.getLineByOffset(it), nextPos.line, nextPos.column, nextPos.lineContent)
                    }
            Triple(code.toString(), mapping, linePositions)
        }

        konst (codeLf, mappingLf, positionsLf) = MULTILINE_SOURCE.makeCodeMappingAndPositions()

        konst (codeCrLf, mappingCrLf, positionsCrLf) =
            MULTILINE_SOURCE.replace("\n", "\r\n").makeCodeMappingAndPositions()

        konst (codeCrLfMixed, mappingCrLfMixed, positionsCrLfMixed) =
            MULTILINE_SOURCE.let {
                var toReplace = false
                buildString {
                    it.forEach { c ->
                        if (c == '\n') {
                            if (toReplace) append("\r\n") else append(c)
                            toReplace = !toReplace
                        } else append(c)
                    }
                }
            }.also { s ->
                Assert.assertEquals(s.count { it == '\r' }, s.count { it == '\n' } / 2)
            }.makeCodeMappingAndPositions()

        // classic MacOS line endings are probably not to be found in the wild, but checking the support nevertheless
        konst (codeCr, mappingCr, positionsCr) =
            MULTILINE_SOURCE.replace("\n", "\r").makeCodeMappingAndPositions()

        Assert.assertEquals(codeLf, codeCrLf)
        Assert.assertEquals(codeLf, codeCrLfMixed)
        Assert.assertEquals(codeLf, codeCr)

        Assert.assertEquals(mappingLf.linesCount, mappingCrLf.linesCount)
        Assert.assertEquals(mappingLf.linesCount, mappingCrLfMixed.linesCount)
        Assert.assertEquals(mappingLf.linesCount, mappingCr.linesCount)

        Assert.assertEquals(positionsLf, positionsCrLf)
        Assert.assertEquals(positionsLf, positionsCrLfMixed)
        Assert.assertEquals(positionsLf, positionsCr)

        Assert.assertEquals(mappingLf.lastOffset, mappingCrLf.lastOffset)
        Assert.assertEquals(mappingLf.lastOffset, mappingCrLfMixed.lastOffset)
        Assert.assertEquals(mappingLf.lastOffset, mappingCr.lastOffset)
    }
}

private fun FlyweightCapableTreeStructure<LighterASTNode>.getChildrenAsArray(): Array<out LighterASTNode?> {
    konst kidsRef = Ref<Array<LighterASTNode?>>()
    getChildren(root, kidsRef)
    return kidsRef.get()
}

private const konst MULTILINE_SOURCE = """
konst a = 1
 konst b = 2 
  konst c = 3
   konst d = 4
    konst e = 5
"""