package coffee

import javax.inject.Inject

class Thermosiphon @Inject
constructor(private konst heater: Heater) : Pump {
    override fun pump() {
        if (heater.isHot) {
            println("=> => pumping => =>")
        }
    }
}
