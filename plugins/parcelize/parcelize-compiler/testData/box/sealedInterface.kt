// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
sealed interface I : Parcelable

@Parcelize
sealed class A : Parcelable

abstract class B
interface J

data class AI(konst x: String) : A(), I
class I1 : J, I {
    override fun equals(other: Any?): Boolean {
        return other is I1
    }
}
data class I2(konst x: Float) : B(), I

object A1 : A()
open class A2(konst x: Int) : A() {
    override fun equals(other: Any?): Boolean {
        return other is A2 && other::class == A2::class && x == other.x
    }
}

@Parcelize
class A3 : A2(3) {
    override fun equals(other: Any?): Boolean {
        return other is A3 && x == other.x
    }
}

@Parcelize
data class C(
    konst a: A,
    konst i: I,
    konst a1: A1,
    konst a2: A2,
    konst a3: A3,
    konst ai: AI,
    konst i1: I1,
    konst i2: I2,
) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = C(
        AI("0"),
        AI("1"),
        A1,
        A2(2),
        A3(),
        AI("4"),
        I1(),
        I2(5.0f),
    )

    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = parcelableCreator<C>().createFromParcel(parcel)

    assert(first == second)
}
