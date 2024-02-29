bundle:
	rm -f ./bundle.zip
	zip ./bundle.zip ./policies/*.yaml
	chmod 644 ./bundle.zip
.PHONY: bundle

empty-bundle:
	rm -f ./bundle.zip
	zip ./bundle.zip Makefile
	zip -d ./bundle.zip Makefile
	chmod 644 ./bundle.zip
.PHONY: empty-bundle