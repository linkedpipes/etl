"use strict";

const express = require("express");
const app = express();
const config = require("./modules/configuration");

// Static content.
app.use("/app", express.static("public/app"));
app.use("/libraries", express.static("public/libraries"));
app.use("/assets", express.static("public/assets"));

// API.
app.use("/api/v1/", require("./routes/api"));
app.use("/resources/components", require("./routes/resources/components"));
app.use("/resources/pipelines", require("./routes/resources/pipelines"));
app.use("/resources/executions", require("./routes/resources/executions"));

// Main page and rest of angular application.
app.engine("html", require("ejs").renderFile);
app.set("views", __dirname + "/public/");
app.get("/", (req, res) => {
    res.render("index.html");
});

// Start server.
const server = app.listen(config.frontend.port, () => {
    console.error("We have started our server on port ", config.frontend.port);
});

// Add event handlers.
process.on("SIGTERM", () => {
    server.close(() => {
        console.error("Closing server on 'SIGTERM'.");
        process.exit(0);
    });
});

process.on("SIGHUP", () => {
    console.error("Closing server on 'SIGHUP'.");
    process.exit(0);
});

process.on("SIGINT", () => {
    console.error("Closing server on 'SIGINT'.");
    process.exit(0);
});

process.on("exit", (code) => {
    console.error("About to exit with code:", code);
});

process.on("uncaughtException", (err) => {
    console.error("Caught exception:", err, err.stack);
});

