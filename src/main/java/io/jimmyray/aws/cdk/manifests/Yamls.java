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
              annotations:
                service.beta.kubernetes.io/load-balancer-source-ranges: <REMOTE_ACCESS_CIDRS>
            spec:
              ports:
                - port: 80
                  targetPort: 8080
                  protocol: TCP
                  name: http
              type: LoadBalancer
              selector:
                app: read-only""";

    /**
     * https://github.com/aws-samples/ssm-agent-daemonset-installer
     */
    public static final String ssmAgent = """
            apiVersion: v1
            kind: Namespace
            metadata:
              name: node-configuration-daemonset
            ---
            apiVersion: rbac.authorization.k8s.io/v1
            kind: ClusterRole
            metadata:
              name: ssm-agent-installer
            rules:
            - apiGroups: ['policy']
              resources: ['podsecuritypolicies']
              verbs:     ['use']
              resourceNames:
              - ssm-agent-installer
            ---
            apiVersion: v1
            kind: ServiceAccount
            metadata:
              name: ssm-agent-installer
              namespace: node-configuration-daemonset
            ---
            apiVersion: rbac.authorization.k8s.io/v1
            kind: RoleBinding
            metadata:
              name: ssm-agent-installer
              namespace: node-configuration-daemonset
            roleRef:
              kind: ClusterRole
              name: ssm-agent-installer
              apiGroup: rbac.authorization.k8s.io
            subjects:
            - kind: ServiceAccount
              name: ssm-agent-installer
              namespace: node-configuration-daemonset
            ---
            apiVersion: policy/v1beta1
            kind: PodSecurityPolicy
            metadata:
              name: ssm-agent-installer
            spec:
              privileged: true
              hostPID: true
              seLinux:
                rule: RunAsAny
              supplementalGroups:
                rule: RunAsAny
              runAsUser:
                rule: RunAsAny
              fsGroup:
                rule: RunAsAny
            ---
            apiVersion: v1
            kind: ConfigMap
            metadata:
              name: ssm-installer-script
              namespace: node-configuration-daemonset
            data:
              install.sh: |
                #!/bin/bash
                # Update and install packages
                sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm
                STATUS=$(sudo systemctl status amazon-ssm-agent)
                if echo $STATUS | grep -q "running"; then
                    echo "Success"
                else
                    echo "Fail" >&2
                fi
            ---
            apiVersion: apps/v1
            kind: DaemonSet
            metadata:
              name: ssm-agent-installer
              namespace: node-configuration-daemonset
            spec:
              selector:
                matchLabels:
                  job: ssm-agent-installer
              template:
                metadata:
                  labels:
                    job: ssm-agent-installer
                spec:
                  hostPID: true
                  restartPolicy: Always
                  initContainers:
                  - image: jicowan/ssm-agent-installer:1.2
                    name: ssm-agent-installer
                    securityContext:
                      privileged: true
                    volumeMounts:
                    - name: install-script
                      mountPath: /tmp
                    - name: host-mount
                      mountPath: /host
                  volumes:
                  - name: install-script
                    configMap:
                      name: ssm-installer-script
                  - name: host-mount
                    hostPath:
                      path: /tmp/install
                  serviceAccount: ssm-agent-installer
                  containers:
                  - image: "gcr.io/google-containers/pause:2.0"
                    name: pause
                    securityContext:
                      allowPrivilegeEscalation: false
                      runAsUser: 1000
                      readOnlyRootFilesystem: true""";
}
