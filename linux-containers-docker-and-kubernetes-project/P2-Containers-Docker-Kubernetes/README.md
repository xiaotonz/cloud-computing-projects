## Task1

```bash
gcloud compute instances delete my-ubuntu-instance --zone=us-east1-b
my-ubuntu-instance
gcloud compute instances update my-ubuntu-instance \
  --update-labels project=containers

gcp-docker-kubernetes-400712
# using ssh connect to the VM
gcloud compute --project gcp-docker-kubernetes-400712 ssh --zone us-east1-b clouduser@student-vm

gcloud compute scp gcp-docker-kubernetes-400712:~/Project_Containers /Users/macbook/Desktop --zone=us-east1-b

gcloud compute scp --recurse student-vm:/home/clouduser/Project_Containers /Users/macbook/Desktop --zone=us-east1-b
```



```yaml
FROM maven:3.8.4-jdk-8-slim AS build

WORKDIR /app

COPY .. /app

RUN mvn clean package



FROM openjdk:8-jre-slim

WORKDIR /app

COPY --from=build /app/target/profile-embedded-0.1.0.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]
```



```
DOCKERFILE_PATH=~/Project_Containers/task1/profile-service-embedded-db/src/main/in/Dockerfile
TAG=profile-service:v1
PROJECT_PATH=~/Project_Containers/task1/profile-service-embedded-db/
docker build -f $DOCKERFILE_PATH -t $TAG $PROJECT_PATH
```



```
# modify the folder name
mv k8s profile-service-embedded-db/src/main/docker
```



```
# Submit
curl 34.139.215.215:8000/profile?username=$xiaotonz@andrew.cmu.edu

export SUBMISSION_USERNAME="xiaotonz@andrew.cmu.edu"
export SUBMISSION_PASSWORD="7Sb0P8KOHuTujhJ0yQvzdv"
```

## Task 2

```
mkdir -p task2/profile-service-embedded-db/src/main/docker

cp ~/Project_Containers/task1/profile-service-embedded-db/src/main/docker/Dockerfile task2/profile-service-embedded-db/src/main/docker/
```



```
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-profile-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-profile
  template:
    metadata:
      labels:
        app: spring-profile
    spec:
      containers:
      - name: spring-profile-container
        image: gcr.io/gcp-docker-kubernetes-400712/profile-service:v1
        ports:
        - containerPort: 8080

```

```
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: spring-profile-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: spring-profile
```

```
# 生成并标记生成的image
DOCKERFILE_PATH=~/Project_Containers/task2/profile-service-embedded-db/src/main/docker/Dockerfile
TAG=profile-service:v1
PROJECT_PATH=~/Project_Containers/task2/profile-service-embedded-db/
docker build -f $DOCKERFILE_PATH -t $TAG $PROJECT_PATH
docker tag $TAG gcr.io/gcp-docker-kubernetes-400712/$TAG
```

```
# 构建的配置文件服务映像推送到 GCR
gcloud auth login

gcloud auth configure-docker

docker push gcr.io/gcp-docker-kubernetes-400712/$TAG
```

```
# 在 GCP 中创建 Kubernetes 集群
gcloud auth application-default login
gcloud config set project gcp-docker-kubernetes-400712
gcloud container get-server-config --zone=us-east1-d 
CLUSTER_NAME="wecloudchatcluster"

## Navigate to the directory and use to deploy the profile service to the cluster with the YAML definitions you developed in step 2:k8s/kubectl
# Using apply (as shown below) or create
kubectl apply -f .
mv deployment.yaml k8s/deployment.yaml
mv service.yaml k8s/service.yaml

## check results
# List all services in the default namespace
kubectl get services
# List all pods in the default namespace
kubectl get pods
# Retrieve the logs for a specific pod
kubectl logs spring-profile-deployment-85755f47c4-5hd9f
# The exec command will give you the terminal access inside of the pod
kubectl exec -it $POD_NAME -- /bin/sh

curl http://34.148.165.182/profile?username=majd
```

删除profile deployment and load balancer

```
cd ~/Project_Containers/task2/profile-service-embedded-db/src/main/k8s
kubectl delete -f .
```

## Task 3

验证Helm

```
helm version

helm repo add bitnami https://charts.bitnami.com/bitnami

helm repo update
```

