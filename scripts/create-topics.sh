#!/usr/bin/env bash

set -euxo pipefail

function create_topic() {
  if ! output=$(rpk topic create "$1" --partitions "$2" --topic-config "$3"); then
    # Don't fail the script when the rpk command failed because the topic already exists.
    if [[ "$output" != *"TOPIC_ALREADY_EXISTS"* ]]; then
      exit 2
    fi
  fi
}

# Wait for Redpanda to become available
rpk cluster health --watch --exit-when-healthy \
  --api-urls "$(echo "$REDPANDA_BROKERS" | sed -E 's/:[[:digit:]]+/:9644/g')"

notification_topics=(
  "dtrack.notification.analyzer"
  "dtrack.notification.bom-consumed"
  "dtrack.notification.bom-processed"
  "dtrack.notification.configuration"
  "dtrack.notification.datasource-mirroring"
  "dtrack.notification.file-system"
  "dtrack.notification.indexing-service"
  "dtrack.notification.integration"
  "dtrack.notification.new-vulnerability"
  "dtrack.notification.new-vulnerable-dependency"
  "dtrack.notification.policy-violation"
  "dtrack.notification.project-audit-change"
  "dtrack.notification.repository"
  "dtrack.notification.vex-consumed"
  "dtrack.notification.vex-processed"
)
for topic_name in "${notification_topics[@]}"; do
  create_topic "$topic_name" "${NOTIFICATION_TOPICS_PARTITIONS:-3}" "retention.ms=${NOTIFICATION_TOPICS_RETENTION_MS:-43200000}"
done

repo_meta_analysis_topics=(
  "dtrack.repo-meta-analysis.component"
  "dtrack.repo-meta-analysis.result"
)
for topic_name in "${repo_meta_analysis_topics[@]}"; do
  create_topic "$topic_name" "${REPO_META_ANALYSIS_TOPICS_PARTITIONS:-3}" "retention.ms=${REPO_META_ANALYSIS_TOPICS_RETENTION_MS:-43200000}"
done

vuln_analysis_topics=(
  "dtrack.vuln-analysis.component"
  "dtrack.vuln-analysis.result"
)
for topic_name in "${vuln_analysis_topics[@]}"; do
  create_topic "$topic_name" "${VULN_ANALYSIS_TOPICS_PARTITIONS:-3}" "retention.ms=${VULN_ANALYSIS_TOPICS_RETENTION_MS:-43200000}"
done

create_topic "dtrack.vulnerability.mirror.osv" "${VULN_MIRROR_TOPICS_PARTITIONS:-3}" "retention.ms=${VULN_MIRROR_TOPICS_RETENTION_MS:-43200000}"
create_topic "dtrack.vulnerability" "${VULN_MIRROR_TOPICS_PARTITIONS:-3}" "cleanup.policy=compact"