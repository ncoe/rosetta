function printArray(a)
    io.write('[')
    if table.getn(a) > 0 then
        io.write(a[0])
    end

    local i = 1
    while i < table.getn(a) do
        io.write(", " .. a[i])
        i = i + 1
    end
    io.write(']')
end

function bellTriangle(n)
    local tri = {}
    for i=1,n do
        tri[i - 1] = {}
        for j=1,i do
            tri[i - 1][j - 1] = 0
        end
    end
    tri[1][0] = 1
    for i=2,n-1 do
        tri[i][0] = tri[i - 1][i - 2]
        for j=1,i-1 do
            tri[i][j] = tri[i][j - 1] + tri[i - 1][j - 1]
        end
    end
    return tri
end

function main()
    local bt = bellTriangle(51)
    print("First fifteen and fiftieth Bell numbers:")
    for i=1,15 do
        print(string.format("%2d: %.0f", i, bt[i][0]))
    end
    print(string.format("50: %.0f", bt[50][0]))
    print()
    print("The first ten rows of Bell's triangle:")
    for i=1,10 do
        printArray(bt[i])
        print()
    end
end

main()
