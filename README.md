# DT-Kafka-POC-II
Implement the design mentioned in phase II here: https://excalidraw.com/#room=fba0103fa2642574be40,NomXwyHw3jvoy0yr6JxCJw

## Setup
### API call to set config properties:
Post request to : http://localhost:8089/config <br/>
Body:<br/>
`[
{
"id": 1,
"propertyName": "snyk.org.id",
"propertyValue": "org id"
},{
"id": 2,
"propertyName": "snyk.api.token",
"propertyValue": "token"
},
{
"id": 3,
"propertyName": "ossindex.enabled",
"propertyValue": "true"
},
{
"id": 4,
"propertyName": "snyk.enabled",
"propertyValue": "true"
}
]`<br/> <br/>
### Verify if Cwe store contains values
Get request to http://localhost:8089/cwe/data?id=178

### Post components to test:
Post request to http://localhost:8089/event <br/>
Body: <br/>
`{
"project": {
"id": 1,
"name": "death_star"
},
"components": [
{
"id": 11,
"name": "air",
"purl": "pkg:pypi/django@1.11.1",
"group": "g1",
"author": "mehatest"
}
]
}`
<br/>
<br/>

## Run the code in dev mode <br/>
If you have quarkus cli installed: `quarkus dev -Dalpine.application.properties=./alpine.properties`<br/>
Using maven: `mvn quarkus:dev -Dalpine.application.properties=./alpine.properties`<br/>