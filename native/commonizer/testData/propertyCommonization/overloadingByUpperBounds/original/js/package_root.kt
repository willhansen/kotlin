interface I1
interface I2
interface I3<T>

object Holder {
    konst <T : I1> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I2> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<String>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<in String>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<out String>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<Int>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<in Int>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<out Int>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<Any>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<in Any>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<out Any>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<Any?>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : I3<*>> T.propertyWithReceiver: Unit get() = Unit
    konst <T : Any> T.propertyWithReceiver: Unit get() = Unit
    konst <T> T.propertyWithReceiver: Unit get() = Unit
    konst I1.propertyWithReceiver: Unit get() = Unit
    konst I2.propertyWithReceiver: Unit get() = Unit
    konst Any.propertyWithReceiver: Unit get() = Unit
    konst Any?.propertyWithReceiver: Unit get() = Unit
    konst propertyWithReceiver: Unit get() = Unit
}
