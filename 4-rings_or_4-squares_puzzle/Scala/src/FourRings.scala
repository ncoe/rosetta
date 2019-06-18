object FourRings {
  def isValid(unique: Boolean, needle: Integer, haystack: Integer*): Boolean
  = !unique || !haystack.contains(needle)

  def fourSquare(low: Int, high: Int, unique: Boolean, print: Boolean): Unit = {
    var count = 0
    if (print) {
      println("a b c d e f g")
    }
    (low to high).foreach(a => (low to high).foreach(b => if (isValid(unique, a, b)) {
      val fp = a + b
      (low to high).foreach(c => if (isValid(unique, c, a, b)) {
        (low to high).foreach(d => if (isValid(unique, d, a, b, c) && fp == b + c + d) {
          (low to high).foreach(e => if (isValid(unique, e, a, b, c, d)) {
            (low to high).foreach(f => if (isValid(unique, f, a, b, c, d, e) && fp == d + e + f) {
              (low to high).foreach(g => if (isValid(unique, g, a, b, c, d, e, f) && fp == f + g) {
                count = count + 1
                if (print) {
                  printf("%d %d %d %d %d %d %d%n", a, b, c, d, e, f, g)
                }
              })
            })
          })
        })
      })
    }))
    if (unique) {
      printf("There are %d unique solutions in [%d, %d]%n", count, low, high)
    } else {
      printf("There are %d non-unique solutions in [%d, %d]%n", count, low, high)
    }
  }

  def main(args: Array[String]): Unit = {
    fourSquare(1, 7, unique = true, print = true)
    fourSquare(3, 9, unique = true, print = true)
    fourSquare(0, 9, unique = false, print = false)
  }
}
