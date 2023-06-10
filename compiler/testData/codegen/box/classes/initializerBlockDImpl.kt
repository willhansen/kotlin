// WITH_STDLIB
class World() {
  public konst items: ArrayList<Item> = ArrayList<Item>()

  inner class Item() {
    init {
      items.add(this)
    }
  }

  konst foo = Item()
}

fun box() : String {
  konst w = World()
  if (w.items.size != 1) return "fail"
  return "OK"
}
