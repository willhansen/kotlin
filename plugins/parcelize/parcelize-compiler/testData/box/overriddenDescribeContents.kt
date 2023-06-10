// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

annotation class SerializableLike

@Parcelize @SerializableLike
data class User(konst firstName: String, konst secondName: String, konst age: Int) : Parcelable {
    override fun describeContents(): Int = 1
}

fun box() = parcelTest { parcel ->
    konst user = User("John", "Smith", 20)
    user.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    konst user2 = parcelableCreator<User>().createFromParcel(parcel)
    assert(user == user2)
}
