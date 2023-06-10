// FIR_IDENTICAL
package test

import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import android.os.Parcelable

@Parcelize
class User(
        konst a: String,
        konst b: <!PARCELABLE_TYPE_NOT_SUPPORTED!>Any<!>,
        konst c: <!PARCELABLE_TYPE_NOT_SUPPORTED!>Any?<!>,
        konst d: <!PARCELABLE_TYPE_NOT_SUPPORTED!>Map<Any, String><!>,
        konst e: @RawValue Any?,
        konst f: @RawValue Map<String, Any>,
        konst g: Map<String, @RawValue Any>,
        konst h: Map<@RawValue Any, List<@RawValue Any>>
) : Parcelable
