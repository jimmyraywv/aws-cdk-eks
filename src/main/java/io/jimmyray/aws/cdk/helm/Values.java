package io.jimmyray.aws.cdk.helm;

public final class Values {
    public static final String readonlyValues = """
            namespace:
              name: readonly
              labels: {owner: "jimmy",env: "dev",app: "readonly"}
                        
            service:
              name: readonly
              namespace: readonly
              type: LoadBalancer
              port: 80
              targetPort: 8080
              labels: {owner: "jimmy", env: "dev", app: "readonly"}
              selector: {app: "readonly"}
                        
            deployment:
              name: readonly
              namespace: readonly
              labels: {owner: "jimmy", env: "dev", app: "readonly"}
              matchLabels: {app: "readonly"}
              revisionHistoryLimit: 3
              podSecurityContext: {fsGroup: 2000}
              replicaCount: 3
              podTemplateLabels: {owner: "jimmy", env: "dev", app: "readonly"}
              containerName: readonly
              image:
                repository: public.ecr.aws/r2l1x4g2/go-http-server
                pullPolicy: IfNotPresent
                # Overrides the image tag whose default is the chart appVersion.
                tag: "v0.1.0-23ffe0a715"
              securityContext: {capabilities: {drop: [ALL]}, readOnlyRootFilesystem: true, runAsNonRoot: true, runAsUser: 1000, allowPrivilegeEscalation: false}
              # securityContext:
              #   capabilities:
              #     drop:
              #     - ALL
              #   readOnlyRootFilesystem: true
              #   runAsNonRoot: true
              #   runAsUser: 1000
              #   allowPrivilegeEscalation: false
              resources: {limits: {cpu: "200m", memory: "20Mi"}, requests: {cpu: "100m", memory: "10Mi"}}
              # resources:
              #   limits:
              #     cpu: 200m
              #     memory: 20Mi
              #   requests:
              #     cpu: 100m
              #     memory: 10Mi
              readinessProbe: {tcpSocket: {port: 8080}, initialDelaySeconds: 5, periodSeconds: 10}
              # readinessProbe:
              #   tcpSocket:
              #     port: 8080
              #   initialDelaySeconds: 5
              #   periodSeconds: 10
              livenessProbe: {tcpSocket: {port: 8080}, initialDelaySeconds: 15, periodSeconds: 20}
              # livenessProbe:
              #   tcpSocket:
              #     port: 8080
              #   initialDelaySeconds: 15
              #   periodSeconds: 20
              ports: [containerPort: 8080]
              # ports:
              # - containerPort: 8080

            """;
}
