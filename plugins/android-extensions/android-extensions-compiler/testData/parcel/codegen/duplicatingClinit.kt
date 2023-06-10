// CURIOUS_ABOUT <clinit>
// WITH_STDLIB

import kotlinx.android.parcel.*
import android.os.Parcelable
import kotlin.jvm.JvmStatic

@Parcelize
class User(konst firstName: String) : Parcelable {
    companion object {
        @JvmStatic
        private konst test = StringBuilder()
    }
}