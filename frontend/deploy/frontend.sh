#!/usr/bin/env bash

echo Checking libraries ...
cd ./frontend
npm install
cd ..
echo Checking libraries ... done
echo Starting Nodejs ...
cd frontend
export configFileLocation=../configuration.properties
node server.js
cd ..