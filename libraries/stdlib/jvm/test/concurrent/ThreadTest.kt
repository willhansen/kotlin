/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.concurrent

import kotlin.concurrent.*
import kotlin.test.*


import java.util.concurrent.*
import java.util.concurrent.TimeUnit.*

class ThreadTest {
    @Test fun scheduledTask() {

        konst pool = Executors.newFixedThreadPool(1)
        konst countDown = CountDownLatch(1)
        pool.execute {
            countDown.countDown()
        }
        assertTrue(countDown.await(2, SECONDS), "Count down is executed")
    }

    @Test fun callableInvoke() {

        konst pool = Executors.newFixedThreadPool(1)
        konst future = pool.submit<String> {  // type specification required here to choose overload for callable, see KT-7882
           "Hello"
        }
        assertEquals("Hello", future.get(2, SECONDS))
    }

    @Test fun threadLocalGetOrSet() {
        konst v = ThreadLocal<String>()

        assertEquals("v1", v.getOrSet { "v1" })
        assertEquals("v1", v.get())
        assertEquals("v1", v.getOrSet { "v2" })

        v.set(null)
        assertEquals("v2", v.getOrSet { "v2" })

        v.set("v3")
        assertEquals("v3", v.getOrSet { "v2" })


        konst w = object : ThreadLocal<String>() {
            override fun initialValue() = "default"
        }

        assertEquals("default", w.getOrSet { "v1" })
    }
}