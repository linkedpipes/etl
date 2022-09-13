import React from "react";
import ReactDOM from "react-dom/client";
import {HashRouter, Routes, Route} from "react-router-dom";
//
import AppLayout from "./app-layout/app-layout";
import Home from "./home-view/home-view";
import Debug from "./debug-view/debug-view";
import Missing from "./missing-view/missing-view";

const root = ReactDOM.createRoot(document.getElementById("lp-etl"));
root.render(
  <HashRouter>
    <Routes>
      <Route path="/" element={<AppLayout/>}>
        <Route index element={<Home/>}/>
        <Route path={"/debug"} element={<Debug/>}/>
        <Route path={"*"} element={<Missing/>}/>
      </Route>
    </Routes>
  </HashRouter>
);

// https://demo.etl.linkedpipes.com/debug#//?path=000

// http://localhost:8080/react#/debug?execution=1662122919405-1924-8f4bebf6-6b70-4721-951d-3e37150bd196?path=000