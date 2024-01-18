# P2. Containers: Docker and Kubernetes Linux Containers



## Tagging and Budget Notes on GCP and Azure

### GCP and Azure Tagging

Tag Azure and GCP resources using `project`:`containers`.

### Azure Subscription

Be sure to use the correct subscription when you are using the Azure portal or Azure CLI. As you may have multiple subscriptions, make sure you are using the course subscription for this project. If you have multiple subscriptions, you can use the command below to change the default subscription.

```
    az account list --output table --refresh
    az account set --subscription <name or id>
```

**Warning: your subscription will be disabled if you run out of your subscription budget. Please exercise caution to plan the budget.**



## Note

- It is **bad practice** in the industry to use **personally identifiable information (PII)** to name resources, in this project, please do **NOT** use your name or other PII to name GCP project, Azure resource group, Azure Container Registry hostname, etc. In this project, you will need to include these cloud resource IDs, and you do not want to submit your PII.
- Make sure you have finished the `Intro to Containers and Docker`, `Kubernetes and Container Orchestration`, and `Intro to GitHub Actions` primers before starting this project. Otherwise, it will require much more time to complete this project.



Microservices and Overview

## The Microservices Pattern

In this module, you will practice containerizing and deploying RESTful applications. During this process, you have to make decisions on how to organize the different components of our system. There are two common approaches to this problem. Combining all of the components to define a single deployable artifact (commonly referred to as monolithic applications) or decomposing our system into discrete components that are individually deployable (commonly referred to as microservices).

1. **Monolith** - A single logical application, under which a change to the system involves building and deploying a new version of the entire application.
2. **Microservices** - Loosely coupled applications, that generally communicate over a network and exist independently of each other.

The below table describes some of the common characteristics of `monoliths` and `microservices`:































| Characteristic                 | Monolith                                                     | Microservice                                                 |
| :----------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| Application Size               | The size of the application may be large and will continue to increase over time as the functionalities grow. | The size of each service can stay minimal, because each component is standalone and its functionality is within a bounded scope. |
| Scalability                    | The entire application must be scaled at the same time, which results in increased cost and decreased utilization because not all the components of the application have the same scaling requirements. | Because the services are deployed independently, it’s possible to scale an individual service separately. For example, if the login service becomes a bottleneck, we can scale that service only. |
| Modifiability                  | Each change to the application may require rebuilding and redeploying the whole service. | Microservices enable loose coupling between services. A service can get rebuilt and re-deployed independently. |
| Fault-tolerance (Availability) | Bugs or overload situations would affect the entire application. | Potential issues may only affect a subset of services.       |
| Networking costs               | Because monolithic applications usually run on a single resource, this can reduce the networking cost and reduce the time spent on service to service communication. | Microservices need to communicate using REST, SOAP, etc. The communication between services introduces additional network costs and latencies. |



## The WeChat Architecture

