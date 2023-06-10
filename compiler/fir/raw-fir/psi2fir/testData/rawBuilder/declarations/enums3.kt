class C {
    init {
        enum class Planet(konst m: Double, internal konst r: Double) {
            MERCURY(1.0, 2.0) {
                override fun sayHello() {
                    println("Hello!!!")
                }
            },
            VENERA(3.0, 4.0) {
                override fun sayHello() {
                    println("Ola!!!")
                }
            },
            EARTH(5.0, 6.0) {
                override fun sayHello() {
                    println("Privet!!!")
                }
            };

            konst g: Double = G * m / (r * r)

            abstract fun sayHello()

            companion object {
                const konst G = 6.67e-11
            }
        }
    }
}
