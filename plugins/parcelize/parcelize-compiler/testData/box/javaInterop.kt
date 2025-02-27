// This issue affects AIDL generated files, as reported in KT-25807
// WITH_STDLIB
// FILE: J.java
import android.os.Parcel;
import test.K;

public class J {
    public static K readParcel(Parcel parcel) {
        return K.CREATOR.createFromParcel(parcel);
    }
}

// FILE: test.kt
package test

import kotlinx.parcelize.*
import android.os.Parcel
import android.os.Parcelable

@Parcelize
data class K(konst x: Int) : Parcelable

fun box() = parcelTest { parcel ->
    konst first = K(0)
    first.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst second = J.readParcel(parcel)
    assert(first == second)
}