![WeChat Microservices architecture](https://clouddeveloper.blob.core.windows.net/f21-cloud-developer/docker-kubernetes/images/we_chat_arch.png)

**Figure 1:** WeChat Microservices architecture

Many businesses today are experiencing significant growth with respect to the number of users and the number of requests the users generate. The [Overload Control for Scaling WeChat Microservices](https://www.cs.columbia.edu/~ruigu/papers/socc18-final100.pdf) white paper introduces the WeChat architecture, the techniques for handling request overload and the common approaches to building highly scalable services. As described in this whitepaper, WeChat is using the microservice pattern to develop a scalable and resilient application.

To describe the topology of microservices, WeChat refers to services as **nodes** and the connections between services as **edges**. This allows the WeChat designers to model the interaction between services as a directed acyclic graph ([DAG](https://en.wikipedia.org/wiki/Directed_acyclic_graph)). For example, the group chat service will need to call the profile service to retrieve the user’s profile information. By using this architectural pattern, it is possible to modularize each application and independently update, add features, etc. without having to change the design of any of the other microservices that have been identified as a bottleneck. Additionally, structuring applications in this manner aligns with agile methodology, where small teams can work on a specific service.

Converting a monolithic application into individual microservices does create additional complexity with respect to managing the microservices (i.e., handling separate deployments, monitoring, and API versions).

## WeCloud Chat Scenario

![WeCloud Chat architecture](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/task5_architecture.png)

**Figure 2:** WeCloud Chat architecture

As shown in the diagram above, the WeChat architecture has many interconnected components; designing and implementing such a system would require a large number of teams to coordinate. In this project, to simplify the scenario, you are provided with the Java code that implements the login service, the group chat service, and the profile service. Together these services compose the WeCloud Chat application. The provided code is not ready to be deployed as Docker images. Your task is to complete the missing parts of these three services, package them as Docker images, deploy them to a Kubernetes cluster to form a scalable, fault-tolerant application, and to develop a CI/CD workflow that can automate the integration and deployment of your application.

The tools used in this project include the Java Spring Suite (Spring Boot, Spring Websocket, Spring Security, Spring JPA, Spring Cloud Ribbon), Maven, [STOMP](https://en.wikipedia.org/wiki/Streaming_Text_Oriented_Messaging_Protocol) (a sub-protocol over Websocket), [Redis Pub/Sub](https://redis.io/topics/pubsub) (a messaging service feature from Redis), MySQL, Docker, Kubernetes, Helm, and GitHub Actions. You are free to explore the code implementations provided, but you will primarily focus on Docker files, Kubernetes YAML files, and Spring related configurations.

After you complete the project, you will achieve a microservices architecture as defined in the image above. We will deploy three services - the login, chat, and profile services - that form a simple DAG (`[LOGIN] -> [GROUP CHAT] -> [PROFILE]`). The profile service and login service will be replicated across multiple clouds to achieve fault tolerance.

## References

- [Overload Control for Scaling WeChat Microservices](https://www.cs.columbia.edu/~ruigu/papers/socc18-final100.pdf)
- [Stomp Websocket](http://jmesnil.net/stomp-websocket/doc/)

Task 1. Containerizing the Profile Service

## Task 1. Containerizing the Profile Service

### Description

The profile service is a simple REST application that handles `GET` requests to fetch profile data from the database and responds in the JSON format. Each user profile object contains a username, name, gender, and age. The initial implementation of the profile service uses H2, which is an [embedded database](https://en.wikipedia.org/wiki/Embedded_database), to reduce the number of components that need to be deployed. In the subsequent tasks, you will need to modify the Spring configuration and the source code to replace the embedded database(H2) with MySQL.

### Tasks to Complete

#### Orchestrate and manage the architecture with Terraform

Although you are allowed to use the Web UI to provision your resources in this project, we strongly recommend that you use Terraform. We provide you with this prepared template that enables you to provision the required resources in this project.

```
wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/docker-kubernetes/terraform/terraform.tgz && tar -xvf terraform.tgz
```

In order to manage GCP resources with Terraform, you must first set up Google Cloud SDK for authentication and create a GCP project to work with.

#### Setup Google Cloud SDK

1. [Install Cloud SDK](https://cloud.google.com/sdk/docs/downloads-interactive).

2. [Initialize Cloud SDK](https://cloud.google.com/sdk/docs/initializing#run_gcloud_init).

   ```
   gcloud init
   ```

3. Create a GCP project. You may use an existing project, but make sure to follow the other steps.

   ```
   $ gcloud projects create --name gcp-docker-kubernetes
   No project id provided.
   
   Use [gcp-docker-kubernetes-xxxxxx] as project id (Y/n)?  Y
   ```

4. Note down the project ID as you will use the ID very often later, please note the difference between project ID `gcp-docker-kubernetes-xxxxxx` (e.g., `gcp-docker-kubernetes-123456`) and project name (`gcp-docker-kubernetes`). Project ID is a globally unique identifier on GCP.

   ```
   $ gcloud projects list
   PROJECT_ID                 NAME                PROJECT_NUMBER
   gcp-docker-kubernetes-xxxxxx  gcp-docker-kubernetes  ...
   ```

5. Go to https://console.cloud.google.com/billing/projects. Click the "Actions" - "Change Billing" from the dropdown list. Select the billing account that belongs to this course (the account name may be different from the one in the picture) and click "SET ACCOUNT".![Billing & Budget](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/set_billing_account.png)

6. From our previous experience, learners are likely to **exceed** their budget limits in this project. Therefore, we strongly recommend that you estimate and plan your resource usage before getting started. Budgets track expenses within a Google Cloud Platform project or billing account. Your budget can be a specified amount or based on previous spending. At the billing page, select the billing account name you enabled, and search and click "Budgets & alerts" - "CREATE BUDGET".

7. In the Scope section, choose your GCP project for "Projects" and "All services" for "Services". **In the Credits section, you must uncheck all the checkboxes to deselect all the options (e.g., "Discounts", "Promotions and others").** Budget tracks the total cost minus any applicable selected credits. Therefore, in order to calculate and monitor your actual spend before any credits are applied, do not select any credit options.

8. In the Amount section, choose "Specified amount" for "Budget type", and set the target amount as $15 (or lower).![Create Budget](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/gcp_create_budget_amount.png)

9. In the Actions section, set the alert threshold rules (e.g., 50%, 90%, 100%). Check "Email alerts to billing admins and users" so that you will receive emails when you exceed certain thresholds. Finally, click "Finish".

10. Enable the APIs for the GCP project: [Google Compute Engine API](https://console.cloud.google.com/apis/library/compute.googleapis.com), [Google Container Registry API](https://console.cloud.google.com/apis/library/containerregistry.googleapis.com) and [Google Kubernetes Engine API](https://console.cloud.google.com/apis/library/container.googleapis.com). Please ensure that you are enabling APIs for the correct GCP project.

11. Configure gcloud and set the default region, zone, and project, and enable the API when it prompts:

    ```
    gcloud config set project gcp-docker-kubernetes-xxxxxx
    gcloud config set compute/region us-east1
    gcloud config set compute/zone us-east1-b
    ```

12. Configure Application Default Credentials (ADC) to allow the Google Auth library to view and manage your data across Google Cloud Platform services.

    ```
    gcloud auth application-default login
    ```

13. Create a file named `terraform.tfvars` in the same directory where you ran the `wget` command, and set the value of the variable defined in `variables.tf`:

    ```
    project = "gcp-docker-kubernetes-xxxxxx" # your gcp-docker-kubernetes project ID
    ```



Information



- **Always destroy the resources managed by terraform through `terraform destroy`**.

- Be careful when you use `terraform destroy`. It may destroy all your resources previously provisioned by your script, not only the latest one! To destroy a particular resource, you can use `terraform destroy --target=RESOURCE_TYPE.NAME`.

- When working on GCP, if you get any errors related to credentials or permissions, it is likely that you omitted some authentication or configuration step, e.g.

  ```
    gcloud auth application-default login
    gcloud config set project gcp-docker-kubernetes-xxxxxx
    gcloud config set compute/region us-east1
    gcloud config set compute/zone us-east1-b
  ```





### Setting up the Student Instance

1. Launch Student Instance with Terraform by running the following commands in the same directory where you ran the `wget` command to download and extract the Terraform template.

   ```
   terraform init # initialize a Terraform working directory if you have not done so
   terraform apply
   ```

   If you get `The zone does not have enough resources available to fulfill the request` error, try editing the `variables.tf` file and the `VM/variables.tf` to use a different zone. You can consult this [GCP documentation](https://cloud.google.com/compute/docs/regions-zones) to find the available zones.

2. When `terraform apply` completes, Terraform will print the HTTP endpoint of the instance and the command to SSH into the instance. **Note that after `terraform apply` completes, it may take 1-2 minutes before you can access port 80.** Please be patient. You should be able to see the following:

   ```
   student_instance_guide = Please open http://<student-vm-external-ip> in your web browser and select Containers: Docker and Kubernetes.
   Note that it may take 1-2 minutes before you can access it.
   After the installation finishes, SSH into the instance using:
   gcloud compute --project gcp-docker-kubernetes-xxxxxx ssh --zone us-east1-b clouduser@student-vm
   ```

3. Go to `http://<student-vm-external-ip>` and log into your student instance. Choose **Containers: Docker and Kubernetes**, this may take several minutes.

4. Use the `gcloud compute --project gcp-docker-kubernetes-xxxxxx ssh --zone us-east1-b clouduser@student-vm` command shared earlier to connect to the instance. You may be prompted to create a new SSH key if this is the first time you run this command. **Please do not use the GCP console or other commands to ssh into the instance as you may face issues of not seeing the project files or permission issues to run docker.**

5. Get familiar with the provided Maven project, you may import it into an IDE such as [Intellij](https://www.jetbrains.com/help/idea/maven-support.html#maven_import_project_start) to view the source code and comments. Review the project files - including pom.xml, application.properties, ProfileController.java, Profile.java, ProfileRepository.java, ProfileApplication.java - before you move on to the next step.

6. Create a directory named `docker` in `~/Project_Containers/task1/profile-service-embedded-db/src/main/` and develop a `Dockerfile` in it to containerize the Profile service. The Dockerfile should **copy the source code into the container, compile the application in the container** and run the `JAR` file of the profile service. You may want to refer to the example in the `Intro to Containers and Docker` primer. In addition, consider `maven:3.8.4-jdk-8-slim` as the parent image for your Dockerfile. As explained in the primer, the parent image includes system libraries, dependencies and files needed for the container environment.

7. Build a docker image. You can use `docker build -f $DOCKERFILE_PATH -t $TAG $PROJECT_PATH` to build a docker image.

8. Start a container based on the image. You will need to use `docker run -p` to map port `8000` of localhost to the correct port of the container. The application server port is defined in `resources/application.properties`. The docker run command takes in arguments in the order `hostPort:containerPort`. As per the [documentation](https://docs.docker.com/engine/reference/commandline/run/), you should also specify the image to pull.

9. Once the application is containerized and a container is launched, you will be able to use cURL to send a GET request inside the VM via `http://localhost:8000/profile?username=$USERNAME`. For example, http://localhost:8000/profile?username=majd. The service will be available via `VM_IP:8000/profile?username=$USERNAME`, or you may visit `http://VM_IP:8000/profile?username=$USERNAME` in the browser. You can find multiple usernames for testing in `ProfileApplication.java`.

### Hints

1. In order to be ready for the hands-on tasks in this project, make sure that you complete the Docker and Kubernetes primers and practice developing Dockerfiles and using the `docker` and `kubectl` CLI.

2. This Java application requires Java 8, do not use any other Java version during your development and testing. The student virtual machine uses Java 8.

3. If you want to use scp or third-party IDE to transfer files between the student-vm and your local machine, you can locate the SSH key files in the [following locations](https://cloud.google.com/compute/docs/instances/adding-removing-ssh-keys#locatesshkeys):

   ```
   Linux and macOS
   Public key: $HOME/.ssh/google_compute_engine.pub
   Private key: $HOME/.ssh/google_compute_engine
   Windows:
   Public key: C:\Users\[USERNAME]\.ssh\google_compute_engine.pub
   Private key: C:\Users\[USERNAME]\.ssh\google_compute_engine
   ```

   where [USERNAME] is your username on your local workstation.

4. Postman, cURL, or `httpie` are tools that can be used to invoke REST endpoints and you should be familiar with at least one of them.

5. [Spring guides](https://spring.io/guides) provide fully working solutions. You may consider referring to them throughout the project.

   - https://spring.io/guides/gs/spring-boot/
   - https://spring.io/guides/gs/accessing-data-jpa/
   - https://spring.io/guides/topicals/spring-boot-docker/
   - https://spring.io/guides/gs/messaging-stomp-websocket/
   - https://spring.io/guides/gs/spring-cloud-loadbalancer/

6. You can also write your own sh script to pack and run the application in the container.

### What to Submit



Warning



In manual grading, we will grade your **last submission of each task separately**. Therefore, you should have your latest code ready for **your last submission** of each task rather than putting them all in the last task. It is okay if you forget to include all the files in previous submissions as long as you include all the files in the last submission of each task. It is not acceptable to submit the code of all tasks in the last task and expect us to manually grade them.





When submitting, please make sure the following files are in your project folder:

- the Dockerfile under `~/Project_Containers/task1/profile-service-embedded-db/src/main/docker`
- the citation file references under `~/Project_Containers/task1/` to include all the links that you referred to for completing this task.

### How to Submit

1. Before you submit, deploy the profile service on the student-vm by creating a Docker container. You should test that the profile service accepts GET requests:

   ```
   $ curl VM_IP:8000/profile?username=$USERNAME
   ```

2. Export your credentials as follows:

   ```
   export SUBMISSION_USERNAME="your_submission_username"
   export SUBMISSION_PASSWORD="your_submission_pwd"
   ```

3. Under the `task1` directory, run the submitter with `./submitter`.

Task 2. Deploy the Profile Service with GCR and GKE

## Task 2. Using GCR and GKE to Deploy the Profile Service

![Profile Service deployed to GKE](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/task2_architecture.png)

**Figure 3:** Profile Service deployed to GKE

Now that you have packaged the Profile service as a Docker image, you will deploy the service on a Kubernetes cluster in the cloud and make the service available to external clients. You will push the Docker images to the Google Container Registry ([GCR](https://cloud.google.com/container-registry/)) and deploy the applications to the Google Kubernetes Engine ([GKE](https://cloud.google.com/kubernetes-engine/)).

### Tasks to Complete

1. Copy the `Dockerfile` you developed in Task 1 into `task2/profile-service-embedded-db/src/main/docker`. You might want to create the `docker` directory first.

2. Define the cluster state with YAML files in the `task2/profile-service-embedded-db/src/main/k8s` folder. The YAML files should define **three replicas** of the profile service and a load balancer service.

   You will need at minimum 2 YAML files - [deployment.yaml](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) and [service.yaml](https://kubernetes.io/docs/concepts/services-networking/service/).

   The **load balancer** service must be named **spring-profile-service** and the deployment must be named **spring-profile-deployment**. The service port will be port `80` and the service should forward port `80` to port `8080` of the pod.

3. Build and tag the built image with the example commands below. Please refer to the GKE [Pushing and Pulling documentation](https://cloud.google.com/container-registry/docs/pushing-and-pulling) to decide the hostname to use for data centers in the United States.

   ```
   docker build -f $DOCKERFILE_PATH -t $TAG $PROJECT_PATH
   docker tag   [SOURCE_IMAGE]:[TAG]     [HOSTNAME]/[GCP_PROJECT_ID]/[IMAGE]:[TAG]
   ```

4. Push the profile service image built in the previous step to GCR. Refer to the [Pushing and Pulling documentation](https://cloud.google.com/container-registry/docs/pushing-and-pulling) for further information.

   ```
   gcloud auth login
   
   gcloud auth configure-docker
   
   docker push [HOSTNAME]/[GCP_PROJECT_ID]/[IMAGE]
   ```

5. Create a Kubernetes cluster in GCP. We will use the Googe Cloud SDK to create the cluster and retrieve the cluster credentials so you can access the cluster via kubectl. It might take up to 10 minutes for GCP to create the cluster.

   ```
   $ gcloud auth application-default login
   $ gcloud config set project $GCP_PROJECT_ID
   
   # Show the available Kubernetes versions
   $ gcloud container get-server-config --zone=us-east1-d 
   
   # You can modify the cluster name, machine type and the zone
   $ CLUSTER_NAME="wecloudchatcluster"
   $ gcloud container clusters create $CLUSTER_NAME --zone=us-east1-d --num-nodes=1 --machine-type=custom-4-12288 
   
   $ gcloud container clusters get-credentials $CLUSTER_NAME --zone=us-east1-d
   ```

6. Navigate to the `k8s/` directory and use `kubectl` to deploy the profile service to the cluster with the YAML definitions you developed in step 2:

   ```
   # Using apply (as shown below) or create
   $ kubectl apply -f .
   ```

7. Use the following commands to check the deployment results, including viewing the deployed Kubernetes objects and pod logs.

   ```
   # List all services in the default namespace
   $ kubectl get services
   
   # List all pods in the default namespace
   $ kubectl get pods
   
   # Retrieve the logs for a specific pod
   $ kubectl logs $POD_NAME
   
   # The exec command will give you the terminal access inside of the pod
   $ kubectl exec -it $POD_NAME -- /bin/sh
   ```

8. Similar to the previous task, test if the profile service is available. However, unlike the previous task, you will access the service via the load balancer.

   ```
   $ curl http://$LOAD_BALANCER_EXTERNAL_IP/profile?username=$USERNAME
   ```

The external IP of the load balancer can be found using `kubectl get services`.

### What to Submit

When submitting, please make sure the following files are in your project folder:

- the Dockerfile under `~/Project_Containers/task2/profile-service-embedded-db/src/main/docker`
- the K8s YAML files under `~/Project_Containers/task2/profile-service-embedded-db/src/main/k8s`
- the citation file references under `~/Project_Containers/task2/` to include all the links that you referred to for completing this task.

### How to Submit

1. You will submit from your student VM on GCP.

2. Export your submission username and submission password:

   ```
   $ export SUBMISSION_USERNAME="your_submission_username"
   $ export SUBMISSION_PASSWORD="your_submission_pwd"
   ```

3. Your submission will not be successful if the expected service and deployment names are not returned by kubectl. Before running the submitter you should validate that you have the expected deployments and services by running the respective `kubectl` commands:

   

   

   

   

   

   

   

   

   

   | Kubernetes API objects on the GKE cluster | Names                                                   |
   | :---------------------------------------- | :------------------------------------------------------ |
   | Deployments (`kubectl get deployment`)    | spring-profile-deployment                               |
   | Service (`kubectl get svc`)               | spring-profile-service kubernetes (the default service) |

   

4. Use `kubectl` to validate expected deployment and service and cURL to check that you can access the profile service via the load balancer.

5. In the `task2` directory, execute the submitter `./submitter`.

6. In the next tasks, you will **NOT** need the profile deployment and the load balancer service created by kubectl, and failing to remove them can cause the submissions to fail in the next tasks. Once you complete Task 2, please delete the profile deployment and the load balancer service from the GKE cluster you created in this task.

   ```
   $ cd ~/Project_Containers/task2/profile-service-embedded-db/src/main/k8s
   $ kubectl delete -f .
   ```

### Hints

1. For the Intellij users, the [Kubernetes plugin](https://www.jetbrains.com/help/idea/kubernetes.html) will be very helpful in developing the YAML files.
2. You may refer to GKE's https://github.com/GoogleCloudPlatform/kubernetes-engine-samples/tree/master/hello-app/manifests as a example of YAML definitions of service and deployment.
3. Make sure you have not used tabs for indentation in the YAML files. You should use 2 spaces instead. If you have an incorrect indentation in your YAML files, you will see `error converting YAML to JSON` when installing your profile service. Refer to this [website](https://gettaurus.org/docs/YAMLTutorial/) for more details about YAML syntax and debugging.

Task 3. Using Helm Charts and Migrating to MySQL

## Task 3. Using Helm Charts and Migrating to MySQL

![Profile Service with MySQL database](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/task3_architecture.png)

**Figure 4:** Profile Service with MySQL database

As mentioned earlier, the profile service was using an embedded database for simplicity. To make the profile service production-ready, we will migrate from an embedded database to a remote MySQL database. One microservice relying on additional services introduces another level of complexity, fortunately, Helm can help us deploy and configure applications with ease.

We use Kubernetes as a tool to manage and orchestrate containerized applications. As the complexity of your application increases, it is advantageous to use tools that help manage this complexity. Helm is a tool that maintains the relationship of Kubernetes objects within a cluster. Helm helps manage Kubernetes applications. Helm Charts are the primary means of organizing these relationships. Helm Charts helps you define, install, and upgrade even the most complex Kubernetes application. Helm Charts are composed of template files that can be parameterized to deploy uniquely configured versions of an application. Please read the Kubernetes primer to learn more about Helm and how to transform Kubernetes YAML defintions into Helm Charts before you continue.

The deployment architecture of the profile service is the same as task 2. At the end of this task, you will have a profile service, a profile deployment with three replicas, a MySQL service, and a MySQL deployment.

### Tasks to Complete

1. Helm is installed on the student VM. You can validate the helm with the command:

   ```
   $ helm version
   ```

2. Get the latest Chart information from chart repositories, similar to the `apt-get update` command in Linux.

   ```
   $ helm repo add bitnami https://charts.bitnami.com/bitnami
   
   $ helm repo update
   ```

3. Use Helm to deploy the MySQL backend to the GKE cluster. If you took a break and deleted the Kubernetes cluster after completing task 2, you must re-create it. `mysql-profile` needs to be installed in a GKE cluster, so it will not be installed successfully if the GKE cluster has not been created yet.

   ```
   # Before you run the command, set the values of the environment variables $mysqlRootPassword, $mysqlUser and $mysqlPassword using `export`.
   
   # Please avoid using digits only in these variables or the value will be recognized as integer instead of string which may cause error.
   
   $ export mysqlRootPassword=...
   $ export mysqlUser=...
   $ export mysqlPassword=...
   
   $ helm install mysql-profile --set auth.rootPassword=${mysqlRootPassword},auth.username=${mysqlUser},auth.password=${mysqlPassword},auth.database=test bitnami/mysql --set image.debug=true \
   --set primary.persistence.enabled=false,secondary.persistence.enabled=false \
   --set primary.readinessProbe.enabled=false,primary.livenessProbe.enabled=false \
   --set secondary.readinessProbe.enabled=false,secondary.livenessProbe.enabled=false
   ```

4. Validate the helm installation by running the following command. If the above steps were successful, you should not receive any error messages and should see `mysql-profile` in the list:

   ```
   $ helm list
   ```

5. Update the profile service to use the MySQL backend instead of the embedded database.

   1. Update the `~/Project_Containers/task3/profile-service/src/main/resources/application.properties` and `~/Project_Containers/task3/profile-service/pom.xml` by removing the properties for H2 and adding properties for MySQL. Note that the folder was named `profile-service-embedded-db` in Task 1 and Task 2, and the folder in Task 3 is named `profile-service`, e.g., do not directly drag and drop the Task 1 or Task 2 folder into `~/Project_Containers/task3/`.

   2. Remove the dependency on `com.h2database` and add a dependency of the `MySQL connector` in `~/Project_Containers/task3/profile-service/pom.xml`.

      ```
          <dependency>
                  <groupId>mysql</groupId>
                  <artifactId>mysql-connector-java</artifactId>
                  <version>8.0.19</version>
          </dependency>
      ```

6. You will not need to modify any of the Java code, but you will have to **rebuild the Docker image and push it to GCR using a new tag!** Make sure you have copied the `/docker` folder from the previous tasks into the `/main` directory.

7. You can make use of the YAML files developed in the previous task as the starting point, and implement the profile Helm chart by copying over the `deployment.yaml` and `service.yaml` files into the `~/Project_Containers/task3/profile-service/src/main/helm/profile/templates` directory. This time you **must use [configMap](https://kubernetes.io/docs/tasks/configure-pod-container/configure-pod-configmap/#configure-all-key-value-pairs-in-a-configmap-as-container-environment-variables)** for environment variables, failing to do so will result in score deduction during the manual grading.

8. Instead of using `kubectl create -f` or `kubectl apply -f` to create resources from configuration files, we are using Helm to deploy the services to GKE in the following tasks. This means we will use the `helm install` command to install a pre-packaged set of resources. Please refer to the Helm Section in the `Primer: Kubernetes and Container Orchestration` for more information. Install the `profile` chart you developed. You must name the release exactly as `profile`:

   ```
   $ helm install profile profile-service/src/main/helm/profile/
   ```

9. Check the name of the profile pods.

   ```
   $ kubectl get pods
   ```

10. Check the logs to see if you have deployed the profile service successfully.

    ```
    $ kubectl logs $POD_NAME
    ```

11. You can use `kubectl` to retrieve services, pods and application logs similar to what you did in the previous task.

12. After you confirm that the deployment is successful, make a GET request to verify the profile service:

    ```
    $ curl http://LOAD_BALANCER_EXTERNAL_IP/profile?username=USERNAME
    ```

The external IP of the load balancer can be found using `kubectl get services`.

### What to Submit

When submitting, please make sure the following files are in your project folder:

- the Dockerfile under `~/Project_Containers/task3/profile-service/src/main/docker`
- the Helm Chart files under `~/Project_Containers/task3/profile-service/src/main/helm/profile/templates`
- the `~/Project_Containers/task3/profile-service/src/main/resources/application.properties` and `~/Project_Containers/task3/profile-service/pom.xml` which include the MySQL dependency
- the citation file references under `~/Project_Containers/task3/` to include all the links that you referred to for completing this task.

### How to Submit

1. You will submit from your student VM on GCP.

2. Export your submission username and submission password:

   ```
   $ export SUBMISSION_USERNAME="your_submission_username"
   $ export SUBMISSION_PASSWORD="your_submission_pwd"
   ```

3. Before running the submitter, validate that the required helm releases and kubernetes objects exist with the required names.

   

   

   

   

   

   

   

   | Helm objects on the GKE cluster            | Names                 |
   | :----------------------------------------- | :-------------------- |
   | Helm Releases (as returned by `helm list`) | mysql-profile profile |

   

   

   

   

   

   

   

   

   

   

   | Kubernetes API objects on the GKE cluster             | Names                                                        |
   | :---------------------------------------------------- | :----------------------------------------------------------- |
   | Deployments (as returned by `kubectl get deployment`) | spring-profile-deployment                                    |
   | Services (as returned by `kubectl get services`)      | mysql-profile spring-profile-service kubernetes (which is the default service) |

   

4. Similar to the previous task, use kubectl and cURL, to verify that your profile service deployment is able to reply to requests sent to the load balancer.

5. In the `task3` directory, execute the submitter `./submitter`.

6. Once you complete Task 3, please delete the profile release:

   ```
   $ helm uninstall profile
   ```

   This ensures that the K8s cluster is at a clean state that is ready for the next tasks.

### Troubleshooting

1. If you encounter the **ImagePullBackOff** error, please check if the image name and tag in the deployment YAML configuration match the ones pushed in the Google Container Registry.
2. If you encounter the **CrashLoopBackOff** error, please use `kubectl get pods` to get all pods and `kubectl logs POD_NAME` to log the output of specific pods to find error messages.
3. If you encounter the **Communications link failure** error from running `kubectl logs …` on one of the pods, please check if you have deployed MySQL correctly in step 3 by running `helm list`.
4. If you see **Failed to parse the host:port pair `${MYSQL_DB_HOST}:${MYSQL_DB_PORT}`** in the error log, please check if you have correctly linked the configMap in your `deployment.yaml` file.
5. Make sure you have not used tabs for indentation in the YAML files. You should use 2 spaces instead. If you have an incorrect indentation in your YAML files, you will get a `YAML parse error` when installing your profile service. Refer to this [website](https://gettaurus.org/docs/YAMLTutorial/) for more details about YAML syntax and debugging.
6. You can refer to the `Introduction to Helm` section in the `Kubernetes and Container Orchestration` primer to learn about the helm commands. For instance, you can use the command `helm uninstall <helm chart name>` to uninstall a helm chart.

Task 4. WeCloud Chat Microservices

## Task 4. WeCloud Chat Microservices - Putting it Together

![Chat, Login and Profile Services deployed to GKE](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/task4_architecture.png)

**Figure 5:** Chat, Login and Profile Services deployed to GKE

Congratulations! At this point, you have containerized an application as well as deployed it to a Kubernetes cluster running in the cloud. In this task, you will containerize the login service and the chat service similarly and integrate them into the full architecture.

In addition, you need to enable external access to the chat service and the login service. In Task 3, you used a Kubernetes service with the service `type` as `LoadBalancer`, which creates an external load balancer that points to a Kubernetes service in the cluster. Although it is possible to create more load balancers similarly for the chat service and the login service, having multiple external load balancers can be inefficient. Additional public IP addresses on managed K8s clusters can incur extra cost. Besides, having multiple public IPs complicates the usage and the maintenance of the application compared to sharing the same external IP for the application in a holistic approach.

Kubernetes supports a high-level abstraction called [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/), which allows simple traffic routing, e.g., based on URL or hostname. Ingress exposes HTTP and HTTPS routes from outside the cluster to services within the cluster. Traffic routing is controlled by rules defined on the Ingress resource. An ingress is a core concept of Kubernetes but is always implemented by a third party proxy. These implementations are known as ingress controllers. Ingress controller is responsible for reading the Ingress Resource information and processing that data accordingly. The ingress controller you will use in this project is [Ingress-Nginx Controller](https://kubernetes.github.io/ingress-nginx/deploy/).

Below there is a brief introduction of the Chat service and the Login service to help you understand the components of each service. There are no actions you need to take other than reading for the “Chat Service” and “Login Service” sections. The task for you to complete will be specified in the “Tasks to Complete” section.



Information



### (Optional Reading) Implementation of a Real-time Distributed Chat Service

The group chat service uses primarily Stomp and WebSockets to enable real-time communications. WebSockets are better than raw HTTP connections for chat rooms as they provide a two-way full-duplex connection between the client and server. Additionally, WebSockets can reduce the amount of network traffic. HTTP headers are typically around 700 bytes, while WebSocket headers are around 3 bytes. Google Docs and online multiplayer games are [other good applications of WebSockets](https://www.infoworld.com/article/2609720/application-development/9-killer-uses-for-websockets.html).

If too many users access the group chat service at the same time, the service will become overloaded and prone to failure. Hence, scalability is a must for this real-time chat service.

Because the number of connections each instance of the application can handle concurrently is limited, it is better to design a service that can be scaled horizontally. If the group chat service is scaled horizontally, all the replicas must synchronize the data. A common solution to this problem is to use the PubSub pattern, in which there is a messaging service or an in-memory database to synchronize the data among replicas.

Several open-source tools support PubSub semantics, including Redis, RabbitMQ, and Kafka. There are differences between the available options in terms of ease of use, reliability and performance which should be considered when choosing a solution. The following selection of articles discusses some of these considerations.

1. https://tech.trello.com/why-we-chose-kafka/
2. https://aws.amazon.com/compare/the-difference-between-rabbitmq-and-redis/
3. https://www.ibm.com/cloud/learn/message-queues.





### (Reading Only) Chat Service

In this project , the provided application uses Redis’ PubSub to synchronize the chat messages among replicas as it can be easily deployed and managed in a Kubernetes cluster. Besides, the provided application uses MySQL to persist the chat messages, so that users don’t lose their chat history.

### (Reading Only) Login Service

Once you have containerized and deployed the group chat service, you will containerize and deploy the login service similarly. Information security is one of the most important aspects of the login service and it’s important to protect the identity information of your users. Common attacks on software systems include SQL injection, Cross-site scripting (XSS), Cross-site request forgery (CSRF), etc. Adopting an authentication and authorization framework can help address many security vulnerabilities. The provided application adopts Spring Security.

### Tasks to Complete

![Summary of the services to deploy in task 4](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/task4_services.png)

**Figure 6:** Summary of the services to deploy in task 4

#### Create the Ingress resource

1. Create an NGINX ingress controller by running the following command

   ```
   $ helm install my-nginx bitnami/nginx-ingress-controller --version v9.3.24
   ```

2. Create an Ingress resource file named as **ingress.yaml** under `~/Project_Containers/task4/Ingress`. Please refer to [the K8s Ingress Resource documentation](https://kubernetes.io/docs/concepts/services-networking/ingress/#the-ingress-resource) to develop ingress.yaml. You should use **networking.k8s.io/v1** as apiVersion and **nginx** as ingressClassName. In the Ingress Resource documentation, you may notice usage of Kubernetes annotations (`metadata.annotations`) in the example YAML snippet to customize the behavior of the Ingress resource. Note that in this project you do **NOT** need to use any annotations for the Ingress resource. You need to develop the `spec.rules` section per the requirements in the table below:

   

   

   

   

   

   

   

   

   

   

   

   | path     | serviceName            | servicePort |
   | :------- | :--------------------- | :---------- |
   | /chat    | spring-chat-service    | 80          |
   | /login   | spring-login-service   | 80          |
   | /profile | spring-profile-service | 80          |

   

3. Create an Ingress resource by running the following command under the `/Ingress` folder.

   ```
   $ kubectl create -f ingress.yaml
   ```

4. Check the state of the Ingress you just added with

   ```
   $ kubectl get ingress
   ```



Warning



In the `deployment.yaml` file, make sure to use different labels for different services to avoid routing errors. A service includes all pods with the specified label, so using the same label for multiple services may cause a service to route requests of a particular type to the wrong pods. For example, if both the chat service and the profile service use the same label, then the chat service may route a chat request to the profile service’s pods, which could cause an error.





#### Create the profile service

1. Reuse the files in `profile-service` developed in the previous tasks. You will need to copy the profile-service folder to the task4 folder, e.g.,

   ```
   $ cp -r ~/Project_Containers/task3/profile-service ~/Project_Containers/task4
   ```

2. **Delete the profile service you installed with Helm in Task 3 if you haven't.** However, you shouldn’t delete the `mysql-profile` or the docker images that you deployed in Task 3. If you have already deleted them, you should install them again.

3. In this task, you will use Ingress to handle external traffic, so there is no more need for an external load balancer exclusively for the profile service. Therefore, if you reuse the profile-service solution in the previous tasks, you need to change the **type** in **service.yaml** from **LoadBalancer** to **NodePort**. NodePort enables you to set up your own load balancing solution.

4. Install the profile service with the updated **service.yaml** file.

   ```
   $ helm install profile profile-service/src/main/helm/profile/
   ```

#### Create the chat service

1. (Optional) If you are interested in the implementation of the chat Spring application, review the application code as listed below. You do not need to make any changes to these files to complete this task:

   - `group-chat-service/src/main/java`
   - `group-chat-service/src/main/resources`

   Java code contains comments that will help you understand the functionality of the chat service.

2. Deploy the MySQL backend for the chat service. You must use **mysql-chat** as the name of the release.

3. Copy the Dockerfile from the profile service into the `group-chat-service` directory. Make necessary changes to run the appropriate JAR application, and build and push the Docker image for the chat service.

4. As mentioned before, the chat service uses Redis to synchronize the data among replicas. For the Redis deployment, the YAML file (`group-chat-service/src/main/helm/chat/templates/redis.yaml`) is provided for you and you do not need to make any changes to it.

5. A `configMap.yaml` file is also provided in `group-chat-service/src/main/helm/chat/templates/`. You need to update the values of some environment variables.

6. Use the YAML files from the previous tasks as the starting point to develop the chat Helm chart in the same folder as the `configMap.yaml` file. Use the names **spring-chat-deployment** for the deployment, and **spring-chat-service** for the service. The deployments should use three replicas and the service should be a **NodePort** service. Expose port **80** for the service, similar to the profile service.

7. Install the helm chart of the chat service by running the following command. You must use **chat** as the name of the release:

   ```
      $ helm install chat group-chat-service/src/main/helm/chat/
   ```

8. After deploying the chat service, it can be accessed via `http://NGINX_INGRESS_CONTROLLER_LOAD_BALANCER_EXTERNAL_IP/chat`.

#### Create the login service

1. (Optional) Similar to the chat and profile services, you can review the implementation of the login Spring application:

   - `login-service/src/main/java`
   - `login-service/src/main/resources`

2. Deploy the MySQL backend with the name **mysql-login**. Also, build and push the Docker image for the login service.

3. The MySQL data source configuration is provided in `login-service/src/main/resources/application.properties`. You do not need to change `application.properties`. However, you should refer to this file to know what environment variable you need to add into `configMap.yaml`. The value of this variable should be set as the **Ingress controller load balancer’s external IP**, and this value can be found using `kubectl get services`. This is used to redirect the user to the chat service after a successful login.

4. Deploy the login service. Use the names **spring-login-deployment** for the deployment, and **spring-login-service** for the service. The deployments should use three replicas and the service should be a **NodePort** service. Expose port **80** for the service, similar to the profile service.

5. Install the Helm chart of the login service via Helm. You must use **login** as the name of the release:

   ```
      $ helm install login login-service/src/main/helm/login/
   ```

6. Verify the login service. The login Spring application uses Spring Security which adds a CSRF token to the login page to prevent [CSRF](https://en.wikipedia.org/wiki/Cross-site_request_forgery) attack; the CSRF token makes it more difficult to test the requests using tools like cURL or Postman. We suggest that you directly visit the login page via the browser. It might take a few minutes for the login service to be running, so please be patient. `LoginApplication.java` contains the details of the test users that you can use to log into the UI for testing.

### What to Submit

When submitting, please make sure the following files are in your project folder:

- the Dockerfiles under
  - ~/Project_Containers/task4/profile-service/src/main/docker
  - ~/Project_Containers/task4/group-chat-service/src/main/docker
  - ~/Project_Containers/task4/login-service/src/main/docker
- the Helm Chart files under
  - ~/Project_Containers/task4/profile-service/src/main/helm/profile/templates
  - ~/Project_Containers/task4/group-chat-service/src/main/helm/chat/templates
  - ~/Project_Containers/task4/login-service/src/main/helm/login/templates
- the citation file references under `~/Project_Containers/task4/` to include all the links that you referred to for completing this task.

### How to Submit

1. You will submit from your student VM on GCP.

2. Export your submission username and submission password:

   ```
   $ export SUBMISSION_USERNAME="your_submission_username"
   $ export SUBMISSION_PASSWORD="your_submission_pwd"
   ```

3. Before running the submitter, validate that the required Helm releases and Kubernetes objects exist with the required names.

   

   

   

   

   

   

   

   | Helm objects on the GKE cluster | Names                                                        |
   | :------------------------------ | :----------------------------------------------------------- |
   | Helm Releases (`helm list`)     | chat login my-nginx mysql-chat mysql-login mysql-profile profile |

   

   

   

   

   

   

   

   

   

   

   | Kubernetes API objects on the GKE cluster | Names                                                        |
   | :---------------------------------------- | :----------------------------------------------------------- |
   | Deployments (`kubectl get deployment`)    | my-nginx-nginx-ingress-controller my-nginx-nginx-ingress-controller-default-backend redis-deployment spring-chat-deployment spring-login-deployment spring-profile-deployment |
   | Services (`kubectl get services`)         | kubernetes (the default service) my-nginx-nginx-ingress-controller my-nginx-nginx-ingress-controller-default-backend mysql-chat mysql-login mysql-profile redis-service spring-chat-service spring-profile-service spring-login-service |

   

4. Using kubectl, cURL and/or the browser to verify that your service deployments are able to reply to requests via the load balancer and all the services can communicate with one another.

5. In the `task4` directory, execute the submitter `./submitter`.

### Hints

1. Initialization of the services will take time subject to the application size and the cluster configuration (e.g., CPU, memory). Validate that your services are running and are able to handle web requests before you submit.
2. You can use `kubectl` to get the logs of the pods to identify issues.
3. Pressing the show details button on the group chat service UI will invoke the call to the profile service; you should test this via the UI to confirm that the chat service can make requests to the profile service.
4. The provided configMap.yaml for the chat service can be a starting point for you to decide all the Kubernetes environment variables in this task.
5. It may take around 1 minute for the submitter to finish, please wait patiently. If any service is not working, the submission may take longer because of the request timeout.
6. If you encounter the **404 Not Found error**, please make sure that you have specified the `ingressClassName : nginx` parameter in your `ingress.yaml` file. When you apply `ingress.yaml`, the ingress resources will be created and managed by the ingress-nginx instance. nginx is configured to automatically discover all ingress where `ingressClassName: nginx` is present, so without this parameter, your ingress would not be recognized.
7. You can refer to the `Introduction to Helm` section in the `Kubernetes and Container Orchestration` primer to learn about the helm commands. For instance, you can use the command `helm uninstall <helm chart name>` to uninstall a helm chart.

Task 5. Auto-scaling and Multiple Cloud Deployment

## Auto-scaling, Multiple Cloud Deployment and Fault-tolerance

![Login and Profile Service replicated to AKS](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/task5_architecture.png)

**Figure 6:** Login and Profile Service replicated to AKS

You have successfully built and deployed the WeCloud Chat microservices that collectively works as a holistic application.

In this task we will take advantage of Kubernetes features that make it easy to horizontally scale your microservices based on pod metrics. The Horizontal Pod Autoscaler (HPA) handles the complexities of triggering auto-scaling for deployments:

1. [Horizontal Pod Autoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
2. [Autoscaling in Kubernetes](https://kubernetes.io/blog/2016/07/autoscaling-in-kubernetes/)

HPA allows automatic update of workload resources to match the demand being experienced based on a set of metrics set. Akin to your experience with horizontal scaling in project 1, you may write a HPA config file to set a policy to horizontally scale your resources when the CPU usage is higher than 50%. So when there are more requests and the CPU resources usage exceeds 50%, it will automatically deploy more Pods to handle the additional load.

![Horizontal Pod Autoscaler Architecture](https://clouddeveloper.blob.core.windows.net/f21-cloud-developer/docker-kubernetes/images/horizontal-pod-autoscaler.png)

This is an architecture diagram to explain how HPA config works. The HPA controls the scale of a Deployment and its ReplicaSet to automatically scale in and out based on the metrics set.

Once you have created the HPAs for each service, we will introduce the idea of multi-cloud deployments and fault-tolerance. While cloud platforms like AWS, Azure and GCP are very reliable and do not encounter long term outages frequently, it’s possible that one cloud platform could suffer from a temporary or transient outage in a specific zone or region. To mitigate the impact of such an outage in one cloud service provider, we can deploy our application to multiple clouds. This will achieve fault-tolerance and potentially improved performance (if the traffic is routed between the different cloud deployments based on some heuristic).

We will experiment with multi-cloud deployments. Specifically, we will replicate the login and profile services to Azure. Replicating the chat service across multiple clouds would require mechanisms to share the Redis PubSub data and is beyond the scope of this project.

### Tasks to Complete

1. Copy the 3 working services and Ingress resource from the task4 folder to the task5 folder.

   ```
   cp -r task4/Ingress task4/profile-service task4/group-chat-service task4/login-service task5
   ```

2. In this step, you will create a **HorizontalPodAutoscaler (HPA)** for every deployment - the profile, chat and login services. You should monitor the CPU utilization of the deployments to determine an appropriate **targetCPUUtilizationPercentage** level to ensure scaling will occur under a high load.

   WeChat uses queue-based request routing and supports different priorities for different kinds of requests, so they may use additional metrics for scaling. CPU utilization is a reasonable metric to use to trigger scaling in this project. You **only** need to make changes to the YAML files under the **helm** folder of each service.

   Use the HPA template below as the starting point. Please pay attention to the apiVersion, a GKE cluster supports multiple HorizontalPodAutoscaler API object versions such as stable version `autoscaling/v1` and beta version `autoscaling/v2beta2`. In this project, you must use the stable version `autoscaling/v1`. In addition, please make sure to use 1 as the `minReplicas` value. Note that you must make necessary changes in order to make the provided template below work.

   ```
   {other contents in deployment.yaml}
   ---
   apiVersion: autoscaling/v1
   kind: HorizontalPodAutoscaler
   metadata:
     name: spring-profile-autoscaling
     namespace: default
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: php-apache
     minReplicas: 1
     maxReplicas: 10
     targetCPUUtilizationPercentage: 50
   ```

   To ensure all your services are able to scale up properly, you need to add an HPA to **each deployment.yaml** file except for the Redis deployment in redis.yaml, i.e., you will add 3 HorizontalPodAutoscaler sections in total (1 for profile, 1 for chat, and 1 for login).

3. **You need to add the following lines of code in the containers object in each deployment.yaml file for HPA to work**. The following code specifies a value (e.g., 200 milli-cores) for CPU requests for each pod. By specifying requests for the resource, you are able to autoscale based on the target CPU utilization as a relative percentage. Please refer to the example Deployment configuration in [this page](https://cloud.google.com/kubernetes-engine/docs/how-to/horizontal-pod-autoscaling) to add HPA configuration to your `deployment.yaml` file.

   ```
      resources:
       requests:
         cpu: 200m
   ```

4. Use **Helm** to update each service in the GKE cluster, **DO NOT** use `kubectl`.

   ```
   $ helm upgrade profile profile-service/src/main/helm/profile/
   
   $ helm upgrade login login-service/src/main/helm/login/
   
   $ helm upgrade chat group-chat-service/src/main/helm/chat/
   ```

5. Validate that the HPA contains two percentage numbers - **Target CPU utilization** and **Current CPU utilization** by running `kubectl describe hpa`.

   You might get the `FailedGetResourceMetric` error at first. There are two possible situations:

   1. Your code is correct and the error would disappear if you run the command a few minutes later. The error occurs because it takes some time for the pods to retrieve the current CPU utilzation level.
   2. Your code is problematic. Please read the instructions and review example configurations in the provided links carefully and troubleshoot your HPA configuration.

6. Once you have created the HPAs for each service on the GKE cluster, you will create an AKS cluster to implement a multi-cloud deployment. First, create a new [AKS cluster with ACR integration](https://docs.microsoft.com/en-us/azure/aks/cluster-container-registry-integration). Azure Container Registry (ACR) name should be globally unique. Creating an AKS cluster may take up to 30 minutes to complete, so be patient.

   ```
   $ az login --use-device-code
   
   # Check and set the Azure subscription.
   $ az account list --output table --refresh
   $ az account set --subscription <name or id>
   
   # Initialize these variables
   
   # Container registry resource group
   $ export RESOURCE_GROUP=... 
   
   # Azure Container registry, the name must be in all lowercase
   $ export ACR_NAME=...
   
   # AKS Cluster name
   $ export CLUSTER_NAME=...
   
   $ az group create -n ${RESOURCE_GROUP} -l eastus
   
   $ az acr create -n ${ACR_NAME} -g ${RESOURCE_GROUP} --sku basic --admin-enabled true
   
   $ az aks create -n ${CLUSTER_NAME} -g ${RESOURCE_GROUP} --attach-acr ${ACR_NAME}  --generate-ssh-keys
   ```

7. Before pushing and pulling container images, you must log in to the container registry with the `az acr login` command.

   ```
   az acr login --name ${ACR_NAME} 
   # Expected output:
   # Login Succeeded
   ```

8. Run the following commands to set up the access to the AKS cluster. Note that you should always remember to run this `az aks get-credentials` command once before you can use `kubectl` to manage an AKS cluster.

   ```
   az aks get-credentials --resource-group=${RESOURCE_GROUP} --name=${CLUSTER_NAME}
   # Expected output:
   # Merged "${CLUSTER_NAME}" as current context in .../.kube/config
   ```

9. Now you have set up the connection to multiple Kubernetes clusters, you need to switch between Kubernetes clusters by using the following commands:

   ```
    $ kubectl config get-contexts  # display list of contexts (i.e., clusters)
   
    $ kubectl config use-context GCP_OR_AZURE_CONTEXT  # set the default context (i.e, set the default cluster you will work on)
   ```

10. After you have **changed the context to Azure Kubernetes clusters**, create an ingress controller by running the following code:

    ```
     $ helm install my-nginx bitnami/nginx-ingress-controller --version v9.3.24
    ```

11. Create an Ingress resource file **ingress.yaml** under **/home/clouduser/Project_Containers/task5/Ingress_Azure**. The path and service should have the following mapping:

    

    

    

    

    

    

    

    

    

    | path     | serviceName            | servicePort |
    | :------- | :--------------------- | :---------- |
    | /login   | spring-login-service   | 80          |
    | /profile | spring-profile-service | 80          |

    

12. Create an Ingress resource by running the following code

    ```
     $ kubectl create -f ingress.yaml
    ```

13. Create a new folder under `profile-service/src/main/` called **helm-multi-cloud**. Copy the profile chart from `profile-service/src/main/helm` folder to `profile-service/src/main/helm-multi-cloud`. You may use the helm charts and YAML files developed previously as the starting point, but do not forget to **update the image registry**. You will need to use the Azure’s registry address, which will be of the form `{ACR_NAME}.azurecr.io`.

14. Similarly, we want to ensure that HPA is being added to our services in Azure. Following the same syntax, here’s another reference you can use to help you add the HPA for the profile service on the [AKS cluster](https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-scale#autoscale-pods).

    ```
     # Sample for defining Azure HPA:  [Hint: you may need to set specific CPU requests ]
    
     {other contents in deployment.yaml}
     ---
     apiVersion: autoscaling/v1
     kind: HorizontalPodAutoscaler
     metadata:
       name: azure-vote-front-hpa
     spec:
       maxReplicas: 10 # define max replica count
       minReplicas: 1  # define min replica count
       scaleTargetRef:
         apiVersion: apps/v1
         kind: Deployment
         name: azure-vote-front
       targetCPUUtilizationPercentage: 50 # target CPU utilization
    ```

15. Install a separate MySQL service for the profile service on the AKS cluster.

16. After you have installed MySQL for the profile service on Azure, revisit the `configMap.yaml` of the profile service on Azure and decide if you need to update any environment variables.

17. Install the profile service on Azure and validate that it returns the expected results. Since we are using Azure at this step, be sure to **push the profile docker image to ACR**. Otherwise, the pods may not be able to pull the image from the container registry. You can refer to this [documentation](https://learn.microsoft.com/en-us/azure/container-instances/container-instances-tutorial-prepare-acr#push-image-to-azure-container-registry) to learn about pushing images to ACR.

18. Update the chat service on GCP so that it can communicate with the services running in Azure.

    1. Modify the `LIST_OF_PROFILE_ENDPOINTS` variable in `configMap.yaml` of the chat service on GCP and add the external IP of the load balancer of the Ingress service on Azure.
    2. **Switch the context to the GKE cluster**, and reinstall the chat service on the GCP.

19. Finally, deploy the login service to Azure.

    1. **Switch the context to the AKS cluster**.
    2. Create a new folder under `login-service/src/main/` called **helm-multi-cloud**.
    3. Copy the login chart from `login-service/src/main/helm` folder to `login-service/src/main/helm-multi-cloud`. Make sure to update the the image in `deployment.yaml` with the correct name.
    4. Add the HPA with the apiVersion as `autoscaling/v1`.
    5. Install a separate MySQL service for the login service on Azure.
    6. After you have installed MySQL for the login service on Azure, revisit the `configMap.yaml` of the login service on Azure and decide if you need to update any environment variables.
    7. Install the login service to Azure using Helm.

### What to Submit

When submitting, please make sure the following files are in your project folder:

- the Dockerfiles under
  - ~/Project_Containers/task5/profile-service/src/main/docker
  - ~/Project_Containers/task5/group-chat-service/src/main/docker
  - ~/Project_Containers/task5/login-service/src/main/docker
- the Helm Chart files under
  - ~/Project_Containers/task5/profile-service/src/main/helm/profile/templates
  - ~/Project_Containers/task5/profile-service/src/main/helm-multi-cloud/profile/templates
  - ~/Project_Containers/task5/group-chat-service/src/main/helm/chat/templates
  - ~/Project_Containers/task5/login-service/src/main/helm/login/templates
  - ~/Project_Containers/task5/login-service/src/main/helm-multi-cloud/login/templates
- the citation file references under `~/Project_Containers/task5/` to include all the links that you referred to for completing this task.

### How to Submit

1. You will submit from your student VM on GCP.

2. Export your submission username and submission password:

   ```
   $ export SUBMISSION_USERNAME="your_submission_username"
   $ export SUBMISSION_PASSWORD="your_submission_pwd"
   ```

3. Before running the submitter, validate that the required helm releases and kubernetes objects exist with the required names.

   

   

   

   

   

   

   

   | Helm objects on the GKE cluster | Names                                                        |
   | :------------------------------ | :----------------------------------------------------------- |
   | Helm Releases (`helm list`)     | mysql-chat mysql-profile mysql-login my-nginx chat profile login |

   

   

   

   

   

   

   

   

   | Helm objects on the AKS cluster | Names                                            |
   | :------------------------------ | :----------------------------------------------- |
   | Helm Releases (`helm list`)     | my-nginx mysql-profile mysql-login profile login |

   

   

   

   

   

   

   

   

   

   

   

   

   | Kubernetes API objects on the GKE cluster | Names                                                        |
   | :---------------------------------------- | :----------------------------------------------------------- |
   | Deployments (`kubectl get deployment`)    | my-nginx-nginx-ingress-controller my-nginx-nginx-ingress-controller-default-backend redis-deployment spring-chat-deployment spring-login-deployment spring-profile-deployment |
   | Services (`kubectl get services`)         | kubernetes (the default service) my-nginx-nginx-ingress-controller my-nginx-nginx-ingress-controller-default-backend mysql-chat mysql-login mysql-profile redis-service spring-chat-service spring-login-service spring-profile-service |
   | HPAs (`kubectl get hpa`)                  | spring-login-autoscaling (name of your hpa for login service) spring-profile-autoscaling (name of your hpa for profile service) spring-chat-autoscaling (name of your hpa for chat service) |

   

   

   

   

   

   

   

   

   

   

   

   

   | Kubernetes API objects on the AKS cluster | Names                                                        |
   | :---------------------------------------- | :----------------------------------------------------------- |
   | Deployments (`kubectl get deployment`)    | my-nginx-nginx-ingress-controller my-nginx-nginx-ingress-controller-default-backend spring-profile-deployment spring-login-deployment |
   | Services (`kubectl get services`)         | kubernetes (the default service) my-nginx-nginx-ingress-controller my-nginx-nginx-ingress-controller-default-backend mysql-login mysql-profile spring-login-service spring-profile-service |
   | HPAs (`kubectl get hpa`)                  | spring-login-autoscaling (name of your hpa for login service) spring-profile-autoscaling (name of your hpa for profile service) |

   

4. Using kubectl, cURL and the browser to verify that your service deployments are able to reply to requests via the load balancer and all the services can communicate with one another.

5. In the `task5` directory, execute the submitter `./submitter`.

### Hints

1. `kubectl get deployment` will tell you the number of desired, current and available replicas for a given deployment. `kubectl get HorizontalPodAutoscaler` will return a summary of the autoscaling policy you have defined in your YAML template. `kubectl get hpa` will additionally show the CPU utilization for the deployment each autoscaler is monitoring.
2. Note that you should confirm you are in the expected context when running any helm or kubectl commands. `kubectl config get-contexts` lists the contexts available and `kubectl config use-context CONTEXT_NAME` will switch between available contexts.
3. For the Azure HPA objects, you will need to specify the CPU usage for the **deployment.yaml**, otherwise, you might not be able to get the CPU metrics. **Remember to use API version autoscaling/v1 for GCP and Azure. Please refer to the [official documentation](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/)!**
4. If you are failing the HPA for Azure, revisit step 2 and ensure you have added the resources object under the containers object in each of the deployment.yaml files. Also, do remember that HPA should scale up resources when the metric condition is met and scale down resources otherwise as we will be testing both of these use cases.

### Troubleshooting

1. If you get '-' as your score, please make sure the services are all running and connected, double check the endpoint of both profile services are reflected in the deployment.yaml for chat service.

2. Deleting an Azure cluster sometimes cannot delete the contexts in kubectl. If running `az aks get-credentials` returns the error message `A different object named XXX already exists in YYY` you should unset the previous context values by running:

   ```
   $ kubectl config unset users.[USER]
   $ kubectl config unset contexts.[CONTEXT]
   $ kubectl config unset clusters.[CLUSTER]
   ```

3. If you encountered "az: error: unrecognized arguments: --attach-acr myContainerRegistry", double-check you are using the latest version of Azure CLI.

4. If you encounter the **ImagePullBackOff** error in Azure, please refer to the official [Azure CLI documentation](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-authentication) and [docker authentication](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-get-started-azure-cli) for more information. It is very likely that you did not follow [AKS to ACR integration](https://docs.microsoft.com/en-us/azure/aks/cluster-container-registry-integration).

5. If you encounter the **CrashLoopBackOff** error in Azure, double-check you enabled the MySQL backend.

6. If you encounter the **404 Not Found error**, please make sure that you have specified the `ingressClassName : nginx` parameter in your `ingress.yaml` files. When you apply `ingress.yaml`, the ingress resources will be created and managed by the ingress-nginx instance. nginx is configured to automatically discover all ingress where `ingressClassName: nginx` is present, so without this parameter, your ingress would not be recognized.

7. You can refer to the `Introduction to Helm` section in the `Kubernetes and Container Orchestration` primer to learn about the helm commands. For instance, you can use the command `helm uninstall <helm chart name>` to uninstall a helm chart.

Task 6. Domain Name with Azure Front Door

## Domain Name with Azure Front Door

In the real world, it is not practical to visit websites with IP addresses since IPs are hard for human beings to remember. Imagine that you have to remember all your friend’s phone numbers to make phone calls! Domain Name System (DNS) is like a phone book for the internet. If you know a person’s name but don’t know their telephone number, you can simply look it up in a phone book. What’s more, your friend may have multiple phone numbers, and in most cases, you don’t really care which one you dialed as long as you can reach your friend. In our previous tasks, we have deployed the profile service, the login service and the chat service on Azure and GCP, and we are left with two IPs. Similar to the phone book example, we can define routing rules to map a single domain name to these two IPs. In this task, you will use Azure Front Door Service to achieve a path-based routing to the web application deployed on Azure and GCP.

As per official Microsoft docs, “Azure Front Door Service enables you to define, manage, and monitor the global routing for your web traffic by optimizing for best performance and instant global failover for high availability. With Front Door, you can transform your global (multi-region) consumer and enterprise applications into robust, high-performance personalized modern applications, APIs, and content that reach a global audience with Azure”.

In this task, you will experiment with terraform to create and configure an Azure Front Door resource that will expose a single frontend endpoint that is able to route traffic to the multiple-cloud deployment.

The configuration for Azure Front Door Service consists of 3 steps:

**a) Frontends/domains** specifies a frontend endpoint that gets exposed to the Internet. Traffic sent to the frontend endpoint will get routed to the backend pool(s) as per the routing rules. The frontend endpoint will be a subdomain on Front Door's default domain i.e. azurefd.net.

**b) Backend pools** are a set of equivalent backends to which Front Door load balances your client requests.

**c) Routing rules** route traffic received by the frontend endpoint that matches a URL path pattern to a specific backend pool.

Below is a relation diagram to better understand the relationship between all the resources inside the Front Door instance. For more information, please read the following [document](https://learn.microsoft.com/en-us/azure/application-gateway/application-gateway-components).

![relation diagram](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/docker-kubernetes/images/relation_diagram.png)

There are two `.tf` files provided under the `task6` folder: `main.tf` file and `variables.tf` file. The `main.tf` file is the main file that specifies the configurations for your Front Door service. You have to fill in the necessary arguments in this file to correctly deploy your Front Door instance. The `variables.tf` file defines the necessary variables that will be used in the `main.tf` file. You must not make any modification to the `variables.tf` file. However, you will have to create a `terraform.tfvars` file to specify the values to the variables defined in the `variables.tf` file.

### Tasks to Complete

1. Refer to this document [**azurerm_frontdoor documentation**](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs/resources/frontdoor#address) to complete the TODOs in the `main.tf` file based on the instructions in the following steps.

2. The `frontend_endpoint` block is already defined in the file and doesn't require any changes. This block specifies the hostname of your services which is accessible to the Internet.

3. Front Door sends periodic HTTP/HTTPS probe requests to each of your configured backends to determine the proximity and health of each backend to load balance your end-user requests. Therefore, in order to create a backend pool, you first need to define a **health probe**.

   **Fill in the required components for the two health probes in `main.tf` based on the table below.**

   

   

   

   

   

   

   

   

   

   | Name       | Path   | Protocol | Probe method | Interval (s) |
   | :--------- | :----- | :------- | :----------- | :----------- |
   | probelogin | /login | Http     | HEAD         | 30           |
   | probechat  | /chat  | Http     | HEAD         | 30           |

   

   **Table**: Configuration for the two health probes.

4. Next, you need to configure the load balancing settings to define what sample set to use to call the backend healthy or unhealthy. The default value for latency sensitivity is 0, which means it always sends the request to the fastest available backend. If the latency sensitivity is set to non-zero, Front Door will only round robin traffic between backends whose latencies are within the configured latency sensitivity. You will need to set a reasonable value (e.g. 50) so that the Azure Front Door resource will round-robin traffic between the GKE and AKS clusters.

   **Fill in the `backend_pool_load_balancing` block with the following specifications:**

   

   

   

   

   

   

   

   | Name                | Sample size | Successful samples required | Additional latency (ms) |
   | :------------------ | :---------- | :-------------------------- | :---------------------- |
   | wecloudloadbalancer | 4           | 2                           | 50                      |

   

   **Table**: Configuration for the load balancing setting.

5. Now you will need to create the **backend pools** and the **routing rules** for your Front Door instance. Since routing rules depend on backend pools, we will start with the backend pools. A backend pool is a set of equivalent backends which the Front Door uses to balance the client request load to the respective service. Based on the architecture of the WeCloud Chat application in Task 5, you will need to create two backend pools:

   For the login service and the profile service, which exist on both the GKE and AKS clusters, you need a backend pool consisting of two IPs: one is the external IP of the Ingress on GCP, and the other one is the external IP of the Ingress on Azure.

   For the chat service, which exists only on GCP, you need a second backend pool consisting of only a single IP of GCP.

   Let's first create the **backend pool for the login and profile services** which should contain both IPs from GKE and AKS as the backend. In the backend blocks, you should set the **HTTP port to 80** and the **HTTPS port to 443**. Additionally, connect the backend pool to the previously defined **health probe for the login service** and **load balancing settings** by specifying their names.

6. Then you will create a second backend pool for the chat services which should only contain the IP of the GKE cluster as the backend. Again, set the **HTTP port to 80** and the **HTTPS port to 443**. Connect the backend pool to the previously defined **health probe for the chat service** and **load balancing settings** by specifying their names.

7. With the frontend endpoint and the two backend pools created, you will now create two routing rules: one for the profile and the login service, and another one for the chat service. The specifications for them are listed below:

   

   

   

   

   

   

   

   

   

   | Name                | Accepted protocols | Patterns to match | Forwarding protocol |
   | :------------------ | :----------------- | :---------------- | :------------------ |
   | loginprofilerouting | Http               | /login, /profile  | Http only           |
   | chatrouting         | Http               | /chat, /chat/*    | Http only           |

   

   **Table**: Configuration for the routing rules.

   Make sure that you connect each routing rule to the **corresponding backend pool** and the **frontend endpoint**.

8. To set up the necessary variables, inspect `main.tf` and `variables.tf` files carefully to identify which variables you need to define. Once identified, create and specify these variables in the `terraform.tfvars` file under the `task6` folder. Keep in mind that your Front Door name is unique and should be between 5 to 64 characters long, using only alphanumerics and hyphens, and it must begin and end with an alphanumeric character.

9. Launch the Front Door service by running the commands below in `task6` directory.

   ```
    $ terraform init 
    $ terraform apply
   ```

### Validate Your Work

#### Validate the Azure Front Door configuration in the Azure Portal

Now you have designed the whole architecture for the Front Door, double check you have the same architecture as the following figure. You can check your Front Door’s configuration by selecting it from **All resources** page in [Azure portal](https://portal.azure.com/#home).

![dns_image12](https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/docker-kubernetes/images/frontdoor_designer_image.png)**Figure:** Overview of Front Door configurations.

#### Validate the Azure Front Door endpoints in the Azure Portal

The configurations you made above will take effect **within several minutes**. Please make sure that you validate the endpoints of your Azure Front Door instance before running the submitter. The endpoints for each service are as follows: `http://<frondoor-name>.azurefd.net:80/login`, `http://<frondoor-name>.azurefd.net:80/chat?username=majd`, and `http://<frondoor-name>.azurefd.net:80/profile?username=majd`.

### What to Submit

The citation file under `~/Project_Containers/task6/` to include all the links that you referred to for completing this task.

### How to Submit

1. You will submit from your student VM on GCP.

2. Export your submission username and submission password:

   ```
   $ export SUBMISSION_USERNAME="your_submission_username"
   $ export SUBMISSION_PASSWORD="your_submission_pwd"
   ```

3. In the `task6` directory, execute the submitter `./submitter`.

### Clean up Resources

After finishing task 6, delete the resources deployed by Terraform by running the command below in the task6 folder:

```
  $ terraform destroy
```

Task 7. Automate the build and deployment

## Task 7 - Automate the build and deployment of microservices with CI/CD

In the previous tasks, you gained experience in constructing and launching the given microservices application. Now, it's time to streamline the deployment process through automation.

Your objective in this task is to establish a CI/CD pipeline utilizing GitHub Actions that consists of five jobs. These jobs will detect changes to the microservices, and if necessary, rebuild and push Docker images to ACR and GCR. They will also deploy the microservices to the GKE and AKS clusters. To accomplish this, you'll need to employ conditional expressions, strategies, and job output. Prior to proceeding with this task, please read and complete the `Intro to GitHub Actions` primer.

### GitHub Settings

If you are not familiar with the git commands, please refer to this [Microsoft Learn Git and GitHub Basic Course](https://learn.microsoft.com/training/paths/intro-to-vc-git) to learn before starting this task.

Authenticate with GitHub using the command below. Choose `GitHub.com` for the account you want to log into, `SSH` as the preferred protocol, and say `Yes` to generate a new SSH key to add to your GitHub account. You can then use your web browser to authenticate with GitHub CLI.

```
$ gh auth login
```

Navigate to the task7 directory and initialize it as a Git repository using the following command:

```
$ git init -b main
```

Configure your user information to let GitHub know who's committing the changes.

```
$ git config --global user.email "<your-email-address>"
$ git config --global user.name "<your-username>"
```

Copy all of the files related to the microservices to the remote repository using the following commands:

```
$ cp -r ../task5/Ingress ../task5/Ingress_Azure ../task5/profile-service ../task5/login-service .
$ cp -r ../task5/group-chat-service chat-service
```

Create a new repository with the name `containers-devops` from the local repository and push all of the existing commits using the following commands:

```
$ git add . && git commit -m "initial commit"
$ gh repo create containers-devops --source=. --private --push
```

If you get a `Permission denied (publickey)` error, try following the [troubleshooting documentation](https://docs.github.com/en/authentication/troubleshooting-ssh/error-permission-denied-publickey?platform=linux) or [generating and adding a new SSH key](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent?platform=linux) on your student VM.

By following these steps, you will have a Git repository set up in your task7 directory with all the necessary files. You can confirm that the new repository have been created by visiting your GitHub repositories page.

### Authentication Settings

Before proceeding, it's important to set up several [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets) that contain the necessary resource identifiers and credentials. This will allow GitHub Actions to access your GCP and Azure resources seamlessly.

Here's a list of the tokens that you need to configure in your GitHub Secrets:



























| Secret                 | Description                                                  |
| :--------------------- | :----------------------------------------------------------- |
| `AZ_REGISTRY_USERNAME` | The username for GitHub Actions to log in to your ACR        |
| `AZ_REGISTRY_PASSWORD` | The password for GitHub Actions to log in to your ACR        |
| `AZ_CREDENTIALS`       | The credential for GitHub Actions to control your Azure Kubernetes Service (AKS) using an Azure Service Principal |
| `GCP_SA_KEY`           | The service account key for GCP                              |



Below are the detailed instructions to set up those tokens:

1. Open your code repository in GitHub and go to the `Settings` page. Under the `Secrets and variables` section, click on `Actions`. Then navigate to the `Secrets` tab and click on `New repository secret` as shown in the image below:

   ![img](https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/docker-kubernetes/images/github_actions_secret.png)

2. Open the Azure Portal in a separate tab and search for "Container registries". On the Container registries page, navigate to the ACR you provisioned for this project. Under "Settings", select "Access keys" and enable the "Admin user". You should then be able to view the "Username" and "Password" as shown below:

   ![acr_keys](https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/docker-kubernetes/images/acr_password.png)

3. To add a new secret, click on `New secret` in the GitHub repository settings. For the first secret, specify the `Name` as `AZ_CONTAINER_REGISTRY` and the `Value` as the Login server (<lowercased_acr_name>.azurecr.io).

4. Add the new secret `AZ_REGISTRY_USERNAME` using the `Username` obtained in Step 2 as the value.

5. Add a new secret `AZ_REGISTRY_PASSWORD` using the `Password` obtained in Step 2 as the value.

6. Run `az ad sp create-for-rbac --sdk-auth` in your command prompt to generate an Azure service principal with contributor access to your Azure subscription. The command will output the credentials of the Azure service principal in JSON format. This Azure service principal is needed for the workflow to gain access to your Azure resources and deploy new microservices in the Azure cluster. Add a new secret named `AZ_CREDENTIALS` using the credentials of the Azure service principal in JSON format.

   ![img](https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/docker-kubernetes/images/az_credentials_secret.png)

7. Refer to steps 1 to 5 in the `Configuring a service account and storing its credentials` section of this [tutorial](https://docs.github.com/en/actions/deployment/deploying-to-your-cloud-provider/deploying-to-google-kubernetes-engine#configuring-a-service-account-and-storing-its-credentials) to obtain the service account key for GCP. Store this key value in the new secret `GCP_SA_KEY`.

Ensure that you have set up all 4 secrets as specified above before continuing with the next steps.

### Environment Variable Settings

Here's a list of the environment variables that you need to configure in your `.github/workflows/cicd.yml` file. You should first add the `env` section between the `on` and `job` sections. Then you can define the variables in the `env` section by following this [documentation](https://docs.github.com/en/actions/learn-github-actions/variables#defining-environment-variables-for-a-single-workflow).















| Variable                | Description                                                  |
| :---------------------- | :----------------------------------------------------------- |
| `GCP_CLUSTER_NAME`      | The name of your GCP cluster                                 |
| `GCP_PROJECT_ID`        | The ID of your project in GCP                                |
| `GCP_REGION`            | The zone of your cluster in GCP. (e.g. us-east1-d)           |
| `AZ_CONTAINER_REGISTRY` | The login server name of your Azure Container Registry (ACR) |
| `AZ_CLUSTER_NAME`       | The name of your Azure cluster                               |
| `AZ_RESOURCE_GROUP`     | The resource group of your Azure cluster                     |



## Create the Workflow for GCP

To create the GitHub Actions workflow YAML file on GitHub to define the pipeline for GCP, follow the guideline below. We will first define the workflow file that deploys to GCP. The template code is provided in the `.github/workflows/cicd.yml` file.

### First Job: Generate Strategy Matrix

The prep-matrix job generates the strategy matrix for the downstream job, and has a total of 10 steps. The first step checkouts to your repository so that your job can access it. The second to fifth steps obtain the information of the changed files for re-building/pushing Docker images and the Kubernetes deployment using `actions/changed-files`. The sixth to ninth steps use shell scripts to generate the matrix for future jobs. In both steps, they loop through the list of the changed files to see if there is a match for a specific microservice, then add it to an array as the final result. The matrix for the future steps should be in JSON format when exposed to others so that you can use it using the `fromJSON` command. That's why you need to convert the final result to a JSON string using the `jq` command at the end of the step.

Make the following changes:

- Set the `<output_for_gcp_rebuild>` to be `${{ steps.matrix-docker-gcp.outputs.rebuilds }}`.
- Set the `<output_for_gcp_redeploy>` to be `${{ steps.matrix-k8s-gcp.outputs.redeploys }}`.
- Set the `<output_for_az_rebuild>` to be `${{ steps.matrix-docker-az.outputs.rebuilds }}`.
- Set the `<output_for_az_redeploy>` to be `${{ steps.matrix-k8s-az.outputs.redeploys }}`.

### Second Job: Re-build / Push Docker Images to GCP

The next job, `build-docker-image-gcp`, will re-build and push the Docker images into the GKE cluster using the strategy matrix generated from the `prep-matrix` job.

Make the following changes:

- Set the `<dependent_of_jobs>` to be the ID of the previous job.
- Set the `<outputs_from_previous_job>` matrix to be `${{ fromJSON(needs.prep-matrix.outputs.rebuilds-gcp) }}`.
- Replace `<GCP_PROJECT_ID>`, `<GCP_CLUSTER_NAME>`, and `<GCP_REGION>` to use the environment variables that you defined above. Please refer to this [documentation](https://docs.github.com/en/actions/learn-github-actions/variables#using-the-env-context-to-access-environment-variable-values) to search for example usage of environment variables in Github Actions and figure out how to refer to them.

### Third Job: Deploy Microservices to GCP

You will make modifications to the `deploy-service-gcp` job after the build-docker-image job to enable deployment of microservices to the GKE cluster. Here's how:

- Set the `<dependent_of_jobs>` to be the ID of the first job.
- Set the `<outputs_from_previous_job>` matrix to `${{ fromJSON(needs.prep-matrix.outputs.redeploys-gcp) }}`.
- Replace `<GCP_PROJECT_ID>`, `<GCP_CLUSTER_NAME>`, and `<GCP_REGION>` to use the environment variables.

Similar to the previous job, this job gets the strategy matrix from the prep-matrix job using `fromJSON` and deploys the microservices defined in the matrix to the Kubernetes cluster.

## Create the Workflow for Azure

### Fourth Job: Re-build / Push Docker Images to Azure

Now we will work on the jobs for the Azure workflow. The `build-docker-image-az` job will re-build and push the Docker images into the AKS cluster using the strategy matrix generated from the `prep-matrix` job.

Make the following changes:

- Set the `<dependent_of_jobs>` to be the ID of the first job.
- Set the `<outputs_from_previous_job>` matrix to be `${{ fromJSON(needs.prep-matrix.outputs.rebuilds-az) }}`.
- Replace `<AZ_CONTAINER_REGISTRY>` to use the environment variable.

### Fifth Job: Deploy Microservices to Azure

You will make modifications to the `deploy-service-az` job after the build-docker-image job to enable deployment of microservices to the AKS cluster:

- Set the `<dependent_of_jobs>` to be the ID of the first job.
- Set the `<outputs_from_previous_job>` matrix to `${{ fromJSON(needs.prep-matrix.outputs.redeploys-az) }}`.

This time, you will add the necessary steps to the job by yourself. Similar to the previous `deploy-service-gcp` job, this job should get the strategy matrix from the prep-matrix job using `fromJSON` and deploy the microservices defined in the matrix to the Kubernetes cluster in Azure.

1. The first step should check out to your repository so that your job can access it. This should be identical to the first step of the previous `build-docker-image-az` job.

2. The second step should login to Azure. It should use the `azure/login@v1` action with the `AZ_CREDENTIALS` secret. Please refer to the documentation of the [login action](https://github.com/Azure/login) to check how you can supply the `AZ_CREDENTIALS` secret to the action.

3. The third step should set the cluster context to Azure using the `azure/aks-set-context@v3` action. Please also refer to the documentation of this action to supply the necessary variables. Don't forget to import the variables as the Actions variables instead of hardcoding their values.

4. The last step should deploy the microservices to AKS. You should run the following command:

   ```
   $ helm install ${{ matrix.microservices }} ${{ matrix.microservices }}-service/src/main/helm-multi-cloud/${{ matrix.microservices }}/
   ```

### Trigger the GitHub Actions workflow

Once you have finished the tasks mentioned above, you can commit your changes to the `.github/workflows/cicd.yml` file.

If you make any modifications to your Dockerfiles or helm charts, GitHub Actions will be triggered, and the modified microservices will be rebuilt and redeployed. **Remember to uninstall the services using `helm uninstall <service-name>` before triggering the workflow.**

Try to trigger the workflow to redeploy the profile service by changing the `name` of the `containers` field in the `profile-service/src/main/helm/profile/templates/deployment.yaml` file.

Check that the workflow has successfully finished in the `Actions` tab of your GitHub repository..

### Validate your work

Before you submit, you should check that you have:

1. The latest run of the workflow should have been successful.

2. Appropriate configurations and commands have been added for the tasks in `cicd.yml`.

3. If you have edited your code from the GitHub repository, you should have pulled the changes that you made to the student VM. You can do this by running the below command in your student VM's task7 directory.

   ```
   $ git pull
   ```

### Edit your information for the submitter

In order to ensure your submissions are successful, it's important to include certain meta information. Specifically, there are three values that must be provided within the `meta.json` file:

```
{
    "github_username": "",
    "repository": "",
    "token": ""
}
```

1. For the `github_username` value, enter the GitHub username that you used for this assignment.
2. For the `repository` value, enter the name of your GitHub repository.
3. For the `token` value, create a new personal access token by following the `Set up Fine-Grained Personal Access Token in GitHub` section in the `Intro to GitHub Actions` primer.

### Submissions

The submitter can be found in the `task7` directory. To submit your work, navigate to the root directory and run the command `./submitter`. This will submit your `.github/workflows/cicd.yml` YAML file and the `meta.json` file to the autograder.

Delete Resources

## Delete Cloud Resources

After finishing all the tasks, you need to delete all the resources on Azure and GCP.

- For Azure, delete all the resource groups related to this project in the Azure console or Azure CLI using the command:

  ```
  $ az group delete --name $RESOURCE_GROUP
  ```

- For GCP, delete the cluster using the command:

  ```
  $ gcloud container clusters delete $CLUSTER_NAME --zone=us-east1-d
  ```

- Run the command below in the same directory where you ran `terraform apply` to delete the student VM and the resources deployed by Terraform in task 6:

  ```
  $ terraform destroy
  ```

- After deleting the cluster and the VM in GCP, delete the GCP project in the console or gcloud CLI using the command:

  ```
  $ gcloud projects delete $PROJECT_NAME
  ```