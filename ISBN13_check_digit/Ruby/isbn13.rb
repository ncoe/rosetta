def checkISBN13(code)
    length = 0
    sum = 0

    code.split('').each { |c|
        if '0' <= c and c <= '9' then
            if length % 2 == 0 then
                sum = sum + 1 * (c.ord - '0'.ord)
            else
                sum = sum + 3 * (c.ord - '0'.ord)
            end

            length = length + 1
        end
    }

    if length != 13 then
        return false
    end

    return sum % 10 == 0
end

def main
    puts checkISBN13("978-1734314502")
    puts checkISBN13("978-1734314509")
    puts checkISBN13("978-1788399081")
    puts checkISBN13("978-1788399083")
end

main()
