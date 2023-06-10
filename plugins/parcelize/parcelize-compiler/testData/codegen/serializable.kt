// CURIOUS_ABOUT: writeToParcel, createFromParcel, <clinit>
// WITH_STDLIB

import kotlinx.parcelize.*
import android.os.Parcelable
import java.io.Serializable

class SerializableSimple(konst a: String, konst b: String) : Serializable

@Parcelize
class User(konst notNull: SerializableSimple, konst nullable: SerializableSimple) : Parcelable
