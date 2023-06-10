// CURIOUS_ABOUT: writeToParcel, createFromParcel
// WITH_STDLIB
// IGNORE_BACKEND: JVM

import kotlinx.parcelize.*
import android.os.Parcelable

@Parcelize
data class Test(
    konst a: UByteArray,
    konst b: UShortArray,
    konst c: UIntArray,
    konst d: ULongArray,
    konst e: UByteArray?,
    konst f: UShortArray?,
    konst g: UIntArray?,
    konst h: ULongArray?,
) : Parcelable
