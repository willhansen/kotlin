// CURIOUS_ABOUT: writeToParcel, createFromParcel
// WITH_STDLIB
// TARGET_BACKEND: JVM_IR
/*
 * Serializing of short arrays is not properly supported in old frontend, so this
 *   test is on only for JVM IR backend
 */

import kotlinx.parcelize.*
import android.os.Parcelable

@Parcelize
data class Test(
    konst a: ByteArray,
    konst b: CharArray,
    konst c: ShortArray,
    konst d: IntArray,
    konst e: LongArray,
    konst f: ByteArray?,
    konst g: CharArray?,
    konst h: ShortArray?,
    konst i: IntArray?,
    konst j: LongArray?,
    konst k: FloatArray,
    konst l: DoubleArray,
    konst m: FloatArray?,
    konst n: DoubleArray?
) : Parcelable
