{{/*
    Common labels
*/}}
{{- define "commonLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
{{- end -}}

{{/*
    Vulnerability analyzer labels
*/}}
{{- define "vulnAnalyzerLabels" -}}
app.kubernetes.io/component: vulnerability-analyzer
{{- end -}}

{{/*
    Vulnerability analyzer selector labels
*/}}
{{- define "vulnAnalyzerSelectorLabels" -}}
{{ include "commonLabels" . }}
{{ include "vulnAnalyzerLabels" . }}
{{- end -}}

{{/*
    Vulnerability analyzer name
*/}}
{{- define "vulnAnalyzerName" -}}
{{ .Release.Name }}-vulnerability-analyzer
{{- end -}}

{{/*
    Vulnerability analyzer image
*/}}
{{- define "vulnAnalyzerImage" -}}
{{ .Values.common.image.registry }}/{{ .Values.vulnAnalyzer.image.repository }}:{{ .Values.vulnAnalyzer.image.tag }}
{{- end -}}