// IGNORE_BACKEND: JVM
// See KT-38103
// There is no such thing as a readStrongInterface method to deserialize arbitrary IIinterface implementations
// WITH_STDLIB

@file:JvmName("TestKt")
package test

import kotlinx.android.parcel.*
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class MockBinder : Binder(), Serializable

@Parcelize
class MockIInterface : IInterface, Parcelable {
    override fun asBinder(): IBinder = MockBinder()
}

@Parcelize
class ServiceContainer(
    konst binder: MockBinder,
    konst iinterface: MockIInterface,
    konst binderArray: Array<IBinder>,
    konst binderList: List<IBinder>
) : Parcelable

fun box() = parcelTest { parcel ->
    konst test = ServiceContainer(MockBinder(), MockIInterface(), arrayOf(MockBinder()), listOf(MockBinder()))
    test.writeToParcel(parcel, 0)

    konst bytes = parcel.marshall()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)

    konst test2 = readFromParcel<ServiceContainer>(parcel)
}
