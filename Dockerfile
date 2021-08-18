ARG ALPINE_VERSION=3.13
ARG AWS_CDK_VERSION=1.118.0
FROM alpine:${ALPINE_VERSION}

ENV SOURCE_DIR=/
COPY . $SOURCE_DIR
WORKDIR $SOURCE_DIR

RUN apk -v --no-cache --update add \
        nodejs \
        npm \
        python3 \
        ca-certificates \
        groff \
        less \
        bash \
        make \
        curl \
        wget \
        zip \
        git \
        && \
    update-ca-certificates && \
    npm install -g aws-cdk@${AWS_CDK_VERSION}

# VOLUME [ "/root/.aws" ]
# VOLUME [ "/opt/app" ]

# Allow for caching python modules
VOLUME ["/usr/lib/python3.8/site-packages/"]

# WORKDIR /opt/app

RUN cdk --version

CMD ["cdk", "--version"]