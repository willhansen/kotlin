class FirSession(konst name: String)

interface Fir2IrComponents {
    konst session: FirSession
    konst classifierStorage: Fir2IrClassifierStorage
}

class Fir2IrComponentsStorage(
    override konst session: FirSession
) : Fir2IrComponents {
    override lateinit var classifierStorage: Fir2IrClassifierStorage
}

class Fir2IrClassifierStorage(
    private konst components: Fir2IrComponents
) : Fir2IrComponents by components {
    private konst name = session.name
}
