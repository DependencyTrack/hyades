# Topics

| Name                                                                                            | Partitions | Config                                                                              |
|:------------------------------------------------------------------------------------------------|:-----------|:------------------------------------------------------------------------------------|
| `dtrack.repo-meta-analysis.component`<sup>1</sup>                                               | 3          |                                                                                     |
| `dtrack.repo-meta-analysis.result`                                                              | 3          |                                                                                     |
| `hyades-repository-meta-analyzer-command-by-purl-coordinates-repartition`<sup>1</sup>           | 3          |                                                                                     |

*<sup>1</sup> The topic is subject to [co-partitioning requirements](#co-partitioning-requirements)*

## Co-Partitioning Requirements

Some topics must be [co-partitioned](https://www.confluent.io/blog/co-partitioning-in-kafka-streams/),
meaning they must share the *exact same number of partitions*. Applications using those topics will not work
correctly when this is not the case.
