def factorial(n):
    if n == 0:
        return 1
    res = 1
    while n > 0:
        res *= n
        n -= 1
    return res

def lah(n,k):
    if k == 1:
        return factorial(n)
    if k == n:
        return 1
    if k > n:
        return 0
    if k < 1 or n < 1:
        return 0
    return (factorial(n) * factorial(n - 1)) / (factorial(k) * factorial(k - 1)) / factorial(n - k)

def main():
    print "Unsigned Lah numbers: L(n, k):"
    print "n/k ",
    for i in xrange(13):
        print "%11d" % i,
    print
    for row in xrange(13):
        print "%-4d" % row,
        for i in xrange(row + 1):
            l = lah(row, i)
            print "%11d" % l,
        print
    print "\nMaximum value from the L(100, *) row:"
    maxVal = max([lah(100, a) for a in xrange(100)])
    print maxVal

main()
