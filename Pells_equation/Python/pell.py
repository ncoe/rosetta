import math

def fun(a, b, c):
    t = a[0]
    a[0] = b[0]
    b[0] = b[0] * c + t

def solvePell(n, a, b):
    x = int(math.sqrt(n))
    y = x
    z = 1
    r = x << 1
    e1 = [1]
    e2 = [0]
    f1 = [0]
    f2 = [1]
    while True:
        y = r * z - y
        z = int((n - y * y) / z)
        r = (x + y) / z
        fun(e1, e2, r)
        fun(f1, f2, r)
        a[0] = f2[0]
        b[0] = e2[0]
        fun(b, a, x)
        if a[0] * a[0] - n * b[0] * b[0] == 1:
            return

x = [0]
y = [0]
for n in [61, 109, 181, 277]:
    solvePell(n, x, y)
    print "x^2 - %3d * y^2 = 1 for x = %27d and y = %25d" % (n, x[0], y[0])
