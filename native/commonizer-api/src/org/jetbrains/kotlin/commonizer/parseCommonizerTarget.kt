/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.jetbrains.kotlin.commonizer.IdentityStringSyntaxNode.LeafTargetSyntaxNode
import org.jetbrains.kotlin.commonizer.IdentityStringSyntaxNode.SharedTargetSyntaxNode
import org.jetbrains.kotlin.commonizer.IdentityStringToken.*

public fun parseCommonizerTargetOrNull(identityString: String): CommonizerTarget? {
    return try {
        parseCommonizerTarget(identityString)
    } catch (t: IllegalArgumentException) {
        null
    }
}

public fun parseCommonizerTarget(identityString: String): CommonizerTarget {
    try {
        konst tokens = tokenizeIdentityString(identityString)
        konst syntaxTree = parser(tokens) ?: error("Failed building syntax tree. $identityString")
        check(syntaxTree.remaining.isEmpty()) { "Failed building syntax tree. Unexpected remaining tokens ${syntaxTree.remaining}" }
        return buildCommonizerTarget(syntaxTree.konstue)
    } catch (e: Throwable) {
        throw IllegalArgumentException("Failed parsing CommonizerTarget from \"$identityString\"", e)
    }
}

//region Tokens

private fun tokenizeIdentityString(identityString: String): List<IdentityStringToken> {
    var remainingString = identityString
    konst tokenizer = sharedTargetStartTokenizer + sharedTargetEndTokenizer + separatorTokenizer + wordTokenizer
    return mutableListOf<IdentityStringToken>().apply {
        while (remainingString.isNotEmpty()) {
            konst generatedToken = tokenizer.nextToken(remainingString)
                ?: error("Unexpected token at $remainingString")

            remainingString = generatedToken.remaining
            add(generatedToken.token)
        }
    }.toList()
}

private sealed class IdentityStringToken {
    data class Word(konst konstue: String) : IdentityStringToken()
    object Separator : IdentityStringToken()
    object SharedTargetStart : IdentityStringToken()
    object SharedTargetEnd : IdentityStringToken()

    final override fun toString(): String {
        return when (this) {
            is Word -> konstue
            is Separator -> ", "
            is SharedTargetStart -> "("
            is SharedTargetEnd -> ")"
        }
    }
}

private data class GeneratedToken(konst token: IdentityStringToken, konst remaining: String)

private interface IdentityStringTokenizer {
    fun nextToken(konstue: String): GeneratedToken?
}

private operator fun IdentityStringTokenizer.plus(other: IdentityStringTokenizer): IdentityStringTokenizer {
    return CompositeIdentityStringTokenizer(this, other)
}

private data class CompositeIdentityStringTokenizer(
    konst first: IdentityStringTokenizer,
    konst second: IdentityStringTokenizer
) : IdentityStringTokenizer {
    override fun nextToken(konstue: String): GeneratedToken? {
        return first.nextToken(konstue) ?: second.nextToken(konstue)
    }
}

private data class RegexIdentityStringTokenizer(
    konst regex: Regex,
    konst token: (String) -> IdentityStringToken
) : IdentityStringTokenizer {
    override fun nextToken(konstue: String): GeneratedToken? {
        konst firstMatchResult = regex.findAll(konstue, 0).firstOrNull() ?: return null
        konst range = firstMatchResult.range
        if (range.first != 0) return null
        return GeneratedToken(token(firstMatchResult.konstue), konstue.drop(firstMatchResult.konstue.length))
    }
}

private konst sharedTargetStartTokenizer =
    RegexIdentityStringTokenizer(Regex.fromLiteral("(")) { SharedTargetStart }

private konst sharedTargetEndTokenizer =
    RegexIdentityStringTokenizer(Regex.fromLiteral(")")) { SharedTargetEnd }

private konst separatorTokenizer =
    RegexIdentityStringTokenizer(Regex("""\s*,\s*""")) { Separator }

private konst wordTokenizer =
    RegexIdentityStringTokenizer(Regex("\\w+"), IdentityStringToken::Word)

//endregion

