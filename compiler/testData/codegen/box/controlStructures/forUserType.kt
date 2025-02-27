fun box() : String {
    var sum : Int = 0
    var i = 0

    konst c6 = MyCollection4()
    sum = 0
    for (el in c6) {
        sum = sum + el
    }
    if(sum != 15) return "c6 failed"

    konst c5 = MyCollection3()
    sum = 0
    for (el in c5) {
        sum = sum + (el ?: 0)
    }
    if(sum != 15) return "c5 failed"

    konst c1: Iterable<Int> = MyCollection1()
    sum = 0
    for (el in c1) {
        sum = sum + el!!
    }
    if(sum != 15) return "c1 failed"

    konst c2 = MyCollection1()
    sum = 0
    for (el in c2) {
        sum = sum + el!!
    }
    if(sum != 15) return "c2 failed"

    konst c3: Iterable<Int> = MyCollection2()
    sum = 0
    for (el in c3) {
        sum = sum + el!!
    }
    if(sum != 15) return "c3 failed"

    konst c4 = MyCollection2()
    sum = 0
    for (el in c4) {
        sum = sum + el!!
    }
    if(sum != 15) return "c4 failed"

    konst a : Array<Int> = arrayOfNulls<Int>(5) as Array<Int>
    for(el in 0..4) {
       a[i] = i++
    }
    sum = 0
    for (el in a) {
        sum = sum + el!!
    }
    if(sum != 10) return "a failed"

    konst b : Array<Int?> = arrayOfNulls<Int> (5)
    i = 0
    while(i < 5) {
       b[i] = i++
    }
    sum = 0
    for (el in b) {
        sum = sum + (el ?: 0)
    }
    if(sum != 10) return "b failed"

    return "OK"
}

class MyCollection1(): Iterable<Int> {
    override fun iterator(): Iterator<Int> = MyIterator()

    class MyIterator(): Iterator<Int> {
        var k : Int = 5

        override fun next() : Int = k--
        override fun hasNext() = k > 0
    }
}

class MyCollection2(): Iterable<Int> {
    override fun iterator(): Iterator<Int> = MyIterator()

    class MyIterator(): Iterator<Int> {
        var k : Int = 5

        override fun next() : Int = k--
        override fun hasNext() : Boolean = k > 0
    }
}

class MyCollection3() {
    operator fun iterator() = MyIterator()

    class MyIterator() {
        var k : Int = 5

        operator fun next() : Int? = k--
        operator fun hasNext() : Boolean = k > 0
    }
}

class MyCollection4() {
    operator fun iterator() = MyIterator()

    class MyIterator() {
        var k : Int = 5

        operator fun next() : Int = k--
        operator fun hasNext() = k > 0
    }
}
