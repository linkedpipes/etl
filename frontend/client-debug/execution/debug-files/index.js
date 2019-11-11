import component from "./debug-files";
import {register} from "@client-debug/app/registry";

register({
  "path": "/:execution",
  "name": "debug-files",
  "component": component
});
