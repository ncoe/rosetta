local N = 2
local base = 10
local c1 = 0
local c2 = 0

for k = 1, math.pow(base, N) - 1 do
    c1 = c1 + 1
    if k % (base - 1) == (k * k) % (base - 1) then
        c2 = c2 + 1
        io.write(k .. ' ')
    end
end

print()
print(string.format("Trying %d numbers instead of %d numbers saves %f%%", c2, c1, 100.0 - 100.0 * c2 / c1))
