MODULE PermutationTest;
FROM FormatString IMPORT FormatString;
FROM RealStr IMPORT RealToStr;
FROM Terminal IMPORT WriteString,WriteLn,ReadChar;

VAR data : ARRAY[0..18] OF INTEGER;

PROCEDURE Init;
BEGIN
    data[0] := 85;
    data[1] := 88;
    data[2] := 75;
    data[3] := 66;
    data[4] := 25;
    data[5] := 29;
    data[6] := 83;
    data[7] := 39;
    data[8] := 97;
    data[9] := 68;
    data[10] := 41;
    data[11] := 10;
    data[12] := 49;
    data[13] := 16;
    data[14] := 65;
    data[15] := 32;
    data[16] := 92;
    data[17] := 28;
    data[18] := 98;
END Init;

PROCEDURE Pick(at,remain,accu,treat : INTEGER) : INTEGER;
VAR temp : INTEGER;
BEGIN
    IF remain=0 THEN
        IF accu>treat THEN
            RETURN 1;
        ELSE
            RETURN 0;
        END;
    END;

    temp := Pick(at-1, remain-1, accu+data[at-1], treat);
    IF at>remain THEN
        RETURN temp + Pick(at-1, remain, accu, treat);
    END;

    RETURN temp;
END Pick;

VAR
    treat,i,gt,le : INTEGER;
    total : REAL;
    str : ARRAY[0..32] OF CHAR;
BEGIN
    Init;
    treat := 0;
    total := 1.0;
    FOR i:=0 TO 8 DO
        treat := treat + data[i];
    END;
    FOR i:=19 TO 11 BY -1 DO
        total := total * FLOAT(i);
    END;
    FOR i:=9 TO 1 BY -1 DO
        total := total / FLOAT(i);
    END;
    gt := Pick(19,9,0,treat);
    le := TRUNC(total - FLOAT(gt));

    WriteString("<= : ");
    RealToStr(100.0*FLOAT(le)/total,str);
    WriteString(str);
    FormatString("%%  %i\n",str,le);
    WriteString(str);

    WriteString(" > : ");
    RealToStr(100.0*FLOAT(gt)/total,str);
    WriteString(str);
    FormatString("%%  %i\n",str,gt);
    WriteString(str);

    ReadChar;
END PermutationTest.
