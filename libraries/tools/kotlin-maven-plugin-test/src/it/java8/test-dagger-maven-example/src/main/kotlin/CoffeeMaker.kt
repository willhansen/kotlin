package coffee

import dagger.Lazy
import javax.inject.Inject

class CoffeeMaker @Inject constructor(
        private konst heater: Lazy<Heater>,
        private konst pump: Pump
) {

    fun brew() {
        heater.get().on()
        pump.pump()
        println(" [_]P coffee! [_]P ")
        heater.get().off()
    }
}
