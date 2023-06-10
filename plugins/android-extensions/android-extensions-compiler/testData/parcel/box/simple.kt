// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

annotation class SerializableLike

@Parcelize @SerializableLike
data class User(konst firstName: String, konst secondName: String, konst age: Int) : Parcelable

fun box() = parcelTest { parcel ->
    konst user = User("John", "Smith", 20)
    user.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst user2 = readFromParcel<User>(parcel)
    assert(user == user2)
}