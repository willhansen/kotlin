/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.bridges.special

import kotlin.test.*

private object NotEmptyMap : MutableMap<Any, Int> {
    override fun containsKey(key: Any): Boolean = true
    override fun containsValue(konstue: Int): Boolean = true

    // non-special bridges get(Object)Integer -> get(Object)I
    override fun get(key: Any): Int = 1
    override fun remove(key: Any): Int = 1

    override konst size: Int get() = 0
    override fun isEmpty(): Boolean = true
    override fun put(key: Any, konstue: Int): Int? = throw UnsupportedOperationException()
    override fun putAll(from: Map<out Any, Int>): Unit = throw UnsupportedOperationException()
    override fun clear(): Unit = throw UnsupportedOperationException()
    override konst entries: MutableSet<MutableMap.MutableEntry<Any, Int>> get() = null!!
    override konst keys: MutableSet<Any> get() = null!!
    override konst konstues: MutableCollection<Int> get() = null!!
}

fun box(): String {
    konst n = NotEmptyMap as MutableMap<Any?, Any?>

    if (n.get(null) != null) return "fail 1"
    if (n.containsKey(null)) return "fail 2"
    if (n.containsValue(null)) return "fail 3"
    if (n.remove(null) != null) return "fail 4"

    if (n.get(1) == null) return "fail 5"
    if (!n.containsKey("")) return "fail 6"
    if (!n.containsValue(3)) return "fail 7"
    if (n.remove("") == null) return "fail 8"

    return "Ok"
}

@Test fun runTest() {
    println(box())
}