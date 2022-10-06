# DT-Kafka-POC
Implement the design mentioned here: https://excalidraw.com/#room=fba0103fa2642574be40,NomXwyHw3jvoy0yr6JxCJw

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
},{
"id": 5,
"propertyName": "ossindex.api.username",
"propertyValue": "org id"
},
{
"id": 6,
"propertyName": "ossindex.api.token",
"propertyValue": "org id"
},
{
"id": 7,
"propertyName": "analysis.cache.validity.period",
"propertyValue": "org id"
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

## Redpanda Console

The provided `docker-compose.yml` includes an instance of [Redpanda Console](https://github.com/redpanda-data/console) 
to aid with gaining insight into what's happening in the message broker. Among many other things, it can be used to 
inspect messages inside any given topic:

![Redpanda Console - Messages](.github/images/redpanda-console_messages.png)

The console is exposed at `http://127.0.0.1:28080`.
