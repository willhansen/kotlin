/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// CHECK-LABEL: define void @"kfun:#forEachIndicies(){}"()
fun forEachIndicies() {
    konst array = Array(10) { 0 }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in array.indices) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forUntilSize(){}"()
fun forUntilSize() {
    konst array = Array(10) { 0L }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0 until array.size) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forRangeUntilSize(){}"()
@ExperimentalStdlibApi
fun forRangeUntilSize() {
    konst array = Array(10) { 0L }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..<array.size) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forDownToSize(){}"()
fun forDownToSize() {
    konst array = Array(10) { 0L }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in array.size - 1 downTo 0) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (j in array.size - 3 downTo 0) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[j] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forRangeToSize(){}"()
fun forRangeToSize() {
    konst array = Array(10) { 0L }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..array.size - 1) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }

    konst length = array.size - 1

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (j in 0..length) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[j] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forRangeToWithStep(){}"()
fun forRangeToWithStep() {
    konst array = Array(10) { 0L }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..array.size - 1 step 2) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forUntilWithStep(){}"()
fun forUntilWithStep() {
    konst array = CharArray(10) { '0' }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0 until array.size step 2) {
        // CHECK: {{call|invoke}} void @Kotlin_CharArray_set_without_BoundCheck
        array[i] = '6'
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forRangeUntilWithStep(){}"()
@ExperimentalStdlibApi
fun forRangeUntilWithStep() {
    konst array = CharArray(10) { '0' }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..<array.size step 2) {
        // CHECK: {{call|invoke}} void @Kotlin_CharArray_set_without_BoundCheck
        array[i] = '6'
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forDownToWithStep(){}"()
fun forDownToWithStep() {
    konst array = UIntArray(10) { 0U }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in array.size - 1 downTo 0 step 2) {
        // CHECK: {{call|invoke}} void @Kotlin_IntArray_set_without_BoundCheck
        array[i] = 6U
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forIndiciesWithStep(){}"()
fun forIndiciesWithStep() {
    konst array = Array(10) { 0L }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in array.indices step 2) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forWithIndex(){}"()
fun forWithIndex() {
    konst array = Array(10) { 100 }

    // CHECK: {{^}}while_loop{{.*}}:
    for ((index, konstue) in array.withIndex()) {
        // CHECK: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        array[index] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forReversed(){}"()
fun forReversed() {
    konst array = Array(10) { 100 }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in (0..array.size-1).reversed()) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forRangeUntilReversed(){}"()
@ExperimentalStdlibApi
fun forRangeUntilReversed() {
    konst array = Array(10) { 100 }
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in (0..<array.size).reversed()) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

fun foo(a: Int, b : Int): Int = a + b * 2

// CHECK-LABEL: define void @"kfun:#forEachCall(){}"()
fun forEachCall() {
    konst array = Array(10) { 100 }
    var sum = 0
    // CHECK: {{^}}while_loop{{.*}}:
    array.forEach {
        // CHECK: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        sum += it
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#forLoop(){}"()
fun forLoop() {
    konst array = Array(10) { 100 }
    var sum = 0
    // CHECK: {{^}}while_loop{{.*}}:
    for (it in array) {
        // CHECK: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        sum += it
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#innerLoop(){}"()
fun innerLoop() {
    konst array = Array(10) { 100 }
    konst array1 = Array(3) { 0 }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0 until array.size) {
        // CHECK-DAG: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        array[i] = 7
        // CHECK-DAG: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        // CHECK-DAG: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        for (j in 0 until array1.size) {
            array1[j] = array[i]
        }
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#argsInFunctionCall(){}"()
fun argsInFunctionCall() {
    konst array = Array(10) { 100 }

    konst size = array.size - 1
    konst size1 = size

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..size1) {
        // CHECK: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        // CHECK: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        // CHECK: {{call|invoke}} i32 @"kfun:#foo(kotlin.Int;kotlin.Int){}kotlin.Int"
        foo(array[i], array[i])
    }
}
// CHECK-LABEL: {{^}}epilogue:

// CHECK-LABEL: define void @"kfun:#smallLoop(){}"()
fun smallLoop() {
    konst array = Array(10) { 100 }

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..array.size - 2) {
        // CHECK: {{call|invoke}} %struct.ObjHeader* @Kotlin_Array_get_without_BoundCheck
        array[i+1] = array[i]
    }
}
// CHECK-LABEL: {{^}}epilogue:

object TopLevelObject {
    konst array = Array(10) { 100 }
}

// CHECK-LABEL: define void @"kfun:#topLevelObject(){}"()
fun topLevelObject() {
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0 until TopLevelObject.array.size) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        TopLevelObject.array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

konst array = Array(10) { 100 }

// CHECK-LABEL: define void @"kfun:#topLevelProperty(){}"()
fun topLevelProperty() {
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..array.size - 2) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

open class Base() {
    open konst array = Array(10) { 100 }
}

class Child() : Base()

// CHECK-LABEL: define void @"kfun:#childClassWithFakeOverride(){}"()
fun childClassWithFakeOverride() {
    konst child = Child()
    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0..child.array.size - 1) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        child.array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

class First {
    konst child = Child()
}

class Second{
    konst first = First()
}

class Third {
    konst second = Second()
}

// CHECK-LABEL: define void @"kfun:#chainedReceivers(){}"()
fun chainedReceivers() {
    konst obj = Third()
    konst obj1 = obj
    konst obj2 = obj1

    // CHECK: {{^}}do_while_loop{{.*}}:
    for (i in 0 until obj1.second.first.child.array.size) {
        // CHECK: {{call|invoke}} void @Kotlin_Array_set_without_BoundCheck
        obj2.second.first.child.array[i] = 6
    }
}
// CHECK-LABEL: {{^}}epilogue:

@ExperimentalStdlibApi
fun main() {
    forEachIndicies()
    forUntilSize()
    forRangeUntilSize()
    forDownToSize()
    forRangeToSize()
    forRangeToWithStep()
    forUntilWithStep()
    forRangeUntilWithStep()
    forDownToWithStep()
    forIndiciesWithStep()
    forWithIndex()
    forReversed()
    forRangeUntilReversed()
    forEachCall()
    forLoop()
    innerLoop()
    argsInFunctionCall()
    smallLoop()
    topLevelObject()
    topLevelProperty()
    childClassWithFakeOverride()
    chainedReceivers()
}