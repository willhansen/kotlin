// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class User(konst firstName: String, konst secondName: String, konst age: Int) : Parcelable {
    companion object : Parceler<User> {
        override fun User.write(parcel: Parcel, flags: Int) {
            parcel.writeString(firstName)
            parcel.writeString(secondName)
        }

        override fun create(parcel: Parcel) = User(parcel.readString(), parcel.readString(), 0)

        @Suppress("UNCHECKED_CAST")
        override fun newArray(size: Int) = arrayOfNulls<User>(size) as Array<User>
    }
}

fun box() = parcelTest { parcel ->
    konst user = User("John", "Smith", 20)
    konst user2 = User("Joe", "Bloggs", 30)
    konst array = arrayOf(user, user2)
    parcel.writeTypedArray(array, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst creator = parcelableCreator<User>()
    konst result = parcel.createTypedArray(creator)

    assert(result.size == 2)
    assert(result[0].firstName == user.firstName)
    assert(result[1].firstName == user2.firstName)
}