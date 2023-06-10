class NoPrimary {
//      String
//      │
    konst x: String

    constructor(x: String) {
//           konst (NoPrimary).x: String
//           │   NoPrimary.<init>.x: String
//           │   │
        this.x = x
    }

    constructor(): this("")
}
