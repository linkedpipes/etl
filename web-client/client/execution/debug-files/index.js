import component from "./debug-files";
import {register} from "@/app/registry";

register({
  "path": "/execution/debug-files/:execution",
  "name": "debug-files",
  "component": component
});
