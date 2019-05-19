Module Module1

    Sub Move(n As Integer, fromPeg As Integer, toPeg As Integer, viaPeg As Integer)
        If n > 0 Then
            Move(n - 1, fromPeg, viaPeg, toPeg)
            Console.WriteLine("Move disk from {0} to {1}", fromPeg, toPeg)
            Move(n - 1, viaPeg, toPeg, fromPeg)
        End If
    End Sub

    Sub Main()
        Move(4, 1, 2, 3)
        Console.WriteLine("Puzzle completed!")
    End Sub

End Module
