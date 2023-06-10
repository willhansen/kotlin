// FIR_IDENTICAL
package test

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
class User(konst firstName: String, konst secondName: String, konst age: Int) : Parcelable
