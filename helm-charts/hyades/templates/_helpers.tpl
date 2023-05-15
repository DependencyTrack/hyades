{{/*
Expand the name of the chart.
*/}}
{{- define "hyades.name" -}}
{{- default .Chart.Name .Values.common.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "hyades.fullname" -}}
{{- if .Values.common.fullnameOverride -}}
{{- .Values.common.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.common.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "hyades.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "hyades.commonLabels" -}}
helm.sh/chart: {{ include "hyades.chart" . }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Common selector labels
*/}}
{{- define "hyades.commonSelectorLabels" -}}
app.kubernetes.io/name: {{ include "hyades.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}


{{- define "hyades.mirrorServiceLabels" -}}
{{ include "hyades.commonLabels" . }}
{{ include "hyades.mirrorServiceSelectorLabels" . }}
{{- end -}}

{{/*
Mirror service selector labels
*/}}
{{- define "hyades.mirrorServiceSelectorLabels" -}}
{{ include "hyades.commonSelectorLabels" . }}
app.kubernetes.io/component: mirror-service
{{- end -}}

{{- define "hyades.mirrorServiceName" -}}
{{ .Release.Name }}-mirror-service
{{- end -}}

{{- define "hyades.mirrorServiceImage" -}}
{{ .Values.common.image.registry }}/{{ .Values.mirrorService.image.repository }}:{{ .Values.mirrorService.image.tag }}
{{- end -}}


{{- define "hyades.notificationPublisherLabels" -}}
{{ include "hyades.commonLabels" . }}
{{ include "hyades.notificationPublisherSelectorLabels" . }}
{{- end -}}

{{/*
Notification publisher selector labels
*/}}
{{- define "hyades.notificationPublisherSelectorLabels" -}}
{{ include "hyades.commonSelectorLabels" . }}
app.kubernetes.io/component: notification-publisher
{{- end -}}

{{- define "hyades.notificationPublisherName" -}}
{{ .Release.Name }}-notification-publisher
{{- end -}}

{{- define "hyades.notificationPublisherImage" -}}
{{ .Values.common.image.registry }}/{{ .Values.notificationPublisher.image.repository }}:{{ .Values.notificationPublisher.image.tag }}
{{- end -}}


{{- define "hyades.repoMetaAnalyzerLabels" -}}
{{ include "hyades.commonLabels" . }}
{{ include "hyades.repoMetaAnalyzerSelectorLabels" . }}
{{- end -}}

{{/*
Repository metadata analyzer selector labels
*/}}
{{- define "hyades.repoMetaAnalyzerSelectorLabels" -}}
{{ include "hyades.commonSelectorLabels" . }}
app.kubernetes.io/component: repository-meta-analyzer
{{- end -}}

{{- define "hyades.repoMetaAnalyzerName" -}}
{{ .Release.Name }}-repository-meta-analyzer
{{- end -}}

{{- define "hyades.repoMetaAnalyzerImage" -}}
{{ .Values.common.image.registry }}/{{ .Values.repoMetaAnalyzer.image.repository }}:{{ .Values.repoMetaAnalyzer.image.tag }}
{{- end -}}


{{- define "hyades.vulnAnalyzerLabels" -}}
{{ include "hyades.commonLabels" . }}
{{ include "hyades.vulnAnalyzerSelectorLabels" . }}
{{- end -}}

{{/*
Vulnerability analyzer selector labels
*/}}
{{- define "hyades.vulnAnalyzerSelectorLabels" -}}
{{ include "hyades.commonSelectorLabels" . }}
app.kubernetes.io/component: vulnerability-analyzer
{{- end -}}

{{- define "hyades.vulnAnalyzerName" -}}
{{ .Release.Name }}-vulnerability-analyzer
{{- end -}}

{{- define "hyades.vulnAnalyzerImage" -}}
{{ .Values.common.image.registry }}/{{ .Values.vulnAnalyzer.image.repository }}:{{ .Values.vulnAnalyzer.image.tag }}
{{- end -}}