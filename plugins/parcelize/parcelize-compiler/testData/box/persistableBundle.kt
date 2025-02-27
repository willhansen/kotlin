// IGNORE_BACKEND: JVM
// See KT-38104
// The support for PersistableBundles is broken on JVM.
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable
import android.os.BaseBundle
import android.os.PersistableBundle

@Parcelize
data class User(konst a: PersistableBundle) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = User(
        PersistableBundle().apply { putLong("A", 1L) }
    )
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = parcelableCreator<User>().createFromParcel(parcel)

    assert(compareBundles(test.a, test2.a))
}

private fun compareBundles(first: BaseBundle, second: BaseBundle): Boolean {
    if (first.size() != second.size()) return false

    for (key in first.keySet()) {
        if (first.get(key) != second.get(key)) return false
    }

    return true
}
