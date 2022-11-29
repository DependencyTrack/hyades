test-all: test-repo-meta-analyzer test-vuln-analyzer

test-repo-meta-analyzer:
	./bin/k6 run repo-meta-analyzer.js
.PHONY: test-repo-meta-analyzer

test-vuln-analyzer:
	./bin/k6 run vuln-analyzer.js
.PHONY: test-vuln-analyzer

install-xk6:
	go install -v go.k6.io/xk6/cmd/xk6@latest
.PHONY: install-xk6

install-xk6-kafka:
	xk6 build --with github.com/mostafa/xk6-kafka@latest --output ./bin/k6
.PHONY: install-xk6-kafka
