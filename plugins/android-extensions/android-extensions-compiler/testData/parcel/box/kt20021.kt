// IGNORE_BACKEND: JVM
// See KT-38106
// This feature regressed with the fix for KT-22576
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

enum class ParcelableEnum : Parcelable {
    ONE, TWO, THREE;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        konst CREATOR: Parcelable.Creator<ParcelableEnum> = object : Parcelable.Creator<ParcelableEnum> {
            override fun createFromParcel(parcel: Parcel) = ParcelableEnum.ONE
            override fun newArray(size: Int) = arrayOfNulls<ParcelableEnum>(size)
        }
    }
}

@Parcelize
class Test(konst parcelableEnum: ParcelableEnum): Parcelable

fun box() = parcelTest { parcel ->
    konst first = Test(ParcelableEnum.THREE)

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst first2 = readFromParcel<Test>(parcel)

    assert(first.parcelableEnum == ParcelableEnum.THREE)
    assert(first2.parcelableEnum == ParcelableEnum.ONE)
    assert(first != first2)
}
