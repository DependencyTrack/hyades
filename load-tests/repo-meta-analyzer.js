import {uuidv4} from "https://jslib.k6.io/k6-utils/1.4.0/index.js";
import {BALANCER_MURMUR2, SchemaRegistry, SCHEMA_TYPE_JSON, SCHEMA_TYPE_STRING, Writer} from "k6/x/kafka";

const writer = new Writer({
    brokers: ["localhost:9092"],
    topic: "dtrack.repo-meta-analysis.component",
    balancer: BALANCER_MURMUR2,
    autoCreateTopic: false,
});

const schemaRegistry = new SchemaRegistry();

const bomFile = open("fixtures/boms/bloated.bom.json");
const bom = JSON.parse(bomFile);

export default function () {
    for (let i = 0; i < bom.components.length; i++) {
        const key = uuidv4();
        writer.produce({
            messages: [
                {
                    key: schemaRegistry.serialize({
                        data: key,
                        schemaType: SCHEMA_TYPE_STRING
                    }),
                    value: schemaRegistry.serialize({
                        data: {
                            "uuid": key,
                            "purl": bom.components[i].purl
                        },
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