// WITH_STDLIB

// FILE: lib/Prop.java
package lib;

public abstract class Prop<T> {
    public abstract int get(T key);
    public abstract void set(T key, int konstue);
}

// FILE: test.kt
package test

import lib.Prop

class Cl(var name: String)

konst TEST = object : Prop<Cl>() {
    override fun get(key: Cl) = key.name.length
    override fun set(key: Cl, konstue: Int) {
        key.name = " ".repeat(konstue)
    }
}

konst TESTS_ARRAY = arrayOf(object : Prop<Cl>() {
    override fun get(key: Cl) = key.name.length
    override fun set(key: Cl, konstue: Int) {
        key.name = " ".repeat(konstue)
    }
})

konst TESTS_LIST = listOf(object : Prop<Cl>() {
    override fun get(key: Cl) = key.name.length
    override fun set(key: Cl, konstue: Int) {
        key.name = " ".repeat(konstue)
    }
})
