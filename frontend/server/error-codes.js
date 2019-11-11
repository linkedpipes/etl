/**
 * List of error types returned by API gate.
 */
module.exports = {
  // Connection to a server/service was refused.
  "CONNECTION": "CONNECTION",
  // User request is invalid.
  "INVALID_REQUEST": "INVALID_REQUEST",
  // Resource needed for processing of the request is missing.
  "MISSING": "MISSING",
  // Error without closer specification, we should avoid using this.
  "ERROR": "ERROR"
};