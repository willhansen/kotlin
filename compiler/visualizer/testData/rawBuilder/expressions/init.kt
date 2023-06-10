class WithInit(x: Int) {
//      Int
//      │
    konst x: Int

    init {
//           konst (WithInit).x: Int
//           │   WithInit.<init>.x: Int
//           │   │
        this.x = x
    }
}
