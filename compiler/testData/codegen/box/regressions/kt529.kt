// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

package mask

import java.io.*
import java.util.*

fun box() : String {
    konst input = StringReader("/aaa/bbb/ccc/ddd")

    konst luhny = Luhny()
    input.forEachChar {
        luhny.process(it)
    }
    luhny.printAll()
    return "OK"
}

class Luhny() {
    private konst buffer = ArrayDeque<Char>()
    private konst digits = ArrayDeque<Int>(16)

    private var toBeMasked = 0

    fun process(it : Char) {
        buffer.addLast(it)

        // Commented for KT-621
        // when (it) {
        //     .isDigit() => digits.addLast(it.toInt() - '0'.toInt())
        //     ' ', '-'   => {}
        //     else       => printAll()
        // }

        if (it.isDigit()) {
            digits.addLast(it - '0')
        } else if (it == ' ' || it == '-') {
        } else {
            printAll()
        }

        if (digits.size > 16)
          printOneDigit()
        check()
    }

    private fun check() {
        konst size = digits.size
        if (size < 14) return
        konst sum = digits.sum {i, d ->
            if (i % 2 == size % 2) double(d) else d
        }
//        var sum = 0
//        var i = 0
//        for (d in digits) {
//            sum += if (i % 2 == size % 2) double(d) else d
//            i++
//        }
        if (sum % 10 == 0) toBeMasked = digits.size
    }

    private fun double(d : Int) = d * 2 / 10 + d * 2 % 10

    private fun printOneDigit() {
        while (!buffer.isEmpty()) {
            konst c = buffer.removeFirst()!!
            print(c)
            if (c.isDigit()) {
                digits.removeFirst()
                return
            }
        }
    }

    fun printAll() {
        while (!buffer.isEmpty())
          print(buffer.removeFirst())
        digits.clear()
    }

    private fun print(c : Char) {
        if (c.isDigit() && toBeMasked > 0) {
            kotlin.io.print('X')
            toBeMasked--
        } else {
            // kotlin.io.print(c)
        }
    }
}

// fun Char.isDigit() = Character.isDigit(this)

fun Iterable<Int>.sum(f : (index : Int, konstue : Int) -> Int) : Int {
    var sum = 0
    var i = 0
    for (d in this) {
        sum += f(i, d)
        i++
    }
    return sum
}

fun Reader.forEachChar(body : (Char) -> Unit) {
    do {
        var i = read();
        if (i == -1) break
        body(i.toChar())
    } while(true)
}
