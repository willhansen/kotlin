expect interface I1
expect interface I2
expect interface I3<T>

expect object Holder {
    konst <T : I1> T.propertyWithReceiver: Unit
    konst <T : I2> T.propertyWithReceiver: Unit
    konst <T : I3<String>> T.propertyWithReceiver: Unit
    konst <T : I3<in String>> T.propertyWithReceiver: Unit
    konst <T : I3<out String>> T.propertyWithReceiver: Unit
    konst <T : I3<Int>> T.propertyWithReceiver: Unit
    konst <T : I3<in Int>> T.propertyWithReceiver: Unit
    konst <T : I3<out Int>> T.propertyWithReceiver: Unit
    konst <T : I3<Any>> T.propertyWithReceiver: Unit
    konst <T : I3<in Any>> T.propertyWithReceiver: Unit
    konst <T : I3<out Any>> T.propertyWithReceiver: Unit
    konst <T : I3<Any?>> T.propertyWithReceiver: Unit
    konst <T : I3<*>> T.propertyWithReceiver: Unit
    konst <T : Any> T.propertyWithReceiver: Unit
    konst <T> T.propertyWithReceiver: Unit
    konst I1.propertyWithReceiver: Unit
    konst I2.propertyWithReceiver: Unit
    konst Any.propertyWithReceiver: Unit
    konst Any?.propertyWithReceiver: Unit
    konst propertyWithReceiver: Unit
}
