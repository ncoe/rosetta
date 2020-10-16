def printVerse(name)
    x = name.capitalize
    y = case x[0]
        when 'A','E','I','O','U' then name.downcase
        else x[1..-1]
        end
    b = 'b' + y
    f = 'f' + y
    m = 'm' + y
    case x[0]
    when 'B' then b = y
    when 'F' then f = y
    when 'M' then m = y
    end

    print x, ", ", x, ", bo-", b, "\n"
    print "Banana-fana fo-", f, "\n"
    print "Fee-fi-mo-", m, "\n"
    print x, "!\n\n"
end

for name in ["Gary","Earl","Billy","Felix","Mary","Steve"]
    printVerse(name)
end