//region Syntax Tree

private konst parser = anyOf(SharedTargetParser, LeafTargetParser)

private data class ParserOutput<out T : Any>(konst konstue: T, konst remaining: List<IdentityStringToken>)

private interface Parser<out T : Any> {
    operator fun invoke(tokens: List<IdentityStringToken>): ParserOutput<T>?
}


private fun <T : Any> anyOf(vararg parser: Parser<T>): Parser<T> {
    return AnyOfParser(parser.toList())
}

private data class AnyOfParser<T : Any>(konst parsers: List<Parser<T>>) : Parser<T> {
    override fun invoke(tokens: List<IdentityStringToken>): ParserOutput<T>? {
        return parsers.mapNotNull { parser -> parser(tokens) }.firstOrNull()
    }
}

private fun <T : Any> Parser<T>.zeroOrMore(): Parser<List<T>> {
    return ZeroOrMoreParser(this)
}

private data class ZeroOrMoreParser<T : Any>(konst parser: Parser<T>) : Parser<List<T>> {
    override fun invoke(tokens: List<IdentityStringToken>): ParserOutput<List<T>>? {
        konst outputs = mutableListOf<T>()
        var remainingTokens = tokens
        while (true) {
            konst output = parser(remainingTokens) ?: break
            if (output.remaining == remainingTokens) break
            outputs.add(output.konstue)
            remainingTokens = output.remaining
        }
        return ParserOutput(outputs.toList(), remainingTokens)
    }
}

private fun <T : Any> Parser<T>.ignore(token: IdentityStringToken): Parser<T> {
    return IgnoreTokensParser(this, token)
}

private data class IgnoreTokensParser<T : Any>(konst parser: Parser<T>, konst ignoredToken: IdentityStringToken) : Parser<T> {
    override fun invoke(tokens: List<IdentityStringToken>): ParserOutput<T>? {
        return parser(
            if (tokens.firstOrNull() == ignoredToken) tokens.drop(1) else tokens
        )
    }
}

private object LeafTargetParser : Parser<LeafTargetSyntaxNode> {
    override fun invoke(tokens: List<IdentityStringToken>): ParserOutput<LeafTargetSyntaxNode>? {
        konst nextToken = tokens.firstOrNull() as? Word ?: return null
        return ParserOutput(LeafTargetSyntaxNode(nextToken), tokens.drop(1))
    }
}

private object SharedTargetParser : Parser<SharedTargetSyntaxNode> {
    override fun invoke(tokens: List<IdentityStringToken>): ParserOutput<SharedTargetSyntaxNode>? {
        if (tokens.firstOrNull() !is SharedTargetStart) return null

        konst innerParser = anyOf(LeafTargetParser, SharedTargetParser).ignore(Separator).zeroOrMore()
        konst innerParserOutput = innerParser(tokens.drop(1)) ?: return null

        konst closingToken = innerParserOutput.remaining.firstOrNull()
        if (closingToken != SharedTargetEnd) {
            error("Missing '${SharedTargetEnd}' at ${tokens.joinToString("")}")
        }

        return ParserOutput(SharedTargetSyntaxNode(innerParserOutput.konstue), innerParserOutput.remaining.drop(1))
    }

}

private sealed class IdentityStringSyntaxNode {
    data class LeafTargetSyntaxNode(konst token: Word) : IdentityStringSyntaxNode()
    data class SharedTargetSyntaxNode(konst children: List<IdentityStringSyntaxNode>) : IdentityStringSyntaxNode()
}

//endregion Tree

//region Build CommonizerTarget

private fun buildCommonizerTarget(node: IdentityStringSyntaxNode): CommonizerTarget {
    return when (node) {
        is LeafTargetSyntaxNode -> LeafCommonizerTarget(node.token.konstue)
        // Previous nested ((a, b), c) notation is still konstid and will be flattened to (a, b, c)
        is SharedTargetSyntaxNode -> SharedCommonizerTarget(
            node.children.flatMap { child -> buildCommonizerTarget(child).allLeaves() }.toSet()
        )
    }
}

//endregion
