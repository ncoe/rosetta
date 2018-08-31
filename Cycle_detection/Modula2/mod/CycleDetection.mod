(*Translated from kotlin*)
MODULE CycleDetection;
FROM FormatString IMPORT FormatString;
FROM Terminal IMPORT WriteString,WriteLn,ReadChar;

TYPE IntToInt = PROCEDURE(INTEGER) : INTEGER;
TYPE Pair =
    RECORD
        a,b : INTEGER;
    END;

PROCEDURE Brent(f : IntToInt; x0 : INTEGER) : Pair;
VAR power,lam,tortoise,hare,mu,i : INTEGER;
BEGIN
    (* main phase: search successive powers of two *)
    power := 1;
    lam := 1;
    tortoise := x0;
    hare := f(x0);  (* f(x0) is the element/node next to x0. *)
    WHILE tortoise # hare DO
        (* time to start a new power of two? *)
        IF power = lam THEN
            tortoise := hare;
            power := power * 2;
            lam := 0
        END;
        hare := f(hare);
        INC(lam)
    END;

    (* Find the position of the first repetition of length 'lam' *)
    mu := 0;
    tortoise := x0;
    hare := x0;
    i := 0;
    WHILE i < lam DO
        hare := f(hare);
        INC(i)
    END;

    (* The distance between the hare and tortoise is now 'lam'.
       Next, the hare and tortoise move at same speed until they agree *)
    WHILE tortoise # hare DO
        tortoise := f(tortoise);
        hare := f(hare);
        INC(mu)
    END;

    RETURN Pair{lam,mu}
END Brent;

PROCEDURE Lambda(x : INTEGER) : INTEGER;
BEGIN
    RETURN (x * x + 1) MOD 255
END Lambda;

VAR
    buf : ARRAY[0..63] OF CHAR;
    x0,x,i : INTEGER;
    result : Pair;
BEGIN
    x0 := 3;
    x := x0;

    WriteString("[3");
    FOR i:=1 TO 40 DO
        x := Lambda(x);
        FormatString(", %i", buf, x);
        WriteString(buf)
    END;
    WriteString("]");
    WriteLn;

    result := Brent(Lambda, x0);
    FormatString("Cycle length = %i\nStart index  = %i\nCycle        = [", buf, result.a, result.b);
    WriteString(buf);

    x0 := 3;
    x := x0;
    FOR i:=1 TO result.b DO
        x := Lambda(x)
    END;
    FOR i:=1 TO result.a DO
        IF i > 1 THEN
            WriteString(", ")
        END;

        FormatString("%i", buf, x);
        WriteString(buf);
        x := Lambda(x)
    END;

    WriteString("]");
    WriteLn;

    ReadChar
END CycleDetection.
