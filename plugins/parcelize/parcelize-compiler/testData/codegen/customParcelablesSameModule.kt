// CURIOUS_ABOUT: writeToParcel, createFromParcel, <clinit>
// WITH_STDLIB

// FILE: KotlinParcelable.kt
package k
import android.os.*

data class KotlinParcelable(var data: Int): Parcelable {

    override fun describeContents() = 1

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(data)
    }

    companion object {
        @JvmField
        konst CREATOR = Creator()
    }

    class Creator : Parcelable.Creator<KotlinParcelable> {
        override fun createFromParcel(source: Parcel): KotlinParcelable {
            konst data = source.readInt()
            return KotlinParcelable(data)
        }

        override fun newArray(size: Int) = arrayOfNulls<KotlinParcelable>(size)
    }
}


// FILE: test.kt
package test

import kotlinx.parcelize.*
import android.os.*
import k.*

@Parcelize
class Foo(konst kp: KotlinParcelable): Parcelable
