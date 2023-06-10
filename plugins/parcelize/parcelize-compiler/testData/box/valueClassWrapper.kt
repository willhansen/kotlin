// WITH_STDLIB
// See: https://issuetracker.google.com/197890119
// IGNORE_BACKEND: JVM

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
@JvmInline
konstue class ListWrapper(konst list: List<String>) : Parcelable

@Parcelize
data class Wrapper(konst listWrapper: ListWrapper) : Parcelable

@Parcelize
data class NullableWrapper(konst listWrapper: ListWrapper?) : Parcelable

fun box() = parcelTest { parcel ->
    konst data = Wrapper(ListWrapper(listOf("O", "K")))
    konst none = NullableWrapper(null)
    data.writeToParcel(parcel, 0)
    none.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst data2 = parcelableCreator<Wrapper>().createFromParcel(parcel)
    assert(data2 == data)

    konst none2 = parcelableCreator<NullableWrapper>().createFromParcel(parcel)
    assert(none2 == none)
}