```
# Before you run the command, set the values of the environment variables $mysqlRootPassword, $mysqlUser and $mysqlPassword using `export`.

# Please avoid using digits only in these variables or the value will be recognized as integer instead of string which may cause error.

export mysqlRootPassword=j1106
export mysqlUser=xiaotonz
export mysqlPassword=j123456


helm install mysql-profile --set auth.rootPassword=${mysqlRootPassword},auth.username=${mysqlUser},auth.password=${mysqlPassword},auth.database=test bitnami/mysql --set image.debug=true \
--set primary.persistence.enabled=false,secondary.persistence.enabled=false \
--set primary.readinessProbe.enabled=false,primary.livenessProbe.enabled=false \
--set secondary.readinessProbe.enabled=false,secondary.livenessProbe.enabled=false
```

```
# copy file
cp -r ~/Project_Containers/task2/profile-service-embedded-db/src/main/docker ~/Project_Containers/task3/profile-service/
PROJECT_PATH=~/Project_Containers/task3/profile-service/
DOCKERFILE_PATH=~/Project_Containers/task3/profile-service/src/main/docker/Dockerfile
TAG=profile-service:v6

docker build -f $DOCKERFILE_PATH -t profile-service:v62 $PROJECT_PATH
```

```
// push new tag
docker tag gcr.io/gcp-docker-kubernetes-400712/profile-service:v62
docker push gcr.io/gcp-docker-kubernetes-400712/profile-service:v62

helm install profile ~/Project_Containers/task3/profile-service/src/main/helm/profile/

helm upgrade profile ~/Project_Containers/task3/profile-service/src/main/helm/profile/
```

```
// check
# get the external ip address
kubectl get pods
kubectl exec spring-profile-deployment-6699fcbb9f-zbl8f -- env
kubectl logs spring-profile-deployment-df8b755df-h8j4h
kubectl get svc
10.32.10.226
curl 4.156.166.217/profile?username=lucas
```

```
helm uninstall profile
```

## task 4

```
helm install my-nginx bitnami/nginx-ingress-controller --version v9.3.24
```

ingress.yaml

### chat part

```
export mysqlRootPassword=j1106
export mysqlUser=xiaotonz
export mysqlPassword=j123456
helm install mysql-chat --set auth.rootPassword=${mysqlRootPassword},auth.username=${mysqlUser},auth.password=${mysqlPassword},auth.database=test bitnami/mysql --set image.debug=true \
--set primary.persistence.enabled=false,secondary.persistence.enabled=false \
--set primary.readinessProbe.enabled=false,primary.livenessProbe.enabled=false \
--set secondary.readinessProbe.enabled=false,secondary.livenessProbe.enabled=false
```



```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
spec:
  ingressClassName: nginx
  rules:
    - http:
        paths:
          - path: /chat
            pathType: Prefix
            backend:
              service:
                name: spring-chat-service
                port:
                  number: 80
          - path: /login
            pathType: Prefix
            backend:
              service:
                name: spring-login-service
                port:
                  number: 80
          - path: /profile
            pathType: Prefix
            backend:
              service:
                name: spring-profile-service
                port:
                  number: 80

```

```
PROJECT_PATH=~/Project_Containers/task3/group-chat-service/
docker build -t gcr.io/gcp-docker-kubernetes-400712/chat-service:v3 .
# docker tag group-chat-service:v1 gcr.io/gcp-docker-kubernetes-400712/chat-service:v1
docker push gcr.io/gcp-docker-kubernetes-400712/chat-service:v3
```

```
helm upgrade chat src/main/helm/chat/
kubectl get pods
kubectl exec spring-chat-deployment-7c49fcb8f8-pzw45 -- env
kubectl exec spring-chat-deployment-7c49fcb8f8-pzw45 -c spring-chat-container -- env
kubectl describe pod spring-profile-deployment-c8fcb4944-9r4xj 

kubectl logs spring-profile-deployment-dc86488d6-52p4x 
kubectl get svc
kubectl describe pod spring-profile-deployment-dc86488d6-52p4x 
```

```
kubectl exec -it spring-chat-deployment-b8bd4dfd7-46zjg -- mysql -h mysql-chat.default.svc.cluster.local -u xiaotonz -p
```

### login in part

