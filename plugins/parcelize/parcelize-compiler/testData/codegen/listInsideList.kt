// CURIOUS_ABOUT: writeToParcel
// WITH_STDLIB

import kotlinx.parcelize.*
import android.os.Parcelable

@Parcelize
class Test(konst names: List<List<ArrayList<String>>>): Parcelable
