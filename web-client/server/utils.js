const fileSystem = require("fs");
const logger = require("./logging");

module.exports = {
  "streamToFile": streamToFile,
  "fileToStream": fileToStream,
  "fileToJson": fileToJson,
  "fileToResponse": fileToResponse,
  "mkDir": mkDir,
  "remove": removeFile,
};

function streamToFile(stream, path) {
  const outputStream = fileSystem.createWriteStream(path);
  stream.pipe(outputStream);
}

function fileToStream(path, stream) {
  const inputStream = fileSystem.createReadStream(path);
  inputStream.pipe(stream);
}

function fileToJson(path) {
  return JSON.parse(fileSystem.readFileSync(path, "utf8"));
}

function fileToResponse(path, response, type) {
  if (!fileSystem.existsSync(path)) {
    response.status(404);
    return;
  }
  fileSystem.stat(path, (error, stat) => {
    if (error) {
      logger.error("Can't get file stats.", error);
      response.status(500);
      return;
    }
    response.writeHead(200, {
      "Content-Type": type,
      "Content-Length": stat.size,
    });
    fileToStream(path, response);
  });
}

function mkDir(path) {
  if (!fileSystem.existsSync(path)) {
    fileSystem.mkdirSync(path);
  }
}

function removeFile(path) {
  return new Promise((fulfill, reject) => {
    fileSystem.unlink(path, callbackToPromise(fulfill, reject));
  });
}

function callbackToPromise(fulfill, reject) {
  return (error) => {
    if (error) {
      reject(error);
    } else {
      fulfill();
    }
  };
}
