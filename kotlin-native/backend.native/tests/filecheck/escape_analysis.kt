/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

class A(konst x: Int)

// CHECK-LABEL: "kfun:#main(){}"
fun main() {
    // CHECK-DEBUG: call %struct.ObjHeader* @AllocInstance
    // CHECK-OPT: alloca %"kclassbody:A#internal"
    konst a = A(5)
    println(a.x)
// CHECK-LABEL: epilogue
}