```
export mysqlRootPassword=j1106
export mysqlUser=xiaotonz
export mysqlPassword=j123456
helm install mysql-profile --set auth.rootPassword=${mysqlRootPassword},auth.username=${mysqlUser},auth.password=${mysqlPassword},auth.database=test bitnami/mysql --set image.debug=true \
--set primary.persistence.enabled=false,secondary.persistence.enabled=false \
--set primary.readinessProbe.enabled=false,primary.livenessProbe.enabled=false \
--set secondary.readinessProbe.enabled=false,secondary.livenessProbe.enabled=false
```

```
docker build -t gcr.io/gcp-docker-kubernetes-400712/login-service:v2 .
docker push gcr.io/gcp-docker-kubernetes-400712/login-service:v2
helm upgrade login src/main/helm/login/
```

### Profile part

```
docker build -t gcr.io/gcp-docker-kubernetes-400712/profile-service:v7 .
docker push gcr.io/gcp-docker-kubernetes-400712/profile-service:v7
helm install profile src/main/helm/profile/
```

## Part 5

```
cp -r task4/Ingress task4/profile-service task4/group-chat-service task4/login-service task5

```

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-chat-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-chat
  template:
    metadata:
      labels:
        app: spring-chat
    spec:
      containers:
      - name: spring-chat-container
        image: gcr.io/gcp-docker-kubernetes-400712/chat-service:v3
        resources:
          requests:
            cpu: 200m
        envFrom:
        - configMapRef:
            name: spring-chat-config
        ports:
        - containerPort: 8080
---
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: spring-chat-autoscaling
  namespace: default
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: spring-chat-deployment
  minReplicas: 1
  maxReplicas: 10
  targetCPUUtilizationPercentage: 50

helm upgrade profile src/main/helm/profile/

helm upgrade login login-service/src/main/helm/login/

helm upgrade chat group-chat-service/src/main/helm/chat/

kubectl get hpa
kubectl describe hpa

```



```
$ az login --use-device-code

# Check and set the Azure subscription.
$ az account list --output table --refresh
$ az account set --subscription <name or id>

# Initialize these variables

# Container registry resource group
export RESOURCE_GROUP=lab2group

# Azure Container registry, the name must be in all lowercase
export ACR_NAME=xiaotonzacr

# AKS Cluster name
export CLUSTER_NAME=lab2clusterxiaotong

az group create -n ${RESOURCE_GROUP} -l eastus

az acr create -n ${ACR_NAME} -g ${RESOURCE_GROUP} --sku basic --admin-enabled true

az aks create -n ${CLUSTER_NAME} -g ${RESOURCE_GROUP} --attach-acr ${ACR_NAME}  --generate-ssh-keys

az acr login --name ${ACR_NAME} 
# Expected output:
# Login Succeeded
az aks get-credentials --resource-group=${RESOURCE_GROUP} --name=${CLUSTER_NAME}
docker build -t xiaotonzacr.azurecr.io/profile-service:v8 .
# docker tag profile-service:v7 xiaotonzacr.azurecr.io/profile-service:v7
docker push xiaotonzacr.azurecr.io/profile-service:v7
helm upgrade profile src/main/helm-multi-cloud/profile/


docker build -t xiaotonzacr.azurecr.io/login-service:v2 .
# docker tag login-service:v2 xiaotonzacr.azurecr.io/login-service:v2
docker push xiaotonzacr.azurecr.io/login-service:v2
helm upgrade login src/main/helm-multi-cloud/login/
kubectl get pods
kubectl describe pod azure-spring-login-deployment-5d9b6759c-cjztb 
```

```
kubectl config get-contexts  # display list of contexts (i.e., clusters)

kubectl config use-context gke_gcp-docker-kubernetes-400712_us-east1-d_wecloudchatcluster  # set the default context (i.e, set the default cluster you will work on)

export mysqlRootPassword=j1106
export mysqlUser=xiaotonz
export mysqlPassword=j123456
helm install mysql-login --set auth.rootPassword=${mysqlRootPassword},auth.username=${mysqlUser},auth.password=${mysqlPassword},auth.database=test bitnami/mysql --set image.debug=true \
--set primary.persistence.enabled=false,secondary.persistence.enabled=false \
--set primary.readinessProbe.enabled=false,primary.livenessProbe.enabled=false \
--set secondary.readinessProbe.enabled=false,secondary.livenessProbe.enabled=false
```

```
ssh-keygen -t rsa -b 4096 -C "xiaotonz.dev@gmail.com"
```



```
az group delete --name lab2group
gcloud container clusters delete wecloudchatcluster --zone=us-east1-d
```

