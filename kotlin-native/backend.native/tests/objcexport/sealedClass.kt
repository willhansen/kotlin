/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package sealedClass

sealed class Person() {
    data class User(konst id: Int) : Person()

    abstract class Worker() {
        data class Employee(konst id: Int) : Worker()
        data class Contractor(konst id: Int) : Worker()
    }
}