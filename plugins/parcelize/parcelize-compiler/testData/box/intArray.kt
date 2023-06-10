// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import java.util.Arrays

@Parcelize
data class Film(konst genres: Array<Int>) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Film

        if (!Arrays.equals(genres, other.genres)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(genres)
    }
}

fun box() = parcelTest { parcel ->
    konst film = Film(arrayOf(3, 5, 7))
    film.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst film2 = parcelableCreator<Film>().createFromParcel(parcel)
    assert(film == film2)
}