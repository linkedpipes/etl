#!/usr/bin/env python3
import typing
import argparse
import os

try:
    import rdflib
    from rdflib import RDF
except:
    print("This script required rdflib to be installed.")
    print("See https://github.com/RDFLib/rdflib#installation .")
    exit(1)


def _read_arguments() -> typing.Dict[str, str]:
    parser = argparse.ArgumentParser(
        description="Can be used to change domain of LinkedPipes ETL resources."
                    "By doing so this script can be used to import/export data"
                    "using file instance between instances. Please backup"
                    "your file if you want to perform in-place update as "
                    "once the script is started there is no way back."
    )
    parser.add_argument(
        "--input",
        required=True,
        help="Path to input storage data directory.")
    parser.add_argument(
        "--domain",
        required=True,
        help="Name of the new domain. Must not end with '/'.")
    parser.add_argument(
        "--output",
        help="Path to output storage data directory. It may not exists, "
             "if not set an inplace modification is used.")
    result = vars(parser.parse_args())
    print(result)
    if result["output"] is None:
        result["output"] = result["input"]
    return result


def main(arguments):
    _update_templates(
        os.path.join(arguments["input"], "templates"),
        os.path.join(arguments["output"], "templates"),
        arguments["domain"])
    _update_pipelines(
        os.path.join(arguments["input"], "pipelines"),
        os.path.join(arguments["output"], "pipelines"),
        arguments["domain"])
    _update_knowledge(
        os.path.join(arguments["input"], "knowledge"),
        os.path.join(arguments["output"], "knowledge"),
        arguments["domain"])
    print("All done")


def _update_templates(
        source_dir: str, target_dir: str, target_domain: str) -> None:
    if not os.path.exists(source_dir):
        print("Templates directory does not exist.")
        return

    templates = [
        name
        for name in os.listdir(source_dir)
        if not name.startswith("jar-") and
           os.path.isdir(os.path.join(source_dir, name))
    ]
    for name in templates:
        print(f"Updating template: '{name}' ...")
        template_dir = os.path.join(source_dir, name)
        source_domain = _detect_domain_from_types(
            os.path.join(source_dir, name, "definition.trig"),
            "http://linkedpipes.com/ontology/Template")
        for file in os.listdir(template_dir):
            _update_file(
                os.path.join(source_dir, name, file),
                os.path.join(target_dir, name, file),
                source_domain, target_domain)


def _detect_domain_from_types(source_file: str, type: str) -> str:
    statements = _read_trig_file(source_file)
    pattern = (None, RDF.type, rdflib.URIRef(type), None)
    for s, _, _, _ in statements.quads(pattern):
        domain = _select_domain(str(s))
        if domain is None:
            continue
        return domain
    print(f"Can not detect domain for '{source_file}'!")
    raise RuntimeError()


def _select_domain(url: str):
    index = url.index("/", url.index("//") + 2)
    if index == -1:
        return None
    return url[:index]


def _read_trig_file(path: str) -> rdflib.Dataset:
    result = rdflib.Dataset()
    with open(path, "r", encoding="utf-8") as stream:
        result.parse(file=stream, format="trig")
    return result


def _update_file(
        source_file: str, target_file: str,
        source_domain: str, target_domain: str) -> None:
    os.makedirs(os.path.dirname(target_file), exist_ok=True)

    def update_domain(item):
        return _update_resource(item, source_domain, target_domain)

    source = _read_trig_file(source_file)

    target = rdflib.Dataset()
    for s, p, o, g in source.quads((None, None, None, None)):
        target.add((
            update_domain(s), p, update_domain(o), update_domain(g)))

    with open(target_file, "wb") as stream:
        target.serialize(stream, encoding="utf-8", format="trig")


def _update_resource(value, source_domain: str, target_domain: str):
    if isinstance(value, rdflib.URIRef) and \
            str(value).startswith(source_domain):
        suffix = str(value)[len(source_domain):]
        return rdflib.URIRef(target_domain + suffix)
    return value


def _update_pipelines(
        source_dir: str, target_dir: str, target_domain: str) -> None:
    if not os.path.exists(source_dir):
        print("Pipeline directory does not exist.")
        return

    pipelines = [
        name
        for name in os.listdir(source_dir)
        if name.endswith(".trig") and
           os.path.isfile(os.path.join(source_dir, name))
    ]
    for name in pipelines:
        print(f"Updating pipeline: '{name}' ...")
        source_domain = _detect_domain_from_types(
            os.path.join(source_dir, name),
            "http://linkedpipes.com/ontology/Pipeline")
        _update_file(
            os.path.join(source_dir, name),
            os.path.join(target_dir, name),
            source_domain, target_domain)


def _update_knowledge(
        source_dir: str, target_dir: str, target_domain: str) -> None:
    mapping_file = os.path.join(source_dir, "mapping.trig")
    if not os.path.exists(mapping_file):
        return
    os.makedirs(target_dir, exist_ok=True)

    source = _read_trig_file(mapping_file)
    print("Updating mapping ...")
    target = rdflib.Dataset()
    for s, p, o, g in source.quads((None, None, None, None)):
        source_domain = _select_domain(str(o))
        if source_domain is None:
            print(f"Can not detect domain for '{o}' in mapping!")
            raise RuntimeError()
        target.add((s, p, _update_resource(o, source_domain, target_domain), g))

    with open(os.path.join(target_dir, "mapping.trig"), "wb") as stream:
        target.serialize(stream, encoding="utf-8", format="trig")


if __name__ == "__main__":
    main(_read_arguments())
