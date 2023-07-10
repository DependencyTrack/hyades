# Topics

| Name                                                                                              | Partitions | Config                                                                              |
|:--------------------------------------------------------------------------------------------------|:-----------|:------------------------------------------------------------------------------------|
| `dtrack-apiserver-processed-vuln-scan-result-by-scan-token-repartition`<sup>1A</sup>              | 3          |                                                                                     |
| `dtrack.notification.analyzer`                                                                    | 3          |                                                                                     |
| `dtrack.notification.bom`                                                                         | 3          |                                                                                     |
| `dtrack.notification.configuration`                                                               | 3          |                                                                                     |
| `dtrack.notification.datasource-mirroring`                                                        | 3          |                                                                                     |
| `dtrack.notification.file-system`                                                                 | 3          |                                                                                     |
| `dtrack.notification.integration`                                                                 | 3          |                                                                                     |
| `dtrack.notification.new-vulnerability`                                                           | 3          |                                                                                     |
| `dtrack.notification.new-vulnerable-dependency`                                                   | 3          |                                                                                     |
| `dtrack.notification.policy-violation`                                                            | 3          |                                                                                     |
| `dtrack.notification.project-audit-change`                                                        | 3          |                                                                                     |
| `dtrack.notification.project-created`                                                             | 3          |                                                                                     |
| `dtrack.notification.repository`                                                                  | 3          |                                                                                     |
| `dtrack.notification.vex`                                                                         | 3          |                                                                                     |
| `dtrack.notification.project-vuln-analysis-complete` <sup>3</sup>                                 | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `dtrack.repo-meta-analysis.component`<sup>1B</sup>                                                | 3          |                                                                                     |
| `dtrack.repo-meta-analysis.result`                                                                | 3          |                                                                                     |
| `dtrack.vuln-analysis.component`<sup>1C</sup>                                                     | 3          |                                                                                     |
| `dtrack.vuln-analysis.result`<sup>1A</sup>                                                        | 3          |                                                                                     |
| `dtrack.vuln-analysis.scanner.result`<sup>1C</sup>                                                | 3          |                                                                                     |
| `dtrack.vulnerability`                                                                            | 3          | `cleanup.policy=compact`                                                            |
| `dtrack.vulnerability.digest`<sup>2</sup>                                                         | 1          | `cleanup.policy=compact`                                                            |
| `dtrack.vulnerability.mirror.command`<sup>2</sup>                                                 | 1          |                                                                                     |
| `dtrack.vulnerability.mirror.state`<sup>2</sup>                                                   | 1          | `cleanup.policy=compact`                                                            |
| `hyades-repository-meta-analyzer-command-by-purl-coordinates-repartition`<sup>1B</sup>            | 3          |                                                                                     |
| `hyades-vulnerability-analyzer-completed-scans-table-changelog`<sup>1C</sup>                      | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `hyades-vulnerability-analyzer-expected-scanner-results-last-update-store-changelog`<sup>1C</sup> | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `hyades-vulnerability-analyzer-expected-scanner-results-table-changelog`<sup>1C</sup>             | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `hyades-vulnerability-analyzer-ossindex-batch-store-changelog`<sup>1D</sup>                       | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `hyades-vulnerability-analyzer-ossindex-retry-store-changelog`<sup>1D</sup>                       | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `hyades-vulnerability-analyzer-scan-task-internal-repartition`                                    | 3          |                                                                                     |
| `hyades-vulnerability-analyzer-scan-task-ossindex-repartition`<sup>1D</sup>                       | 3          |                                                                                     |
| `hyades-vulnerability-analyzer-scan-task-snyk-repartition`<sup>1E</sup>                           | 3          |                                                                                     |
| `hyades-vulnerability-analyzer-snyk-batch-store-changelog`<sup>1E</sup>                           | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |
| `hyades-vulnerability-analyzer-snyk-retry-store-changelog`<sup>1E</sup>                           | 3          | `cleanup.policy=compact`<br/>`segment.bytes=67108864`<br/>`max.compaction.lag.ms=0` |

*<sup>1X</sup> The topic is subject to [co-partitioning requirements](#co-partitioning-requirements)*  
*<sup>2</sup> The partition number of this topic should not be changed*
*<sup>3</sup> To use this notification, the PROJECT_VULN_ANALYSIS_COMPLETE group needs to be manually added through api call to the notify on condition
The put request to be executed is [here](https://github.com/DependencyTrack/hyades-apiserver/blob/main/src/main/java/org/dependencytrack/resources/v1/NotificationRuleResource.java#L100)<br/>*

## Co-Partitioning Requirements

Some topics must be [co-partitioned](https://www.confluent.io/blog/co-partitioning-in-kafka-streams/),
meaning they must share the *exact same number of partitions*. Applications using those topics will not work
correctly when this is not the case.
