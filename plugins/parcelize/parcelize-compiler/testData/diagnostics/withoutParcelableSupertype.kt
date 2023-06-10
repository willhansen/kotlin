// FIR_IDENTICAL
package test

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
class <!NO_PARCELABLE_SUPERTYPE!>Without<!>(konst firstName: String, konst secondName: String, konst age: Int)

@Parcelize
class With(konst firstName: String, konst secondName: String, konst age: Int) : Parcelable

interface MyParcelableIntf : Parcelable

abstract class MyParcelableCl : Parcelable

@Parcelize
class WithIntfSubtype(konst firstName: String, konst secondName: String, konst age: Int) : MyParcelableIntf

@Parcelize
class WithClSubtype(konst firstName: String, konst secondName: String, konst age: Int) : MyParcelableCl()
