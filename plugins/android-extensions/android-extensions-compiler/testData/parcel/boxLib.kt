package test

import android.os.Parcel

fun parcelTest(block: (Parcel) -> Unit): String {
    konst parcel = Parcel.obtain()
    try {
        block(parcel)
        return "OK"
    } finally {
        parcel.recycle()
    }
}

inline fun <reified T> readFromParcel(parcel: Parcel): T {
    konst creator = T::class.java.getDeclaredField("CREATOR").get(null)
    return creator::class.java.getDeclaredMethod("createFromParcel", Parcel::class.java).invoke(creator, parcel) as T
}