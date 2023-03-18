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
  "${API_TOPIC_PREFIX:-}dtrack.notification.analyzer"
  "${API_TOPIC_PREFIX:-}dtrack.notification.bom-consumed"
  "${API_TOPIC_PREFIX:-}dtrack.notification.bom-processed"
  "${API_TOPIC_PREFIX:-}dtrack.notification.configuration"
  "${API_TOPIC_PREFIX:-}dtrack.notification.datasource-mirroring"
  "${API_TOPIC_PREFIX:-}dtrack.notification.file-system"
  "${API_TOPIC_PREFIX:-}dtrack.notification.indexing-service"
  "${API_TOPIC_PREFIX:-}dtrack.notification.integration"
  "${API_TOPIC_PREFIX:-}dtrack.notification.new-vulnerability"
  "${API_TOPIC_PREFIX:-}dtrack.notification.new-vulnerable-dependency"
  "${API_TOPIC_PREFIX:-}dtrack.notification.policy-violation"
  "${API_TOPIC_PREFIX:-}dtrack.notification.project-audit-change"
  "${API_TOPIC_PREFIX:-}dtrack.notification.project-created"
  "${API_TOPIC_PREFIX:-}dtrack.notification.repository"
  "${API_TOPIC_PREFIX:-}dtrack.notification.vex-consumed"
  "${API_TOPIC_PREFIX:-}dtrack.notification.vex-processed"
)
for topic_name in "${notification_topics[@]}"; do
  create_topic "$topic_name" "${NOTIFICATION_TOPICS_PARTITIONS:-3}" "retention.ms=${NOTIFICATION_TOPICS_RETENTION_MS:-43200000}"
done

repo_meta_analysis_topics=(
  "${API_TOPIC_PREFIX:-}dtrack.repo-meta-analysis.component"
  "${API_TOPIC_PREFIX:-}dtrack.repo-meta-analysis.result"
)
for topic_name in "${repo_meta_analysis_topics[@]}"; do
  create_topic "$topic_name" "${REPO_META_ANALYSIS_TOPICS_PARTITIONS:-3}" "retention.ms=${REPO_META_ANALYSIS_TOPICS_RETENTION_MS:-43200000}"
done

vuln_analysis_topics=(
  "${API_TOPIC_PREFIX:-}dtrack.vuln-analysis.component"
  "${API_TOPIC_PREFIX:-}dtrack.vuln-analysis.result"
)
for topic_name in "${vuln_analysis_topics[@]}"; do
  create_topic "$topic_name" "${VULN_ANALYSIS_TOPICS_PARTITIONS:-3}" "retention.ms=${VULN_ANALYSIS_TOPICS_RETENTION_MS:-43200000}"
done

create_topic "${API_TOPIC_PREFIX:-}dtrack.vulnerability.mirror.osv" "${VULN_MIRROR_TOPICS_PARTITIONS:-3}" "retention.ms=${VULN_MIRROR_TOPICS_RETENTION_MS:-43200000}"
create_topic "${API_TOPIC_PREFIX:-}dtrack.vulnerability.mirror.nvd" "${VULN_MIRROR_TOPICS_PARTITIONS:-1}" "retention.ms=${VULN_MIRROR_TOPICS_RETENTION_MS:-43200000}"
create_topic "${API_TOPIC_PREFIX:-}dtrack.vulnerability" "${VULN_MIRROR_TOPICS_PARTITIONS:-3}" "cleanup.policy=compact"

echo "All topics created successfully"