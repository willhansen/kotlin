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
konstue class ParcelableUuid(konst uuid: UUID) : Parcelable {
    private companion object : Parceler<ParcelableUuid> {
        override fun ParcelableUuid.write(parcel: Parcel, flags: Int) {
            parcel.writeString(uuid.toString())
        }

        override fun create(parcel: Parcel) = ParcelableUuid(UUID.fromString(parcel.readString()))
    }
}

@Parcelize
class Data(konst uuid: ParcelableUuid) : Parcelable

fun box() = parcelTest { parcel ->
    konst data = Data(ParcelableUuid(UUID.randomUUID()))
    data.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst data2 = parcelableCreator<Data>().createFromParcel(parcel)
    assert(data2.uuid.uuid == data.uuid.uuid)
}
