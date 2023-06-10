// FIR_IDENTICAL
package test

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel

class T(konst x: Int)

@Parcelize
class A(
    // T is not parcelable, but we don't need it to be since it is not being serialized.
    @IgnoredOnParcel konst x: T = T(0)
) : Parcelable {
    @IgnoredOnParcel konst y: T = x
}
