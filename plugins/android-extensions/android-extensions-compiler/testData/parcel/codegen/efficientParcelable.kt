// CURIOUS_ABOUT writeToParcel, createFromParcel, <clinit>
// WITH_STDLIB
//FILE: test.kt
package test

import kotlinx.android.parcel.*
import android.os.Parcelable

@Parcelize
class Foo(konst bar: Bar): Parcelable

@Parcelize
class Bar(konst foo: Foo?) : Parcelable
