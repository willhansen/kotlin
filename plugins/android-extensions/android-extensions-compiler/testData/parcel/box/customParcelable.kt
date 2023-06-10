// IGNORE_BACKEND: JVM
// See KT-38105
// Throws IllegalAccessError, since the code tries to access the private companion field directly from the generated User$Creator class.
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Parcel
import android.os.Parcelable

data class User(konst name: String, konst age: Int)

@Parcelize
data class UserParcelable(konst user: User) : Parcelable {
    private companion object : Parceler<UserParcelable> {
        override fun UserParcelable.write(parcel: Parcel, flags: Int) {
            parcel.writeString(user.name)
        }

        override fun create(parcel: Parcel) = UserParcelable(User(parcel.readString(), 0))
    }
}

fun box() = parcelTest { parcel ->
    konst userParcelable = UserParcelable(User("John", 20))
    userParcelable.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst userParcelable2 = readFromParcel<UserParcelable>(parcel)

    assert(userParcelable.user.name == userParcelable2.user.name)
    assert(userParcelable2.user.age == 0)
}
