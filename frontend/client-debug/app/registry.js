const registry = [];

export function register(component) {
  registry.push(component);
}

export function getRegistered() {
  return registry;
}
