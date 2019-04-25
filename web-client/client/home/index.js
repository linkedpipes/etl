import component from "./home";
import {register} from "@/app/registry";

register({
  "path": "/",
  "name": "home",
  "component": component
});
