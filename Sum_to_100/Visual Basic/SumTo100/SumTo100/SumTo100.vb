Module SumTo100

    Sub Main()
        'All unique expressions that have a plus sign in front of the 1; calculated in parallel
        Dim expressionsPlus = Enumerable.Range(0, Math.Pow(3, 8)).AsParallel().Select(Function(i) New Expression(i, 1))
        'All unique expressions that have a minus sign in front of the 1; calculated in parallel
        Dim expressionsMinus = Enumerable.Range(0, Math.Pow(3, 8)).AsParallel().Select(Function(i) New Expression(i, -1))
        Dim expressions = expressionsPlus.Concat(expressionsMinus)
        Dim results As Dictionary(Of Integer, List(Of Expression))
        results = New Dictionary(Of Integer, List(Of Expression))
        For Each e In expressions
            If Not results.Keys.Contains(e.GetValue()) Then
                results(e.GetValue()) = New List(Of Expression)
            End If
            results(e.GetValue()).Add(e)
        Next

        Console.WriteLine("Show all solutions that sum to 100")
        For Each e In results(100)
            Console.WriteLine(" {0}", e)
        Next
        Console.WriteLine()

        Console.WriteLine("Show the sum that has the maximum number of solutions (from zero to infinity)")
        Dim summary = results.Keys.Select(Function(k) Tuple.Create(k, results(k).Count))
        Dim maxSols = summary.Aggregate(Function(a, b) If(a.Item2 > b.Item2, a, b))
        Console.WriteLine("  The sum {0} has {1} solutions.", maxSols.Item1, maxSols.Item2)
        Console.WriteLine()

        Console.WriteLine("Show the lowest positive sum that can't be expressed (has no solutions), using the rules for this task")
        Dim lowestPositive = Enumerable.Range(1, Integer.MaxValue).First(Function(x) Not results.Keys.Contains(x))
        Console.WriteLine("  {0}", lowestPositive)
        Console.WriteLine()

        Console.WriteLine("Show the ten highest numbers that can be expressed using the rules for this task (extra credit)")
        Dim highest = From k In results.Keys
                      Order By k Descending
                      Select k
        For Each x In highest.Take(10)
            Console.WriteLine("  {0}", x)
        Next
    End Sub

    Enum Operations
        Plus
        Minus
        Join
    End Enum

    Public Class Expression
        Private ReadOnly Value As Integer 'What this expression sums up to
        Private ReadOnly _one As Integer
        Private ReadOnly Gaps(8) As Operations
        '123456789 => there are 8 "gaps" between each number
        '             With 3 possibilities For Each gap: plus, minus, Or join

        Public Sub New(serial As Integer, one As Integer)
            _one = one
            'This represents "serial" as a base 3 number, each Gap expression being a base-three digit
            Dim divisor = 2187 'Math.Pow(3, 7)
            Dim times As Integer
            For i As Integer = 0 To 7
                times = Math.DivRem(serial, divisor, serial)
                divisor /= 3
                If 0 = times Then
                    Gaps(i) = Operations.Join
                ElseIf 1 = times Then
                    Gaps(i) = Operations.Minus
                Else
                    Gaps(i) = Operations.Plus
                End If
            Next

            Value = Evaluate()
        End Sub

        Public Function GetValue()
            Return Value
        End Function

        Private Function Evaluate()
            Dim numbers(9) As Integer
            Dim nc = 0
            Dim ops As List(Of Integer)
            Dim a = 1
            ops = New List(Of Integer)
            For i = 0 To 7
                If Operations.Join = Gaps(i) Then
                    a = a * 10 + (i + 2)
                Else
                    If a > 0 Then
                        If nc = 0 Then
                            a *= _one
                        End If
                        numbers(nc) = a
                        nc = nc + 1
                        a = i + 2
                    End If
                    ops.Add(Gaps(i))
                End If
            Next
            If nc = 0 Then
                a *= _one
            End If
            numbers(nc) = a
            nc = nc + 1
            Dim ni = 0
            Dim left = numbers(ni)
            ni = ni + 1
            For Each operation In ops
                Dim right = numbers(ni)
                ni = ni + 1
                If operation = Operations.Plus Then
                    left = left + right
                Else
                    left = left - right
                End If
            Next
            Return left
        End Function

        Public Overrides Function ToString() As String
            Dim ret = _one.ToString()
            For i = 0 To 7
                Select Case Gaps(i)
                    Case Operations.Plus
                        ret += "+"
                    Case Operations.Minus
                        ret += "-"
                End Select
                ret += (i + 2).ToString()
            Next
            Return ret
        End Function
    End Class
End Module
