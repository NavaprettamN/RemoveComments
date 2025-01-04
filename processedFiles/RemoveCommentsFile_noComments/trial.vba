
Sub CommentExamples()
    Call GreetUser() 
    
    Dim i As Integer
    For i = 1 To 5
        Debug.Print "Count: " & i 
    Next i
End Sub

Sub GreetUser()
    Debug.Print "Hello, User!"
End Sub
