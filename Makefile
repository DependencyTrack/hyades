# This file is part of Dependency-Track.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# Copyright (c) OWASP Foundation. All Rights Reserved.

MVN := $(shell command -v mvn 2>/dev/null)
MVND := $(shell command -v mvnd 2>/dev/null)
ifeq ($(MVND),)
	MVND := $(MVN)
endif

ifdef CI
	MVN_FLAGS := -B
else
	MVN_FLAGS :=
endif

build:
	@$(MVND) $(MVN_FLAGS) -Pquick package
.PHONY: build

build-images:
	@$(MVND) $(MVN_FLAGS) -Pquick package \
		-Dquarkus.container-image.additional-tags=local \
		-Dquarkus.container-image.build=true
.PHONY: build-images

install:
	@$(MVND) $(MVN_FLAGS) -Pquick install
.PHONY: install

lint-java:
	@$(MVND) $(MVN_FLAGS) validate
.PHONY: lint-java

lint-proto:
	@buf lint
.PHONY: lint-proto

test:
	@$(MVND) $(MVN_FLAGS) -Dcheckstyle.skip -Dcyclonedx.skip verify
.PHONY: test

clean:
	@$(MVND) $(MVN_FLAGS) clean
.PHONY: clean