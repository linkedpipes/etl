@echo OFF
if not exist "./frontend/node_modules" (
    echo Downloading libraries ...
    cd ./frontend
    call npm install
    cd ..
    echo Downloading libraries ... done
)
echo Libraries are presented
@echo ON
echo Starting Nodejs ...
cd frontend
set configFileLocation=../configuration.properties
call node server.js
cd ..