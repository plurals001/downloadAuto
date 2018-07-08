Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "cmd /c sources\manager\manager.bat"
oShell.Run strArgs, 0, false