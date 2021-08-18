FROM alpine:3.14.1

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
    npm install -g aws-cdk

# VOLUME [ "/root/.aws" ]
# VOLUME [ "/opt/app" ]

# Allow for caching python modules
VOLUME ["/usr/lib/python3.8/site-packages/"]

# WORKDIR /opt/app

RUN cdk --version

CMD ["cdk", "--version"]