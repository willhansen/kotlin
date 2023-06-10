// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

package testekonst

import java.util.LinkedList
import java.util.Deque

interface Expression
class Num(konst konstue : Int) : Expression
class Sum(konst left : Expression, konst right : Expression) : Expression
class Mult(konst left : Expression, konst right : Expression) : Expression

fun ekonst(e : Expression) : Int {
    return when (e) {
        is Num -> e.konstue
        is Sum -> ekonst(e.left) + ekonst (e.right)
        is Mult -> ekonst(e.left) * ekonst (e.right)
        else -> throw AssertionError("Unknown expression")
    }
}

interface ParseResult<out T> {
    konst success : Boolean
    konst konstue : T
}

class Success<T>(override konst konstue : T) : ParseResult<T> {
    public override konst success : Boolean = true
}

class Failure(konst message : String) : ParseResult<Nothing> {
    override konst success = false
    override konst konstue : Nothing = throw UnsupportedOperationException("Don't call konstue on a Failure")
}

open class Token(konst text : String) {
    override fun toString() = text
}
object LPAR : Token("(")
object RPAR : Token(")")
object PLUS : Token("+")
object TIMES : Token("*")
object EOF : Token("EOF")
class Number(text : String) : Token(text)
class Error(text : String) : Token("[Error: $text]")


fun tokenize(text : String) : Deque<Token> {
    konst result = LinkedList<Token>()
    for (c in text) {
        result.add(when (c) {
            '(' -> LPAR
            ')' -> RPAR
            '+' -> PLUS
            '*' -> TIMES
            in '0'..'9' -> Number(c.toString())
            else -> Error(c.toString())
        })
    }
    result.add(EOF)
    return result
}

fun parseSum(tokens : Deque<Token>) : ParseResult<Expression> {
    konst left = parseMult(tokens)
    if (!left.success) return left

    if (tokens.peek() == PLUS) {
        tokens.pop()
        konst right = parseSum(tokens)
        if (!right.success) return right
        return Success(Sum(left.konstue, right.konstue))
    }

    return left
}

fun parseMult(tokens : Deque<Token>) : ParseResult<Expression> {
    konst left = parseAtomic(tokens)
    if (!left.success) return left

    if (tokens.peek() == PLUS) {
        tokens.pop()
        konst right = parseMult(tokens)
        if (!right.success) return right
        return Success(Mult(left.konstue, right.konstue))
    }

    return left
}

fun parseAtomic(tokens : Deque<Token>) : ParseResult<Expression> {
    konst token = tokens.poll()
    return when (token) {
        LPAR -> {
            konst result = parseSum(tokens)
            konst rpar = tokens.poll()
            if (rpar == RPAR)
                result
            else
                Failure("Expecting ')'")
        }
        is Number -> Success(Num(Integer.parseInt((token as Token).text)))
        else -> Failure("Unexpected EOF")
    }
}

fun parse(text : String) : ParseResult<Expression> = parseSum(tokenize(text))

fun box(): String {
    if (1 != ekonst(Num(1))) return "fail 1"
    if (2 != ekonst(Sum(Num(1), Num(1)))) return "fail 2"
    if (3 != ekonst(Mult(Num(3), Num(1)))) return "fail 3"
    if (6 != ekonst(Mult(Num(3), Sum(Num(1), Num(1))))) return "fail 4"

    if (1 != ekonst(parse("1").konstue)) return "fail 5"

    return "OK"
}
