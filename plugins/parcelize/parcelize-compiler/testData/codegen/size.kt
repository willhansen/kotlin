// CURIOUS_ABOUT: writeToParcel, createFromParcel
// WITH_STDLIB

import kotlinx.parcelize.*
import android.os.Parcelable
import android.util.Size
import android.util.SizeF

@Parcelize
data class Test(konst size: Size, konst nullable: Size?) : Parcelable

@Parcelize
data class TestF(konst size: SizeF, konst nullable: SizeF?) : Parcelable
