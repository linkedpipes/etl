((definition) => {
  if (typeof define === "function" && define.amd) {
    define(["jointjs"], definition);
  }
})((joint) => {

  const ComponentShape = joint.shapes.basic.Generic.extend({
    "markup": "<g class=\"rotatable\"><g class=\"scalable\"><rect class=\"body\"/></g><text class=\"label\"/></g>",
    "portMarkup": "<circle class=\"port-body\"/>",
    "portLabelMarkup": "<text class=\"port-label\"/>",
    "portContainerMarkup": "<g class=\"joint-port\"/>",
    "defaults": joint.util.deepSupplement({
        "type": "linkedpipes.link.component",
        "inPorts": [],
        "outPorts": [],
        "size": {
          "width": 1,
          "height": 1
        },
        "attrs": {
          ".": {
            "magnet": false
          },
          ".body": {
            "width": 150, "height": 250,
            "stroke": "black",
            "stroke-width": 1
          },
          ".port-body": {
            "r": 10,
            "magnet": true,
            "stroke": "#000000"
          },
          "text": {
            "pointer-events": "none"
          },
          ".label": {
            "text": "Model",
            "ref-x": .5,
            "ref-y": 10,
            "ref": ".body",
            "text-anchor": "middle",
            "fill": "#000000"
          }
        },
        "ports": {
          "groups": {
            "input": {
              "position": {"name": "left"},
              "attrs": {
                ".port-label": {"fill": "#000000"},
                ".port-body": {
                  "fill": "#CCFFCC",
                  "stroke": "#000000",
                  "r": 10,
                  "magnet": "passive"
                }
              },
              "label": {
                "position": {"name": "left"}
              }
            },
            "output": {
              "position": {"name": "right"},
              "attrs": {
                ".port-label": {"fill": "#000000"},
                ".port-body": {
                  "fill": "#FFFFCC",
                  "stroke": "#000000",
                  "r": 10,
                  "magnet": true
                }
              },
              "label": {
                "position": {"name": "right"}
              }
            }
          }
        }
      },
      joint.shapes.basic.Generic.prototype.defaults)
  });

  function createFromTemplate(api, template, component) {
    if (template === undefined) {
      console.warn("Ignored component without a template: ", component);
      return;
    }
    const coreTemplate = api.getCoreTemplate(template);
    if (coreTemplate === undefined) {
      console.warn("Ignored component with missing template: ", template);
      return;
    }
    const ports = createPorts(api, coreTemplate);
    const cell = createCell(
      api.getComponentX(component),
      api.getComponentY(component),
      ports);

    updateCell(api, template, component, cell);
    return cell;
  }

  function createPorts(api, template) {
    if (!getCachedPorts(template)) {
      setCachedPorts(template, createPortsData(api, template));
    }
    return getCachedPorts(template);
  }

  function getCachedPorts(template) {
    return template["_ports"];
  }

  function setCachedPorts(template, ports) {
    template["_ports"] = ports;
  }

  function createPortsData(api, template) {
    const cells = [];
    const ports = {};

    prepareInputPorts(api, template, cells, ports);
    prepareOutputPorts(api, template, cells, ports);

    return {
      "cells": cells,
      "ports": ports
    };
  }

  function prepareInputPorts(api, template, cells, ports) {
    api.getTemplateInputs(template).forEach((port) => {
      const info = api.getInputPortInfo(template, port);
      ports[info.binding] = {
        "dataType": info.content
      };
      cells.push({
        "id": info.binding,
        "group": info.type,
        "attrs": {
          ".port-body": {
            "fill": info.color
          },
          ".port-label": {
            "text": info.useLabel ? info.label : null
          }
        }
      });
    });
  }

  function prepareOutputPorts(api, template, cells, ports) {
    const outputs = api.getTemplateOutputs(template);
    const useLabel = outputs.length > 1;
    outputs.forEach((port) => {
      const info = api.getOutputPortInfo(template, port);
      ports[info.binding] = {
        "dataType": info.content
      };
      cells.push({
        "id": info.binding,
        "group": "output",
        "attrs": {
          ".port-label": {
            "text": useLabel ? info.label : null
          }
        }
      });
    });
  }

  function createCell(x, y, ports) {
    return new ComponentShape({
      "position": {
        "x": x,
        "y": y
      },
      "portsData": ports["ports"],
      "ports": {
        "items": ports["cells"]
      }
    });
  }

  function updateCell(api, template, component, cell) {
    const label = api.getComponentLabel(component);
    const description = api.getComponentDescription(component);

    const hasDescription = (description ?? "").trim().length > 0;
    let componentLabel = label;
    if (hasDescription) {
      componentLabel += "\n" + description;
    }
    updateLabel(cell, componentLabel, !hasDescription);
    updateSize(api, cell, template, componentLabel);
    updateColor(api, cell, component, template);
    updateRect(api, cell, component);
  }

  function updateLabel(shape, extendedLabel, alignMiddle) {
    if (alignMiddle) {
      shape.attr(".label", {
        "text": extendedLabel,
        "ref-y": "50%",
        "y-alignment": "middle",
      });
    } else {
      shape.attr(".label", {
        "text": extendedLabel,
        "ref-y": "20%",
        "y-alignment": "top"
      });
    }
  }

  function updateSize(api, cell, template, label) {
    const core = api.getCoreTemplate(template);
    // Calculate size.
    const portCount = Math.max(
      api.getTemplateInputs(core).length,
      api.getTemplateOutputs(core).length,
      1);

    const labelSplit = label.split("\n");
    const height = Math.max(portCount * 25 + 10, labelSplit.length * 23);

    let maxLineLen = 0;
    labelSplit.forEach((line) => {
      let lineLen = stringWidth(line);
      if (lineLen > maxLineLen) {
        maxLineLen = lineLen;
      }
    });

    // Add 30 for ports and as a basic size.
    cell.resize(30 + maxLineLen, height);

    // TODO Emit only if component have changed.
    cell.trigger("change:size");
  }

  function stringWidth(string) {
    let width = 0;
    for (let index in string) {
      const character = string[index];
      if (character > "0" && character < "9") {
        width += 8.2;
        continue;
      }
      switch (character) {
        case " ":
          width += 3;
          break;
        case "i":
        case "j":
        case "l":
        case "t":
          width += 5;
          break;
        case "f":
        case "r":
          width += 5;
          break;
        case "s":
          width += 7;
          break;
        case "w":
          width += 12;
          break;
        case "m":
          width += 13;
          break;
        default:
          width += 8.5;
          break;
      }
      if (character !== " " && character === character.toUpperCase()) {
        width += 4;
      }
    }
    return width;
  }

  function updateColor(api, cell, component, template) {
    const color = api.getComponentFillColor(template, component);
    cell.attr("rect", {"fill": color});
  }

  function updateRect(api, cell, component) {
    const style = api.getComponentRectStyle(component);
    cell.attr("rect", style);
  }

  return {
    "create": createFromTemplate,
    "update": updateCell
  }

});