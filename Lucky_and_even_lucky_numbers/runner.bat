REM showing the first twenty lucky numbers
dub -- -j=1 -k=20

REM showing the first twenty even lucky numbers
dub -- -j=1 -k=20 --evenLucky

REM showing all lucky numbers between 6,000 and 6,100 (inclusive)
dub -- -j=6000 -k=-6100

REM showing all even lucky numbers in the same range as above
dub -- -j=6000 -k=-6100 --evenLucky

REM showing the 10,000th lucky number (extra credit)
dub -- -j=10000

REM showing the 10,000th even lucky number (extra credit)
dub -- -j=10000 --evenLucky
