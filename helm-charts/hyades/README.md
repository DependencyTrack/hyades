# hyades

![Version: 0.1.1](https://img.shields.io/badge/Version-0.1.1-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.0.0-SNAPSHOT](https://img.shields.io/badge/AppVersion-1.0.0--SNAPSHOT-informational?style=flat-square)

**Homepage:** <https://github.com/DependencyTrack/hyades>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| nscuro | <nscuro@protonmail.com> | <https://github.com/nscuro> |
| mehab | <meha.bhargava@citi.com> | <https://github.com/mehab> |
| sahibamittal | <sahiba.mittal@citi.com> | <https://github.com/sahibamittal> |
| VithikaS | <vithika.shukla@citi.com> | <https://github.com/VithikaS> |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| apiServer.annotations | object | `{}` |  |
| apiServer.args | list | `[]` |  |
| apiServer.command | list | `[]` |  |
| apiServer.enabled | bool | `false` |  |
| apiServer.extraEnv | object | `{}` |  |
| apiServer.extraEnvFrom | list | `[]` |  |
| apiServer.image.pullPolicy | string | `"Always"` |  |
| apiServer.image.repository | string | `"dependencytrack/hyades-apiserver"` |  |
| apiServer.image.tag | string | `"latest"` |  |
| apiServer.ingress.annotations | object | `{}` |  |
| apiServer.ingress.enabled | bool | `false` |  |
| apiServer.ingress.hostname | string | `"example.com"` |  |
| apiServer.probes.liveness.failureThreshold | int | `3` |  |
| apiServer.probes.liveness.initialDelaySeconds | int | `10` |  |
| apiServer.probes.liveness.periodSeconds | int | `15` |  |
| apiServer.probes.liveness.successThreshold | int | `1` |  |
| apiServer.probes.liveness.timeoutSeconds | int | `5` |  |
| apiServer.probes.readiness.failureThreshold | int | `3` |  |
| apiServer.probes.readiness.initialDelaySeconds | int | `10` |  |
| apiServer.probes.readiness.periodSeconds | int | `15` |  |
| apiServer.probes.readiness.successThreshold | int | `1` |  |
| apiServer.probes.readiness.timeoutSeconds | int | `5` |  |
| apiServer.replicaCount | int | `1` |  |
| apiServer.resources.limits.cpu | string | `"4"` |  |
| apiServer.resources.limits.memory | string | `"8Gi"` |  |
| apiServer.resources.requests.cpu | string | `"2"` |  |
| apiServer.resources.requests.memory | string | `"4Gi"` |  |
| apiServer.service.nodePort | string | `nil` |  |
| apiServer.service.type | string | `"ClusterIP"` |  |
| apiServer.serviceMonitor.enabled | bool | `false` |  |
| apiServer.serviceMonitor.namespace | string | `"monitoring"` |  |
| apiServer.serviceMonitor.scrapeInternal | string | `"15s"` |  |
| apiServer.serviceMonitor.scrapeTimeout | string | `"30s"` |  |
| apiServer.terminationGracePeriodSeconds | int | `60` | Grace period for pod termination in seconds. Should always be equal to or greater than the sum of `_DRAIN_TIMEOUT` configurations to ensure graceful shutdown. Refer to https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/ for details. |
| common.database.jdbcUrl | string | `""` |  |
| common.database.password | string | `""` |  |
| common.database.username | string | `""` |  |
| common.fullnameOverride | string | `""` |  |
| common.image.pullSecrets | list | `[]` |  |
| common.image.registry | string | `"ghcr.io"` |  |
| common.kafka.bootstrapServers | string | `""` |  |
| common.kafka.topicPrefix | string | `""` |  |
| common.nameOverride | string | `""` |  |
| common.secretKey.createSecret | bool | `false` | Whether the chart should generate a secret key upon deployment. |
| common.secretKey.existingSecretName | string | `""` | Use the secret key defined in an existing secret. |
| frontend.annotations | object | `{}` |  |
| frontend.apiBaseUrl | string | `""` |  |
| frontend.args | list | `[]` |  |
| frontend.command | list | `[]` |  |
| frontend.enabled | bool | `false` |  |
| frontend.extraEnv | object | `{}` |  |
| frontend.extraEnvFrom | list | `[]` |  |
| frontend.image.pullPolicy | string | `"Always"` |  |
| frontend.image.repository | string | `"dependencytrack/hyades-frontend"` |  |
| frontend.image.tag | string | `"4.9.1"` |  |
| frontend.ingress.annotations | object | `{}` |  |
| frontend.ingress.enabled | bool | `false` |  |
| frontend.ingress.hostname | string | `"example.com"` |  |
| frontend.probes.liveness.failureThreshold | int | `3` |  |
| frontend.probes.liveness.initialDelaySeconds | int | `5` |  |
| frontend.probes.liveness.periodSeconds | int | `15` |  |
| frontend.probes.liveness.successThreshold | int | `1` |  |
| frontend.probes.liveness.timeoutSeconds | int | `5` |  |
| frontend.probes.readiness.failureThreshold | int | `3` |  |
| frontend.probes.readiness.initialDelaySeconds | int | `5` |  |
| frontend.probes.readiness.periodSeconds | int | `15` |  |
| frontend.probes.readiness.successThreshold | int | `1` |  |
| frontend.probes.readiness.timeoutSeconds | int | `5` |  |
| frontend.replicaCount | int | `1` |  |
| frontend.resources.limits.cpu | string | `"500m"` |  |
| frontend.resources.limits.memory | string | `"128Mi"` |  |
| frontend.resources.requests.cpu | string | `"150m"` |  |
| frontend.resources.requests.memory | string | `"64Mi"` |  |
| frontend.service.nodePort | string | `nil` |  |
| frontend.service.type | string | `"ClusterIP"` |  |
| mirrorService.annotations | object | `{}` |  |
| mirrorService.args | list | `[]` |  |
| mirrorService.command | list | `[]` |  |
| mirrorService.enabled | bool | `true` |  |
| mirrorService.extraEnv | object | `{}` |  |
| mirrorService.extraEnvFrom | list | `[]` |  |
| mirrorService.image.pullPolicy | string | `"Always"` |  |
| mirrorService.image.repository | string | `"dependencytrack/hyades-mirror-service"` |  |
| mirrorService.image.tag | string | `"latest-native"` |  |
| mirrorService.probes.liveness.failureThreshold | int | `3` |  |
| mirrorService.probes.liveness.initialDelaySeconds | int | `10` |  |
| mirrorService.probes.liveness.periodSeconds | int | `15` |  |
| mirrorService.probes.liveness.successThreshold | int | `1` |  |
| mirrorService.probes.liveness.timeoutSeconds | int | `5` |  |
| mirrorService.probes.readiness.failureThreshold | int | `3` |  |
| mirrorService.probes.readiness.initialDelaySeconds | int | `10` |  |
| mirrorService.probes.readiness.periodSeconds | int | `15` |  |
| mirrorService.probes.readiness.successThreshold | int | `1` |  |
| mirrorService.probes.readiness.timeoutSeconds | int | `5` |  |
| mirrorService.replicaCount | int | `1` | Number of replicas. Should be <= 1. |
| mirrorService.resources.limits.cpu | string | `"2"` |  |
| mirrorService.resources.limits.memory | string | `"2Gi"` |  |
| mirrorService.resources.requests.cpu | string | `"500m"` |  |
| mirrorService.resources.requests.memory | string | `"512Mi"` |  |
| notificationPublisher.annotations | object | `{}` |  |
| notificationPublisher.args | list | `[]` |  |
| notificationPublisher.command | list | `[]` |  |
| notificationPublisher.enabled | bool | `true` |  |
| notificationPublisher.extraEnv | object | `{}` |  |
| notificationPublisher.extraEnvFrom | list | `[]` |  |
| notificationPublisher.image.pullPolicy | string | `"Always"` |  |
| notificationPublisher.image.repository | string | `"dependencytrack/hyades-notification-publisher"` |  |
| notificationPublisher.image.tag | string | `"latest-native"` |  |
| notificationPublisher.probes.liveness.failureThreshold | int | `3` |  |
| notificationPublisher.probes.liveness.initialDelaySeconds | int | `10` |  |
| notificationPublisher.probes.liveness.periodSeconds | int | `15` |  |
| notificationPublisher.probes.liveness.successThreshold | int | `1` |  |
| notificationPublisher.probes.liveness.timeoutSeconds | int | `5` |  |
| notificationPublisher.probes.readiness.failureThreshold | int | `3` |  |
| notificationPublisher.probes.readiness.initialDelaySeconds | int | `10` |  |
| notificationPublisher.probes.readiness.periodSeconds | int | `15` |  |
| notificationPublisher.probes.readiness.successThreshold | int | `1` |  |
| notificationPublisher.probes.readiness.timeoutSeconds | int | `5` |  |
| notificationPublisher.replicaCount | int | `1` |  |
| notificationPublisher.resources.limits.cpu | string | `"2"` |  |
| notificationPublisher.resources.limits.memory | string | `"2Gi"` |  |
| notificationPublisher.resources.requests.cpu | string | `"500m"` |  |
| notificationPublisher.resources.requests.memory | string | `"512Mi"` |  |
| repoMetaAnalyzer.annotations | object | `{}` |  |
| repoMetaAnalyzer.args | list | `[]` |  |
| repoMetaAnalyzer.command | list | `[]` |  |
| repoMetaAnalyzer.extraEnv | object | `{}` |  |
| repoMetaAnalyzer.extraEnvFrom | list | `[]` |  |
| repoMetaAnalyzer.image.pullPolicy | string | `"Always"` |  |
| repoMetaAnalyzer.image.repository | string | `"dependencytrack/hyades-repository-meta-analyzer"` |  |
| repoMetaAnalyzer.image.tag | string | `"latest-native"` |  |
| repoMetaAnalyzer.probes.liveness.failureThreshold | int | `3` |  |
| repoMetaAnalyzer.probes.liveness.initialDelaySeconds | int | `10` |  |
| repoMetaAnalyzer.probes.liveness.periodSeconds | int | `15` |  |
| repoMetaAnalyzer.probes.liveness.successThreshold | int | `1` |  |
| repoMetaAnalyzer.probes.liveness.timeoutSeconds | int | `5` |  |
| repoMetaAnalyzer.probes.readiness.failureThreshold | int | `3` |  |
| repoMetaAnalyzer.probes.readiness.initialDelaySeconds | int | `10` |  |
| repoMetaAnalyzer.probes.readiness.periodSeconds | int | `15` |  |
| repoMetaAnalyzer.probes.readiness.successThreshold | int | `1` |  |
| repoMetaAnalyzer.probes.readiness.timeoutSeconds | int | `5` |  |
| repoMetaAnalyzer.replicaCount | int | `1` |  |
| repoMetaAnalyzer.resources.limits.cpu | string | `"2"` |  |
| repoMetaAnalyzer.resources.limits.memory | string | `"2Gi"` |  |
| repoMetaAnalyzer.resources.requests.cpu | string | `"500m"` |  |
| repoMetaAnalyzer.resources.requests.memory | string | `"512Mi"` |  |
| vulnAnalyzer.annotations | object | `{}` |  |
| vulnAnalyzer.args | list | `[]` |  |
| vulnAnalyzer.command | list | `[]` |  |
| vulnAnalyzer.extraEnv | object | `{}` |  |
| vulnAnalyzer.extraEnvFrom | list | `[]` |  |
| vulnAnalyzer.image.pullPolicy | string | `"Always"` |  |
| vulnAnalyzer.image.repository | string | `"dependencytrack/hyades-vulnerability-analyzer"` |  |
| vulnAnalyzer.image.tag | string | `"latest-native"` |  |
| vulnAnalyzer.persistentVolume.className | string | `""` |  |
| vulnAnalyzer.persistentVolume.enabled | bool | `false` |  |
| vulnAnalyzer.persistentVolume.size | string | `"2Gi"` |  |
| vulnAnalyzer.probes.liveness.failureThreshold | int | `3` |  |
| vulnAnalyzer.probes.liveness.initialDelaySeconds | int | `10` |  |
| vulnAnalyzer.probes.liveness.periodSeconds | int | `15` |  |
| vulnAnalyzer.probes.liveness.successThreshold | int | `1` |  |
| vulnAnalyzer.probes.liveness.timeoutSeconds | int | `5` |  |
| vulnAnalyzer.probes.readiness.failureThreshold | int | `3` |  |
| vulnAnalyzer.probes.readiness.initialDelaySeconds | int | `10` |  |
| vulnAnalyzer.probes.readiness.periodSeconds | int | `15` |  |
| vulnAnalyzer.probes.readiness.successThreshold | int | `1` |  |
| vulnAnalyzer.probes.readiness.timeoutSeconds | int | `5` |  |
| vulnAnalyzer.replicaCount | int | `1` |  |
| vulnAnalyzer.resources.limits.cpu | string | `"2"` |  |
| vulnAnalyzer.resources.limits.memory | string | `"2Gi"` |  |
| vulnAnalyzer.resources.requests.cpu | string | `"500m"` |  |
| vulnAnalyzer.resources.requests.memory | string | `"512Mi"` |  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.13.1](https://github.com/norwoodj/helm-docs/releases/v1.13.1)
