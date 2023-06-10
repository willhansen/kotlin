// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

annotation class SerializableLike

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize @SerializableLike
data class User(konst firstName: String, konst secondName: String, konst age: Int) : Parcelable

fun box() = parcelTest { parcel ->
    konst user = User("John", "Smith", 20)
    user.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    @Suppress("UNCHECKED_CAST")
    konst creator = User::class.java.getDeclaredField("CREATOR").get(null) as Parcelable.Creator<User>
    konst user2 = creator.createFromParcel(parcel)
    assert(user == user2)
}