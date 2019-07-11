((definition) => {
  if (typeof define === "function" && define.amd) {
    define([
      "../app-service/vocabulary"
    ], definition);
  }
})((vocab) => {

  const LP = vocab.LP;

  function convert(status, defaultToRunning) {
    switch (status) {
      case LP.EXEC_CANCELLED:
        return {
          "name": "done",
          "style": {
            "color": "#ff9900"
          }
        };
      case LP.EXEC_QUEUED:
        return {
          "name": "hourglass_empty",
          "style": {
            "color": "black"
          }
        };
      case LP.EXEC_INITIALIZING:
      case LP.EXEC_RUNNING:
        return {
          "name": "directions_run",
          "style": {
            "color": "blue"
          }
        };
      case LP.EXEC_FINISHED:
        return {
          "name": "done",
          "style": {
            "color": "green"
          }
        };
      case LP.EXEC_FAILED:
        return {
          "name": "error_outline",
          "style": {
            "color": "red"
          }
        };
      case LP.EXEC_CANCELLING:
        return {
          "name": "run",
          "style": {
            "color": "#ff9900"
          }
        };
      case LP.EXEC_UNRESPONSIVE:
        return {
          "name": "help_outline",
          "style": {
            "color": "orange"
          }
        };
      case LP.EXEC_INVALID:
      case LP.EXEC_DANGLING:
        return {
          "name": "help_outline",
          "style": {
            "color": "red"
          }
        };
      default:
        if (defaultToRunning) {
          return {
            "name": "hourglass_empty",
            "style": {
              "color": "black"
            }
          };
        }
        console.error("Invalid execution status: ", status);
        break;
    }
  }

  return convert;

});
