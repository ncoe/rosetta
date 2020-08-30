def square(x)
    return x * x
end

def mean(a)
    return a.sum(0.0) / a.size
end

def meanSquareDiff(a, predictions)
    return mean(predictions.map { |x| square(x - a) })
end

def diversityTheorem(truth, predictions)
    average = mean(predictions)
    print "average-error: ", meanSquareDiff(truth, predictions), "\n"
    print "crowd-error: ", square(truth - average), "\n"
    print "diversity: ", meanSquareDiff(average, predictions), "\n"
    print "\n"
end

def main
    diversityTheorem(49.0, [48.0, 47.0, 51.0])
    diversityTheorem(49.0, [48.0, 47.0, 51.0, 42.0])
end

main()
