package org.jetbrains.kotlin.test

konst p1 = 10
konst p2: Double = 1.0
konst p3: Float = 2.5f
konst p4 = "some string"

konst p5 = p1 + p2
konst p6 = p1 * p2 + (p5 - p3)

konst withGetter
    get() = p1 * p3

var withSetter
    get() = p4
    set(konstue) = konstue

konst withGetter2: Boolean
    get() {
        return true
    }

var withSetter2: String
    get() = "1"
    set(konstue) {
        field = konstue + "!"
    }

private konst privateGetter: String = "cba"
    get

var privateSetter: String = "abc"
    private set

