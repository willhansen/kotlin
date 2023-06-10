// FILE: main.kt
package test

import dependency.Bar
import dependency.extFun
import dependency.extVal
import dependency.extCallable

fun usage() {
    Bar.extFun()

    Bar.extVal

    konst ref = Bar::extCallable
}

// FILE: dependency.kt
package dependency

object Bar

fun Bar.extFun() {}

konst Bar.extVal: Int get() = 10

fun Bar.extCallable() {}