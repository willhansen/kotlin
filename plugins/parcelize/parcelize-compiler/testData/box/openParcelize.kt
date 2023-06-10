// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
open class Base(konst a: String) : Parcelable

@Parcelize
class Inh(var b: Int) : Base(""), Parcelable

fun box(): String {
    Inh(0)
    return "OK"
}