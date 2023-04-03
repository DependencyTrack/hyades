# Topics

| Name                                                                              | Partitions | Config                   |
|:----------------------------------------------------------------------------------|:-----------|:-------------------------|
| `dtrack-apiserver-processed-vuln-scan-result-by-scan-token-repartition`           | 3          |                          |
| `dtrack-apiserver-vuln-scan-result-by-component-uuid-repartition`                 | 3          |                          |
| `dtrack.notification.analyzer`                                                    | 3          |                          |
| `dtrack.notification.bom-consumed`                                                | 3          |                          |
| `dtrack.notification.bom-processed`                                               | 3          |                          |
| `dtrack.notification.configuration`                                               | 3          |                          |
| `dtrack.notification.datasource-mirroring`                                        | 3          |                          |
| `dtrack.notification.file-system`                                                 | 3          |                          |
| `dtrack.notification.indexing-service`                                            | 3          |                          |
| `dtrack.notification.integration`                                                 | 3          |                          |
| `dtrack.notification.new-vulnerability`                                           | 3          |                          |
| `dtrack.notification.new-vulnerable-dependency`                                   | 3          |                          |
| `dtrack.notification.policy-violation`                                            | 3          |                          |
| `dtrack.notification.project-audit-change`                                        | 3          |                          |
| `dtrack.notification.project-created`                                             | 3          |                          |
| `dtrack.notification.repository`                                                  | 3          |                          |
| `dtrack.notification.vex-consumed`                                                | 3          |                          |
| `dtrack.notification.vex-processed`                                               | 3          |                          |
| `dtrack.repo-meta-analysis.component`                                             | 3          |                          |
| `dtrack.repo-meta-analysis.result`                                                | 3          |                          |
| `dtrack.vuln-analysis.component`                                                  | 3          |                          |
| `dtrack.vuln-analysis.result`                                                     | 3          |                          |
| `dtrack.vulnerability`                                                            | 3          | `cleanup.policy=compact` |
| `dtrack.vulnerability.mirror.nvd`                                                 | 1          |                          |
| `dtrack.vulnerability.mirror.osv`                                                 | 3          |                          |
| `hyades-mirror-service-nvd-last-modified-epoch-store-changelog`                   | 1          |                          |
| `hyades-repository-meta-analyzer-command-by-purl-coordinates-repartition`         | 3          |                          |
| `hyades-vulnerability-analyzer-completed-scans-table-changelog`                   | 3          |                          |
| `hyades-vulnerability-analyzer-completed-scans-table-last-update-store-changelog` | 3          |                          |
| `hyades-vulnerability-analyzer-expected-scan-results-last-update-store-changelog` | 3          |                          |
| `hyades-vulnerability-analyzer-expected-scan-results-table-changelog`             | 3          |                          |
| `hyades-vulnerability-analyzer-ossindex-batch-store-changelog`                    | 3          |                          |
| `hyades-vulnerability-analyzer-ossindex-retry-store-changelog`                    | 3          |                          |
| `hyades-vulnerability-analyzer-scan-task-internal-repartition`                    | 3          |                          |
| `hyades-vulnerability-analyzer-scan-task-ossindex-repartition`                    | 3          |                          |
| `hyades-vulnerability-analyzer-scan-task-snyk-repartition`                        | 3          |                          |
| `hyades-vulnerability-analyzer-snyk-batch-store-changelog`                        | 3          |                          |
| `hyades-vulnerability-analyzer-snyk-retry-store-changelog`                        | 3          |                          |
