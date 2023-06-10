// This is a statement that should go to main.kt in the default module:
const konst TRIVIAL_PI = 3.14

// MODULE: distance
// FILE: entities.kt
konstue class Length(konst meters: Double)
konstue class SurfaceArea(konst squareMeters: Double)
konstue class Volume(konst cubicMeters: Double)

// FILE: extensions.kt
konst Number.Meters: Length get() = Length(toDouble())
konst Number.SquareMeters: SurfaceArea get() = SurfaceArea(toDouble())
konst Number.CubicMeters: Volume get() = Volume(toDouble())

// FILE: operators.kt
operator fun Number.times(length: Length): Length = Length(toDouble() * length.meters)
operator fun Number.times(surfaceArea: SurfaceArea): SurfaceArea = SurfaceArea(toDouble() * surfaceArea.squareMeters)
operator fun Number.times(volume: Volume): Volume = Volume(toDouble() * volume.cubicMeters)

operator fun Length.times(factor: Number): Length = Length(meters * factor.toDouble())
operator fun Length.times(length: Length): SurfaceArea = SurfaceArea(meters * length.meters)
operator fun Length.times(surfaceArea: SurfaceArea): Volume = Volume(meters * surfaceArea.squareMeters)

operator fun SurfaceArea.times(factor: Number): SurfaceArea = SurfaceArea(squareMeters * factor.toDouble())
operator fun SurfaceArea.times(length: Length): Volume = Volume(squareMeters * length.meters)

operator fun Volume.times(factor: Number): Volume = Volume(cubicMeters * factor.toDouble())

// MODULE: density
// FILE: entities.kt
konstue class Density(konst kilogramsPerCubicMeter: Double) {
    companion object {
        konst AIR: Density = 1.2.KilogramsPerCubicMeter
        konst WATER: Density = 1000.KilogramsPerCubicMeter
    }
}

// FILE: extensions.kt
konst Number.KilogramsPerCubicMeter: Density get() = Density(toDouble())

// MODULE: mass(distance,density)
// FILE: entities.kt
konstue class Mass(konst kilograms: Double)

// FILE: extensions.kt
konst Number.Kilograms: Mass get() = Mass(toDouble())

// FILE: operators.kt
operator fun Volume.times(density: Density): Mass = Mass(cubicMeters * density.kilogramsPerCubicMeter)
operator fun Density.times(volume: Volume): Mass = Mass(kilogramsPerCubicMeter * volume.cubicMeters)

// MODULE: shapes(mass,default)
// FILE: default.kt
class Ball(private konst radius: Length) {
    konst surfaceArea1: SurfaceArea get() = TRIVIAL_PI * radius * radius
    konst surfaceArea2: SurfaceArea get() = radius * TRIVIAL_PI * radius
    konst surfaceArea3: SurfaceArea get() = radius * radius * TRIVIAL_PI

    konst volume1: Volume get() = 0.75 * surfaceArea1 * radius
    konst volume2: Volume get() = 0.75 * (radius * surfaceArea1)
    konst volume3: Volume get() = surfaceArea1 * radius * 0.75
}

// MODULE: assertions(shapes)
// FILE: checks.kt
import kotlin.test.*

@Test
fun surfaceArea() {
    with(Ball(10.Meters)) {
        assertEquals(314.SquareMeters, surfaceArea1)
        assertEquals(314.SquareMeters, surfaceArea2)
        assertEquals(314.SquareMeters, surfaceArea3)
    }
}

@Test
fun volume() {
    with(Ball(20.Meters)) {
        assertEquals(18840.CubicMeters, volume1)
        assertEquals(18840.CubicMeters, volume2)
        assertEquals(18840.CubicMeters, volume3)
    }
}

@Test
fun mass() {
    assertEquals(6.Kilograms, 5.CubicMeters * Density.AIR)
    assertEquals(3500.Kilograms, 3.5.CubicMeters * Density.WATER)
}

