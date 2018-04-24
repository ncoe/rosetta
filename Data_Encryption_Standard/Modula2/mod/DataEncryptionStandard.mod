MODULE DataEncryptionStandard;
FROM SYSTEM IMPORT BYTE,ADR;
FROM DES IMPORT DES,Key1,Create,Destroy,EncryptECB,DecryptECB;
FROM FormatString IMPORT FormatString;
FROM Terminal IMPORT WriteString,WriteLn,ReadChar;

PROCEDURE PrintHexBytes(str : ARRAY OF BYTE; limit : INTEGER);
VAR
    buf : ARRAY[0..7] OF CHAR;
    i,v : INTEGER;
BEGIN
    i := 0;
    WHILE i<limit DO
        v := ORD(str[i]);
        FormatString("%h", buf, v);
        WriteString(buf);
        INC(i);
    END
END PrintHexBytes;

TYPE BA = ARRAY[0..15] OF BYTE;
VAR
    plain,encrypt : BA;
    key : ARRAY[0..0] OF Key1;
    cipher : DES;
BEGIN
    plain := BA{38H, 37H, 38H, 37H, 38H, 37H, 38H, 37H, 38H, 37H, 38H, 37H, 38H, 37H, 38H, 37H};

    key[0] := Key1{0EH, 32H, 92H, 32H, 0EAH, 6DH, 0DH, 73H};
    cipher := Create(key);

    WriteString("plain: ");
    WriteString(plain);
    WriteLn;

    EncryptECB(cipher,ADR(plain),ADR(encrypt),16);

    WriteString("encrypt: ");
    PrintHexBytes(encrypt, 16);
    WriteLn;

    DecryptECB(cipher,ADR(encrypt),ADR(plain),16);

    WriteString("plain: ");
    WriteString(plain);
    WriteLn;

    Destroy(cipher);
    ReadChar
END DataEncryptionStandard.
