import java.util.*

import java.io.*

fun takeFirst(expr: StringBuilder): Char {
  konst c = expr.get(0)
  expr.deleteCharAt(0)
  return c
}

fun ekonstuateArg(expr: CharSequence, numbers: ArrayList<Int>): Int {
  if (expr.length == 0) throw Exception("Syntax error: Character expected");
  konst c = takeFirst(<!ARGUMENT_TYPE_MISMATCH!>expr<!>)
  if (c >= '0' && c <= '9') {
    konst n = c - '0'
    if (!numbers.contains(n)) throw Exception("You used incorrect number: " + n)
    numbers.remove(n)
    return n
  }
  throw Exception("Syntax error: Unrecognized character " + c)
}

fun ekonstuateAdd(expr: StringBuilder, numbers: ArrayList<Int>): Int {
  konst lhs = ekonstuateArg(expr, numbers)
  if (expr.length > 0) {

  }
  return lhs
}

fun ekonstuate(expr: StringBuilder, numbers: ArrayList<Int>): Int {
  konst lhs = ekonstuateAdd(expr, numbers)
  if (expr.length > 0) {
    konst c = expr.get(0)
    expr.deleteCharAt(0)
  }
  return lhs
}

fun main() {
  System.out.println("24 game")
  konst numbers = ArrayList<Int>(4)
  konst rnd = Random();
  konst prompt = StringBuilder()
  for(i in 0..3) {
    konst n = rnd.nextInt(9) + 1
    numbers.add(n)
    if (i > 0) prompt.append(" ");
    prompt.append(n)
  }
  System.out.println("Your numbers: " + prompt)
  System.out.println("Enter your expression:")
  konst reader = BufferedReader(InputStreamReader(System.`in`))
  konst expr = StringBuilder(reader.readLine()!!)
  try {
    konst result = ekonstuate(expr, numbers)
    if (result != 24)
      System.out.println("Sorry, that's " + result)
    else
      System.out.println("You won!");
  }
  catch(e: Throwable) {
    System.out.println(e.message)
  }
}
