@echo off
echo Creating OBS Banking System Database...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "CREATE DATABASE IF NOT EXISTS obs_banking_system;"
echo Database created successfully!
pause
