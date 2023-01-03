#!/usr/bin/env bash

set -euxo pipefail

function create_topic() {
  if ! output=$(rpk topic create "$1" --partitions "$2" --topic-config "retention.ms=$3"); then
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
  create_topic "$topic_name" "$NOTIFICATION_TOPICS_PARTITIONS" "$NOTIFICATION_TOPICS_RETENTION_MS"
done

repo_meta_analysis_topics=(
  "dtrack.repo-meta-analysis.component"
  "dtrack.repo-meta-analysis.result"
)
for topic_name in "${repo_meta_analysis_topics[@]}"; do
  create_topic "$topic_name" "$REPO_META_ANALYSIS_TOPICS_PARTITIONS" "$REPO_META_ANALYSIS_TOPICS_RETENTION_MS"
done

vuln_analysis_topics=(
  "dtrack.vuln-analysis.component"
  "dtrack.vuln-analysis.component.cpe"
  "dtrack.vuln-analysis.component.purl"
  "dtrack.vuln-analysis.component.swid"
  "dtrack.vuln-analysis.vulnerability"
  "dtrack.vuln-analysis.result"
  "dtrack.mirror.osv"
  "dtrack.vulnerability"
)
for topic_name in "${vuln_analysis_topics[@]}"; do
  create_topic "$topic_name" "$VULN_ANALYSIS_TOPICS_PARTITIONS" "$VULN_ANALYSIS_TOPICS_RETENTION_MS"
done