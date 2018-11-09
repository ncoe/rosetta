MODULE SumTo100;
FROM FormatString IMPORT FormatString;
FROM Terminal IMPORT WriteString,WriteLn,ReadChar;

PROCEDURE Evaluate(code : INTEGER) : INTEGER;
VAR
    value,number,power,k : INTEGER;
BEGIN
    value := 0;
    number := 0;
    power := 1;

    FOR k:=9 TO 1 BY -1 DO
        number := power * k + number;
        IF code MOD 3 = 0 THEN
            (* ADD *)
            value := value + number;
            number := 0;
            power := 1
        ELSIF code MOD 3 = 1 THEN
            (* SUB *)
            value := value - number;
            number := 0;
            power := 1
        ELSE
            (* CAT *)
            power := power * 10
        END;
        code := code / 3
    END;

    RETURN value
END Evaluate;

PROCEDURE Print(code : INTEGER);
VAR
    expr,buf : ARRAY[0..63] OF CHAR;
    a,b,k,p : INTEGER;
BEGIN
    a := 19683;
    b := 6561;
    p := 0;

    FOR k:=1 TO 9 DO
        IF (code MOD a) / b = 0 THEN
            IF k > 1 THEN
                expr[p] := '+';
                INC(p)
            END
        ELSIF (code MOD a) / b = 1 THEN
            expr[p] := '-';
            INC(p)
        END;

        a := b;
        b := b / 3;
        expr[p] := CHR(k + 30H);
        INC(p)
    END;
    expr[p] := 0C;

    FormatString("%9i = %s\n", buf, Evaluate(code), expr);
    WriteString(buf)
END Print;

(* Main *)
CONST nexpr = 13122;
VAR
    i,j : INTEGER;
    best,nbest,test,ntest,limit : INTEGER;
    buf : ARRAY[0..63] OF CHAR;
BEGIN
    WriteString("Show all solution that sum to 100");
    WriteLn;
    FOR i:=0 TO nexpr-1 DO
        IF Evaluate(i) = 100 THEN
            Print(i)
        END
    END;
    WriteLn;

    WriteString("Show the sum that has the maximum number of solutions");
    WriteLn;
    nbest := -1;
    FOR i:=0 TO nexpr-1 DO
        test := Evaluate(i);
        IF test > 0 THEN
            ntest := 0;
            FOR j:=0 TO nexpr-1 DO
                IF Evaluate(j) = test THEN
                    INC(ntest)
                END;
                IF ntest > nbest THEN
                    best := test;
                    nbest := ntest
                END
            END
        END
    END;
    FormatString("%i has %i solutions\n\n", buf, best, nbest);
    WriteString(buf);

    WriteString("Show the lowest positive number that can't be expressed");
    WriteLn;
    FOR i:=0 TO 123456789 DO
        FOR j:=0 TO nexpr-1 DO
            IF i = Evaluate(j) THEN
                BREAK
            END
        END;
        IF i # Evaluate(j) THEN
            BREAK
        END
    END;
    FormatString("%i\n\n", buf, i);
    WriteString(buf);

    WriteString("Show the ten highest numbers that can be expressed");
    WriteLn;
    limit := 123456789 + 1;
    FOR i:=1 TO 10 DO
        best := 0;
        FOR j:=0 TO nexpr-1 DO
            test := Evaluate(j);
            IF (test < limit) AND (test > best) THEN
                best := test
            END
        END;
        FOR j:=0 TO nexpr-1 DO
            IF Evaluate(j) = best THEN
                Print(j)
            END
        END;
        limit := best
    END;

    (* Translation of C *)
    ReadChar
END SumTo100.
