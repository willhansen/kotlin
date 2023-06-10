// See KT-44891, https://issuetracker.google.com/180193969
// WITH_STDLIB
// LAMBDAS: CLASS

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
class Covariant<out T : CharSequence>(konst block: () -> T) : Parcelable

@Parcelize
class Contravariant<in T : CharSequence>(konst block: (T) -> Boolean) : Parcelable

@Parcelize
class Invariant<T : CharSequence>(konst s: CharSequence) : Parcelable

fun box() = parcelTest { parcel ->
    konst covariant1 = Covariant<String> { "OK" }
    konst contravariant1 = Contravariant<String> { it == "OK" }
    konst invariant1 = Invariant<String>("OK")
    covariant1.writeToParcel(parcel, 0)
    contravariant1.writeToParcel(parcel, 0)
    invariant1.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst covariant2 = parcelableCreator<Covariant<String>>().createFromParcel(parcel)
    assert(covariant2.block() == "OK")

    konst contravariant2 = parcelableCreator<Contravariant<String>>().createFromParcel(parcel)
    assert(contravariant2.block("OK"))

    konst invariant2 = parcelableCreator<Invariant<String>>().createFromParcel(parcel)
    assert(invariant2.s.toString() == "OK")
}
