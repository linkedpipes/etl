const winston = require("winston");
const moment = require("moment");
const {SPLAT} = require("triple-beam");

(function initialize() {
  const logger = winston.createLogger({
    "level": "info",
    "transports": [],
  });
  addConsoleLogger(logger);

  // Do not exit after the uncaught exception.
  logger.exitOnError = false;

  module.exports = logger;
}());

function addConsoleLogger(logger) {
  const timeFormat = () => moment().format("YYYY-MM-DD hh:mm:ss").trim();

  const consoleFormat = winston.format.printf((log) => {
    const time = timeFormat(log.timestamp);
    const level = log.level.toUpperCase();
    return `${time} [${level}] ${log.message}`;
  });

  logger.add(new winston.transports.Console({
    "timestamp": timeFormat,
    "format": winston.format.combine(
      parametersToMessage(),
      consoleFormat,
    ),
  }));
}

/**
 * Save SPLAT (arguments) into a message.
 */
function parametersToMessage() {
  return winston.format((info) => {
    if (info[SPLAT]) {
      return {
        ...info,
        "message": info.message + info[SPLAT].join(" "),
      };
    }
    return info;
  })();
}
