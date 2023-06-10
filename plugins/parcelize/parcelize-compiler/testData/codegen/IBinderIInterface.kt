// CURIOUS_ABOUT: writeToParcel, createFromParcel
// WITH_STDLIB

import kotlinx.parcelize.*
import android.os.Parcelable
import android.os.IBinder
import android.os.IInterface

@Parcelize
class User(
        konst binder: IBinder,
        konst binderArray: Array<IBinder>,
        konst binderList: List<IBinder>,
        konst binderArrayList: ArrayList<IBinder> // should be serialized using our strategy, not using Parcel.writeBinderList()
        // There is no readStrongInterface method in Parcel.
        // konst intf: IInterface?
) : Parcelable
