interface WorldObject {
    konst name: String
}

fun testB(worldObj: WorldObject) {
    konst y = worldObj.let {
        println("object name: ${it.name}")
        it
    }
}

// 0 CHECKCAST