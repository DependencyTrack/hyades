#!/usr/bin/env python3

import os.path
import zipfile
from argparse import ArgumentParser
from collections import OrderedDict
from datetime import datetime, timezone
from pathlib import Path
from tempfile import TemporaryFile
from xml.etree import ElementTree

import jinja2
import requests
from defusedxml.ElementTree import parse as parse_etree

template = """package {{ package }};

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@Generated(value = {{ script_name | tojson }}, date = {{ date | tojson }})
final class CweDefinitions {
    
    static final Map<Integer, String> DEFINITIONS = new HashMap<>();
    
    static {
        {% for id, name in definitions.items() -%}
        DEFINITIONS.put({{ id }}, {{ name | tojson }});
        {% endfor %}
    }
    
    private CweDefinitions() {
    }
    
}
"""

if __name__ == "__main__":
    arg_parser = ArgumentParser()
    arg_parser.add_argument("-p", "--package", default="org.acme.resolver", help="Package name")
    arg_parser.add_argument("-o", "--output", type=Path, required=True, help="Output file path")
    args = arg_parser.parse_args()

    with TemporaryFile(suffix=".zip") as tmp:
        with requests.get("https://cwe.mitre.org/data/xml/cwec_latest.xml.zip") as res:
            tmp.write(res.content)
        tmp.seek(0)
        with zipfile.ZipFile(tmp) as zip:
            with zip.open("cwec_v4.9.xml") as dict_file:
                tree: ElementTree = parse_etree(dict_file)

    tree_root = tree.getroot()
    namespaces = {"cwe": "http://cwe.mitre.org/cwe-6"}
    definitions: dict[int, str] = OrderedDict()


    def process_definitions(xpath: str):
        for definition in tree_root.findall(xpath, namespaces=namespaces):
            definitions[int(definition.attrib["ID"])] = definition.attrib["Name"]


    process_definitions("./cwe:Categories/cwe:Category")
    process_definitions("./cwe:Weaknesses/cwe:Weakness")
    process_definitions("./cwe:Views/cwe:View")

    with args.output.open(mode="w") as out_file:
        out_file.write(jinja2.Environment().from_string(template).render(
            package=args.package,
            script_name=os.path.basename(__file__),
            date=datetime.now(timezone.utc).isoformat(),
            definitions=definitions
        ))
