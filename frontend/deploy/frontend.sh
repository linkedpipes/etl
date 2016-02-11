if [ ! -d "./frontend/node_modules" ]; then
    echo Downloading libraries ...
    cd ./frontend
    npm install
    cd ..
    echo Downloading libraries ... done
fi
echo Libraries are presented
echo Starting Nodejs ...
cd frontend
export configFileLocation=../configuration.properties
node server.js
cd ..