"use strict";

const HTTP = {
  "INVALID_REQUEST": 400,
  "NOT_FOUND": 404,
  "SERVER_ERROR": 500,
};

const CONTENT = {
  "JSONLD": "application/ld+json",
}

module.exports = {
  "HTTP": HTTP,
  "CONTENT": CONTENT,
};
