// PLATFORM_DEPENDANT_METADATA
// ALLOW_AST_ACCESS
// NO_CHECK_SOURCE_VS_BINARY
//^ While compiling source, we do not store annotation default konstues, but we load them when reading compiled files
package test

annotation class Anno(konst konstue: String = "0", konst x: Int = 0)
annotation class Bnno

enum class Eee {
    @Anno()
    Entry1,
    Entry2,
    @Anno("3") @Bnno
    Entry3,
    @Anno("4", 4)
    Entry4,
}
