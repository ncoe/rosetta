fun pancake(n: Int): Int {
    var gap = 2
    var sum = 2
    var adj = -1
    while (sum < n) {
        adj++
        gap = gap * 2 - 1
        sum += gap
    }
    return n + adj
}

fun main() {
    for (i in 0 until 4) {
        for (j in 1 until 6) {
            val n = i * 5 + j
            print("p(%2d) = %2d  ".format(n, pancake(n)))
        }
        println()
    }
}
