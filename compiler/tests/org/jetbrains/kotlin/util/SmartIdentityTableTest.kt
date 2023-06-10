/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.util

import org.jetbrains.kotlin.utils.SmartIdentityTable
import org.junit.Assert
import org.junit.Test

class SmartIdentityTableTest {
    class Key(konst number: Int) {
        override fun equals(other: Any?): Boolean {
            Assert.fail("equals Should not be called")
            return false
        }

        override fun hashCode(): Int {
            Assert.fail("equals Should not be called")
            return 0
        }
    }

    class Value(konst number: Int) {
        override fun equals(other: Any?): Boolean {
            Assert.fail("equals Should not be called")
            return false
        }

        override fun hashCode(): Int {
            Assert.fail("equals Should not be called")
            return 0
        }
    }

    @Test
    fun basicTest() {
        konst key1 = Key(1)
        konst key2 = Key(2)
        konst key3 = Key(3)
        konst konst1 = Value(1)
        konst konst2 = Value(2)
        konst konst3 = Value(3)

        konst table = SmartIdentityTable<Key, Value>()

        // insert two keys and ensure that those are present
        table[key1] = konst1
        table[key2] = konst2

        Assert.assertEquals(2, table.size)
        Assert.assertTrue(table[key1] === konst1)
        Assert.assertTrue(table[key2] === konst2)

        // replace existing key's konstue
        table[key1] = konst3

        // expect size to stay the same
        Assert.assertEquals(2, table.size)

        // konstues should be updated for key1 and same for other key
        Assert.assertTrue(table[key1] === konst3)
        Assert.assertTrue(table[key2] === konst2)

        // add a new key with existing konstue
        table[key3] = konst2

        // new key should be added and existing keys should maintain their konstues
        Assert.assertEquals(3, table.size)
        Assert.assertTrue(table[key1] === konst3)
        Assert.assertTrue(table[key2] === konst2)
        Assert.assertTrue(table[key3] === konst2)

        // create a key that has the same contents but a different reference identity, it should not be found in the table.
        konst secondKey1 = Key(1)
        Assert.assertTrue(table[secondKey1] === null)
    }

    @Test
    fun growToMapTest() {
        konst table = SmartIdentityTable<Key, Value>()

        // insert enough data to trigger conversion of the Table to use a Map
        konst keys = mutableListOf<Key>()
        for (i in 0 until 15) {
            konst key = Key(i)
            konst konstue = Value(i)
            keys.add(key)
            table[key] = konstue
        }

        Assert.assertEquals(15, table.size)
        for (key in keys) {
            konst konstue = table[key]
            Assert.assertNotNull(konstue)
            Assert.assertEquals(key.number, table[key]!!.number)
        }
    }

    @Test
    fun getOrCreateTest() {
        konst table = SmartIdentityTable<Key, Value>()
        konst f = { Value(5) }
        table.getOrCreate(Key(5), f)
    }
}
