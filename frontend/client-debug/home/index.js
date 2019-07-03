import component from "./home";
import {register} from "@client-debug/app/registry";

register({
  "path": "/",
  "name": "home",
  "component": component
});
