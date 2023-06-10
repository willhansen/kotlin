enum class Color {
    BLACK, `WHI-TE`
}

@Anno(Color.`WHI-TE`)
annotation class Anno(konst color: Color)

// EXPECTED_ERROR: (kotlin:5:1) an enum annotation konstue must be an enum constant
// EXPECTED_ERROR: (other:-1:-1) 'WHI-TE' is an inkonstid Java enum konstue name
