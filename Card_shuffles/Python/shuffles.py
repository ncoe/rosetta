import random

def riffleShuffle(va, flips):
    nl = va
    for n in range(flips):
        #cut the deck at the middle +/- 10%, remove the second line of the formula for perfect cutting
        cutPoint = len(nl)/2 + random.choice([-1, 1]) * random.randint(0, len(va)/10)

        # split the deck
        left = nl[0:cutPoint]
        right = nl[cutPoint:]

        del nl[:]
        while (len(left) > 0 and len(right) > 0):
            #allow for imperfect riffling so that more than one card can come form the same side in a row
            #biased towards the side with more cards
            #remove the if and else and brackets for perfect riffling
            if (random.uniform(0, 1) >= len(left) / len(right) / 2):
                nl.append(right.pop(0))
            else:
                nl.append(left.pop(0))
        if (len(left) > 0):
            nl = nl + left
        if (len(right) > 0):
            nl = nl + right
    return nl

def overhandShuffle(va, passes):
    mainHand = va
    for n in range(passes):
        otherHand = []
        while (len(mainHand) > 0):
            #cut at up to 20% of the way through the deck
            cutSize = random.randint(0, len(va) / 5) + 1
            temp = []

            #grab the next cut up to the end of the cards left in the main hand
            i=0
            while (i<cutSize and len(mainHand) > 0):
                temp.append(mainHand.pop(0))
                i = i + 1

            #add them to the cards in the other hand, sometimes to the front sometimes to the back
            if (random.uniform(0, 1) >= 0.1):
                #front most of the time
                otherHand = temp + otherHand
            else:
                otherHand = otherHand + temp
        #move the cards back to the main hand
        mainHand = otherHand
    return mainHand

print "Riffle shuffle"
nums = [x+1 for x in range(21)]
print nums
print riffleShuffle(nums, 10)
print

print "Riffle shuffle"
nums = [x+1 for x in range(21)]
print nums
print riffleShuffle(nums, 1)
print

print "Overhand shuffle"
nums = [x+1 for x in range(21)]
print nums
print overhandShuffle(nums, 10)
print

print "Overhand shuffle"
nums = [x+1 for x in range(21)]
print nums
print overhandShuffle(nums, 1)
print

print "Library shuffle"
nums = [x+1 for x in range(21)]
print nums
random.shuffle(nums)
print nums
print
