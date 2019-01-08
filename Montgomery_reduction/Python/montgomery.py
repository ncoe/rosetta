class Montgomery:
    BASE = 2

    def __init__(self, m):
        self.m = m
        self.n = m.bit_length()
        self.rrm = (1 << (self.n * 2)) % m

    def reduce(self, t):
        a = t
        for i in xrange(self.n):
            if (a & 1) == 1:
                a = a + self.m
            a = a >> 1
        if a >= self.m:
            a = a - self.m
        return a

# Main
m = 750791094644726559640638407699
x1 = 540019781128412936473322405310
x2 = 515692107665463680305819378593

mont = Montgomery(m)
t1 = x1 * mont.rrm
t2 = x2 * mont.rrm

r1 = mont.reduce(t1)
r2 = mont.reduce(t2)
r = 1 << mont.n

print "b : ", Montgomery.BASE
print "n : ", mont.n
print "r : ", r
print "m : ", mont.m
print "t1: ", t1
print "t2: ", t2
print "r1: ", r1
print "r2: ", r2
print
print "Original x1       :", x1
print "Recovered from r1 :", mont.reduce(r1)
print "Original x2       :", x2
print "Recovered from r2 :", mont.reduce(r2)

print "\nMontgomery computation of x1 ^ x2 mod m:"
prod = mont.reduce(mont.rrm)
base = mont.reduce(x1 * mont.rrm)
exp = x2
while exp.bit_length() > 0:
    if (exp & 1) == 1:
        prod = mont.reduce(prod * base)
    exp = exp >> 1
    base = mont.reduce(base * base)
print mont.reduce(prod)
print "\nAlternate computation of x1 ^ x2 mod m :"
print pow(x1, x2, m)
