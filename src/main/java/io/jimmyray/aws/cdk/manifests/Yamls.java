package io.jimmyray.aws.cdk.manifests;

/**
 * Contains static text blocks of K8s manifest YAMLs
 */
public final class Yamls {
    public static final String namespace = """
                                    apiVersion: v1
                                    kind: Namespace
                                    metadata:
                                      name: read-only
                                      labels:
                                        owner: jimmy
                                        env: dev""";

    public static final String deployment = """
            apiVersion: apps/v1
            kind: Deployment
            metadata:
              name: read-only
              namespace: read-only
              labels:
                app: read-only
                owner: jimmy
                env: dev
            spec:
              revisionHistoryLimit: 3
              selector:
                matchLabels:
                  app: read-only
              replicas: 3
              strategy:
                type: RollingUpdate
                rollingUpdate:
                  maxSurge: 10
                  maxUnavailable: 1
              template:
                metadata:
                  labels:
                    app: read-only
                    owner: jimmy
                    env: dev
                spec:
                  securityContext:
                    fsGroup: 2000
                  containers:
                  - name: read-only
                    image: public.ecr.aws/r2l1x4g2/go-http-server:v0.1.0-23ffe0a715
                    imagePullPolicy: IfNotPresent
                    securityContext:
                      allowPrivilegeEscalation: false
                      runAsUser: 1000
                      readOnlyRootFilesystem: true
                    resources:
                      limits:
                        cpu: 200m
                        memory: 20Mi
                      requests:
                        cpu: 100m
                        memory: 10Mi
                    readinessProbe:
                      tcpSocket:
                        port: 8080
                      initialDelaySeconds: 5
                      periodSeconds: 10
                    livenessProbe:
                      tcpSocket:
                        port: 8080
                      initialDelaySeconds: 15
                      periodSeconds: 20
                    ports:
                      - containerPort: 8080
                    volumeMounts:
                      - mountPath: /tmp
                        name: tmp
                  volumes:
                  - name: tmp
                    emptyDir: {}""";

    public static final String service = """
            kind: Service
            apiVersion: v1
            metadata:
              name: read-only
              namespace: read-only
              labels:
                app: read-only
                owner: jimmy
                env: dev
            spec:
              ports:
                - port: 80
                  targetPort: 8080
                  protocol: TCP
                  name: http
              type: LoadBalancer
              selector:
                app: read-only""";
}
