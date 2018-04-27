MODULE Arrays;
(* Arrays in modula are bound-checked according to their definition. *)

(* Procedures can take an array of variable size.
 * Shown here is a single level for an array *)
PROCEDURE F(a : ARRAY OF INTEGER);
(* Variables can be declared as an array, but the bounds must be declared. *)
VAR b : ARRAY[0..63] OF INTEGER;
(* The bounds can start and end at arbitrary points. *)
VAR c : ARRAY[-5..5] OF INTEGER;
BEGIN
END F;

(* A type alias can be useful for initializing arrays.
 * Arrays can be multi-dimensional. *)
TYPE A = ARRAY[0..3] OF ARRAY[0..3] OF INTEGER;

VAR
    a : A;
    (* Alternate way of specifying the next dimension of an array. *)
    b : ARRAY [0..3], [0..3] OF INTEGER;
BEGIN
    (* Because a has a specific type declaration, the elements can be initialized all at once. *)
    a := A{{0,1,2,3},{4,5,6,7},{8,9,10,11},{12,13,14,15}};
    (* The type of a and the type of b are not assignment compatible.
     * initialization of the elements has to be done one by one. *)
    b[0,0] := 0;
    (* Syntax allows access in two ways *)
    b[1][1] := 0;

END Arrays.
