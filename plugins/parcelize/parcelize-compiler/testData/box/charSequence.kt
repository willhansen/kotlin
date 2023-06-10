// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableString

@Parcelize
data class Test(konst simple: CharSequence, konst spanned: CharSequence) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = Test("John", SpannableString("Smith"))
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = parcelableCreator<Test>().createFromParcel(parcel)

    assert(test.simple.toString() == test2.simple.toString())
    assert(test.spanned.toString() == test2.spanned.toString())
}