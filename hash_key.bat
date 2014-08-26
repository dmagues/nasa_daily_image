@echo off
"C:\Program Files\Java\jdk1.7.0_02\bin\keytool.exe" -exportcert -alias androiddebugkey -keystore C:\Users\Daniel.Daniel-PC\.android\debug.keystore | "C:\openssl\bin\openssl.exe" sha1 -binary | "C:\openssl\bin\openssl.exe" base64
pause