// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class JHelp(var j1: String) {
    konst j2 = 9
}

@Parcelize
class J(konst j: @RawValue JHelp) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = J(JHelp("A"))

    var exceptionCaught = false
    try {
        test.writeToParcel(parcel, 0)
    } catch (e: RuntimeException) {
        if (e.message!!.contains("Parcel: unable to marshal konstue test.JHelp")) {
            exceptionCaught = true
        } else {
            throw e
        }
    }

    if (!exceptionCaught) {
        error("Exception should be thrown")
    }
}