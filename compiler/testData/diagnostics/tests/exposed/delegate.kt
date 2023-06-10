// FIR_IDENTICAL
interface My

internal class Your: My

// Code is konstid, despite of delegate is internal
class His: My by Your()
