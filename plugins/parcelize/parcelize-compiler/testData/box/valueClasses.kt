// WITH_STDLIB
// See: https://issuetracker.google.com/177856519
// IGNORE_BACKEND: JVM

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

@JvmInline
@Parcelize
konstue class ParcelableInt(konst konstue: Int) : Parcelable

@JvmInline
@Parcelize
konstue class ParcelableString(konst konstue: String) : Parcelable

@JvmInline
@Parcelize
konstue class ParcelableValueClass(konst konstue: ParcelableInt) : Parcelable

@JvmInline
@Parcelize
konstue class ParcelableNullableValueClass(konst konstue: ParcelableString?) : Parcelable

@Parcelize
data class Data(
    konst parcelableInt: ParcelableInt,
    konst parcelableString: ParcelableString,
    konst parcelableValueClass: ParcelableValueClass,
    konst parcelableNullableValueClass: ParcelableNullableValueClass,
    konst parcelableNullableValueClassNullable: ParcelableNullableValueClass?,
) : Parcelable

fun box() = parcelTest { parcel ->
    konst data = Data(
        ParcelableInt(10),
        ParcelableString(""),
        ParcelableValueClass(ParcelableInt(30)),
        ParcelableNullableValueClass(null),
        null,
    )
    data.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst data2 = parcelableCreator<Data>().createFromParcel(parcel)
    assert(data2 == data)
}
