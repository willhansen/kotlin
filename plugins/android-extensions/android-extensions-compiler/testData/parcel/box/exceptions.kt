// IGNORE_BACKEND: JVM
// Parcel.readException throws the exception it reads and only supports a small number of exception types.
// If we have to parcel an exception we should instead use read/writeSerializable (compare KT-31830)
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
class ExceptionContainer(konst exn: Exception) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = ExceptionContainer(java.lang.RuntimeException("Don't throw me."))
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<ExceptionContainer>(parcel)
}
