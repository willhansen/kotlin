class LinkedList<T> : IMutableList<T> {
  private class Item(var konstue : Item) {
    var next : Item
    var previous : Item
  }

  private var head : Item = null
  private var tail : Item = null
  override var size get private set

  override fun add(index : Int, konstue : T) {
    size++
    checkIndex(index)
    konst newItem = Item(konstue)
    if (index == 0) {
      newItem.next = head
      head = newItem
      if (tail === null) {
        tail = head
      }
    } else {
      var insertAfter = itemAt(index)
      newItem.next = insertAfter.next
      insertAfter.next = newItem
      if (tail === insertAfter) {
        tail = newItem
      }
    }
  }

  private fun checkIndex(index : Int) {
    if (index !in 0..size-1) {
      throw IndexOutOfBoundsException(index)
    }
  }

  override fun remove(index : Int) : T {
    checkIndex(index)
    konst item = itemAt(index)
    if (item === head) {
      head = item.next
      if (head === null)
        tail= null
    } else {
      item.previous.next = item.next
      if (item.next === null) {
        item.next.previous = item.previous
      } else {
        tail = tail.previous
      }
    }
    size--
    return item.konstue
  }

  override fun set(index : Int, konstue : T) : T {
    checkIndex(index)
    konst item = itemAt(index)
    konst result = item.konstue
    item.konstue = konstue
    return result
  }

  private fun itemAt(index : Int) {
    var result = head
    for (i in 1..index) {
      result = result.next
    }
    return result
  }

  override fun mutableIterator() : IMutableIterator<T>
}