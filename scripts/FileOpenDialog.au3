#Region ;**** Directives created by AutoIt3Wrapper_GUI ****
#AutoIt3Wrapper_Outfile_x64=FileOpenDialog.exe
#EndRegion ;**** Directives created by AutoIt3Wrapper_GUI ****
#include <FileConstants.au3>
#include <MsgBoxConstants.au3>
; This autoit executable is used to facilitate uploading of a file from Selenium WebDriver software as Selenium does not have a built in mechanism for doing so.
; In some Selenium software, a click on a Browse button is executed RIGITH AFTER this autoit script is launched.
; This script will wait until the dialog titled $sTitle is activated and then enter the file name ($sFile) with <Enter> in the text box for file name
; $CmdLine is the command argv[] array with title in $CmdLine[1] and file name in $CmdLine[2]
UploadFile()

Func UploadFile()
	Local $sTitle = ""
	Local $buttonName = "Open"
	Local $sFile = "c:\automation\tmp.txt"

	; Get the window title
	if $CmdLine[0] > 0 Then
	  $sTitle = $CmdLine[1]
	else
	  $sTitle = "Upload File"
	  ;$sTitle = "Open"
	EndIf

	if $CmdLine[0] > 1 Then
	  $sFile = $CmdLine[2]
    EndIf

	if $CmdLine[0] > 2 Then
	  $sbuttonName = $CmdLine[3]
    EndIf

	WinActivate($sTitle)

	Local $hWnd = WinWaitActive($sTitle)

	;ConsoleWrite("hwnd: " & $hWnd + " " & "buttonName: " & $buttonName & @CRLF)

	;Send($sFile & "{ENTER}")
	Send($sFile)
	Sleep(1000)
	Send("{ENTER}")
	Send("{ENTER}")
	ControlClick($hWnd, "", $buttonName)

EndFunc   ;==>Example

