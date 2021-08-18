.DEFAULT_GOAL := build

##################################
# git
##################################
GIT_URL ?= $(shell git remote get-url --push origin)
GIT_COMMIT ?= $(shell git rev-parse HEAD)
#GIT_SHORT_COMMIT := $(shell git rev-parse --short HEAD)
TIMESTAMP := $(shell date '+%Y-%m-%d_%I:%M:%S%p')

IMAGE_REPO ?= aws-cdk
DOCKERFILE ?= Dockerfile
NO_CACHE ?= false
GIT_COMMIT_IN ?=
GIT_URL_IN ?=

ifeq ($(strip $(GIT_COMMIT)),)
GIT_COMMIT := $(GIT_COMMIT_IN)
endif

ifeq ($(strip $(GIT_URL)),)
GIT_URL := $(GIT_URL_IN)
endif

VERSION_HASH := $(shell echo $(GIT_COMMIT)|cut -c 1-10)
# $(info [$(VERSION_HASH)])
VERSION_FROM_FILE ?= $(shell head -n 1 version)
VERSION ?=

ifeq ($(strip $(VERSION_HASH)),)
VERSION := $(VERSION_FROM_FILE)
else
VERSION := $(VERSION_FROM_FILE)-$(VERSION_HASH)
endif

.PHONY: build meta

build:	meta
	$(info    [BUILD_CONTAINER_IMAGE])
	docker build -f $(DOCKERFILE) . -t $(IMAGE_REPO):$(VERSION) --no-cache=$(NO_CACHE)
	$(info	)

meta:
	$(info    [METADATA])
	$(info    timestamp: [$(TIMESTAMP)])
	$(info    git commit: [$(GIT_COMMIT)])
	$(info    git URL: [$(GIT_URL)])
	$(info    Container image version: [$(VERSION)])
	$(info	)




