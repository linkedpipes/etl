export type JsonLdLiteral = { [language: string]: string };

export type JsonLdEntity = { [key: string]: any };

export type JsonLdValue = string | number | boolean;

export type JsonLdGraph = {
  "iri": string,
  "entities": JsonLdEntity[],
}

export type JsonLdDocument = JsonLdGraph[];

export function getId(entity: JsonLdEntity): string {
  return entity["@id"];
}

export function getTypes(entity: JsonLdEntity): string[] {
  const types = entity["@type"];
  if (Array.isArray(types)) {
    return types;
  } else {
    return [types];
  }
}

export function getResource(
  entity: JsonLdEntity, predicate: string
): string | undefined {
  return getFirst(getResources(entity, predicate));
}

function getFirst<T>(values: T[]): T | undefined {
  if (values.length === 0) {
    return undefined;
  } else {
    return values[0];
  }
}

export function getResources(
  entity: JsonLdEntity, predicate: string
): string[] {
  return asArray(entity[predicate]).map(getId);
}

function asArray<T>(value: T | T[] | undefined | null): T[] {
  if (value === undefined || value === null) {
    return [];
  } else if (Array.isArray(value)) {
    return value;
  } else {
    return [value];
  }
}

export function getString(entity: JsonLdEntity, predicate: string): JsonLdLiteral | undefined {
  const strings = getStrings(entity, predicate);
  if (strings.length === 0) {
    return undefined;
  }
  let result = {}
  strings.forEach((string) => {
    result = {...result, ...string};
  });
  return result;
}

export function getStrings(entity: JsonLdEntity, predicate: string): JsonLdLiteral[] {
  return asArray(entity[predicate]).map(valueToLiteral)
}

function valueToLiteral(value: any): JsonLdLiteral {
  if (value["@value"] === undefined) {
    return {"": value};
  } else {
    if (value["@language"]) {
      return {[value["@language"]]: value["@value"]};
    } else {
      return {"": value["@value"]};
    }
  }
}

export function getValue(
  entity: JsonLdEntity, predicate: string
): JsonLdValue | undefined {
  return getFirst(getValues(entity, predicate));
}

export function getValues(entity: JsonLdEntity, predicate: string): JsonLdValue[] {
  return asArray(entity[predicate]).map(valueToPlainString);
}

function valueToPlainString(value: any): string {
  if (value["@value"] === undefined) {
    return value;
  } else {
    return value["@value"];
  }
}

export function getEntityById(
  entities: JsonLdEntity[], iri: string | undefined
): JsonLdEntity | undefined {
  if (iri === undefined) {
    return undefined;
  }
  for (let entity of entities) {
    if (getId(entity) === iri) {
      return entity;
    }
  }
  return undefined;
}

export function getEntityByType(
  entities: JsonLdEntity[], type: string
): JsonLdEntity | undefined {
  for (let entity of entities) {
    if (!getTypes(entity).includes(type)) {
      continue;
    }
    return entity;
  }
  return undefined;
}

export function getEntitiesByType(
  entities: JsonLdEntity[], type: string
): JsonLdEntity[] {
  const result: JsonLdEntity[] = [];
  for (let entity of entities) {
    if (!getTypes(entity).includes(type)) {
      continue;
    }
    result.push(entity);
  }
  return result;
}

/**
 * Given JSON document convert it into a shape of JSONLD document.
 */
export function jsonToJsonLdEntities(content: unknown): JsonLdDocument {
  const result: JsonLdGraph[] = [];
  if (Array.isArray(content)) {
    for (const item of content) {
      if (typeof item === "object") {
        if (item["@graph"]) {
          // [{"@id":... , "@graph:" [...]}, ...]
          result.push({
            "iri": item["@id"] ?? "",
            "entities": item["@graph"] as JsonLdEntity[]
          });
        } else {
          throw Error("Invalid JSONLD: + " + JSON.stringify(content));
        }
      } else {
        throw Error("Invalid JSONLD: + " + JSON.stringify(content));
      }
    }
  } else {
    throw Error("Invalid JSONLD: + " + JSON.stringify(content));
  }
  return result;
}
