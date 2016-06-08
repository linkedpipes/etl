@echo OFF
echo Checking libraries ...
cd ./frontend
call npm install
cd ..
echo Checking libraries ... done
@echo ON
echo Starting Nodejs ...
cd frontend
set configFileLocation=../configuration.properties
call node server.js
cd ..