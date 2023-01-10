import { uuidv4 } from "https://jslib.k6.io/k6-utils/1.4.0/index.js";
import {SchemaRegistry, SCHEMA_TYPE_JSON, SCHEMA_TYPE_STRING, Writer} from "k6/x/kafka";

const writer = new Writer({
    brokers: ["localhost:9092"],
    topic: "dtrack.vuln-analysis.component",
    autoCreateTopic: false,
});

const schemaRegistry = new SchemaRegistry();

const extractComponents = (file) => {
    const bom = JSON.parse(open(file));
    return bom.components
        .filter(component => component.purl !== null)
        .map(component => {
            return {
                uuid: uuidv4(),
                purl: component.purl
            }
        });
}

// Globbing is not supported by xk6, so we need to read all BOM file paths
// from an index file instead.
const components = JSON.parse(open("fixtures/generated/index.json")).boms
    .flatMap(bomFilePath => extractComponents(bomFilePath));

export default function () {
    for (let i = 0; i < components.length; i++) {
        writer.produce({
            messages: [
                {
                    key: schemaRegistry.serialize({
                        data: components[i].uuid,
                        schemaType: SCHEMA_TYPE_STRING
                    }),
                    value: schemaRegistry.serialize({
                        data: components[i],
                        schemaType: SCHEMA_TYPE_JSON
                    })
                }
            ]
        });
    }
}

export function teardown() {
    writer.close();
}