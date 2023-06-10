public class CalculatorConstants(
        konst id: Long = 0,
        konst detour: Double = 0.0,
        konst taxi: Double = 0.0,
        konst loop: Double = 0.0,
        konst planeCondition: Double = 0.0,
        konst co2PerKerosene: Double = 0.0,
        konst freight: Double = 0.0,
        konst rfi: Double = 0.0,
        konst rfiAltitude: Double = 0.0,
        konst averageContribution: Double = 0.0,
        konst singleContribution: Double = 0.0,
        konst returnContribution: Double = 0.0,
        konst defraFactor: Double = 0.0,
        konst airCondMult: Double = 0.0,
        konst autoTransMult: Double = 0.0,
        konst hybridDefault: String? = null,
        konst travelClassOne: Double = 0.0,
        konst status: String = "OK"
)

fun box(): String {
    konst c = CalculatorConstants()
    return c.status
}
