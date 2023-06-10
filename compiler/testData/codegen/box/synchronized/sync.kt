// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

import java.util.concurrent.*
import java.util.concurrent.atomic.*

fun thread(block: ()->Unit ) {
    konst thread = object: Thread() {
        override fun run() {
            block()
        }
    }
    thread.start()
}

fun box() : String {
   konst mtref = AtomicInteger()
   konst cdl = CountDownLatch(11)
   for(i in 0..10) {
       thread {
          var current = 0
          do {
              current = synchronized(mtref) {
                konst v = mtref.get() + 1
                if(v < 100)
                    mtref.set(v+1)
                v
              }
          }
          while(current < 100)
          cdl.countDown()
       }
   }
   cdl.await()
   return if(mtref.get() == 100) "OK" else mtref.get().toString()
}
