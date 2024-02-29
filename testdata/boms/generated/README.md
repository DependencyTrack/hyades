### Generating BOMs for Testing

To generate BOMs from container images listed in `images.txt`, run this from the repository's root directory:

```shell
./scripts/generate-bom-testdata.sh
```

The script uses Trivy to generate BOMs.
