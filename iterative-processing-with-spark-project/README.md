# P4. Iterative Processing with Spark

# Cloud Resource Orchestration

You will deploy an Apache Spark cluster on Azure HDInsight for task 1 and task 2. In this section, you will deploy an Apache Spark cluster, learn how to `ssh` into the master node, and learn to access various UIs of Apache Spark such as YARN, Spark UI and Apache Zeppelin.

**The HDInsight cluster is very expensive**. The cluster provisioned by the provided ARM template costs ~$2.62 per hour. Please plan your budget very carefully. You can use this [pricing calculator](https://azure.microsoft.com/en-us/pricing/calculator/?service=hdinsight) from Azure to find out the pricing of the Azure cluster that you will deploy using the provided ARM template. So, please do not deploy the cluster now if you do not plan to start the subsequent tasks right away.

## Cluster Deployment

Please follow the steps below to deploy the Apache Spark cluster on HDInsight.

1. Make sure you have an active subscription by checking [the Azure portal](https://portal.azure.com/#blade/Microsoft_Azure_Billing/SubscriptionsBlade).

2. You will be provisioning the HDInsight cluster using Terraform **through the [Azure Cloud Shell](https://portal.azure.com/#cloudshell/)**. Refer to the Azure Intro primer for more details on Azure Cloud Shell.

3. If you have multiple subscriptions, make sure you are using the correct subscription for this project.

   ```
   subscription_id="INSERT-YOUR-SUBSCRIPTION-ID"
   az account set --subscription $subscription_id
   ```

4. Registering a resource provider enables your subscription to work with a category of Azure services, for example, `Microsoft.Compute` for virtual machines and `Microsoft.CognitiveServices` for Azure Cognitive Services. By default, many resource providers are automatically registered. However, you may need to manually register some resource providers. The scope for registration is always the subscription.

   To work with Azure HDInsight with a new subscription, you need to first register the following resource provider:

   ```
   az provider register --namespace Microsoft.HDInsight --wait
   ```

   Confirm that you have registered the necessary provider by running:

   ```
   az hdinsight list-usage --location eastus
   # If the available cores (limit) equal or greater than 60, then
   # you should be good to go for the project. 
   # Since this is asynchronous registration, please wait up to 10 minutes to see
   # if the process has been completed.
   ```

5. Create a new folder `spark_project` and download provided terraform template into it. This template creates a cluster with 1 head node and 5 worker nodes, each of the instance type being of type `D12_V2`.

   ```
   mkdir spark_project && cd spark_project
   
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/iterative-processing/project/terraform.tgz && tar -xvzf terraform.tgz
   ```

6. Create a file named `terraform.tfvars` and set the values of the variables defined in `variables.tf`:

   ```
   gateway_password      = "cc15619CMU" # The password for Ambari UI gateway of the Spark cluster
   ssh_password          = "cc15619CMU" # The password for SSH into the Spark cluster
   username              = "azureuser" # The username to SSH into the Spark cluster
   ```

   The configurations here are just for references only, you should define your own username and passwords.

   

   Warning

   For ssh_password: The password must be at least 10 characters in length and must contain at least one digit, one non-alphanumeric character, one upper case letter and one lower case letter. Also, the password should not contain 3 consecutive letters from the username.

   

7. Initialize, validate, and deploy the template using the commands `terraform init`, `terraform validate`, and `terraform apply` respectively.

   

   Information

   If you have previously deployed the resources, you may encounter the following error when deploying again: `Error: A resource with the ID "/subscriptions/xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxx/resourceGroups/spark-resources" already exists - to be managed via Terraform this resource needs to be imported into the State. Please see the resource documentation for "azurerm_resource_group" for more information`. You can try to run `terraform import azurerm_resource_group.spark-rg /subscriptions/xxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxx/resourceGroups/spark-resources` , then `terraform destroy` to clean up the broken resources. Afterwards, run `terraform apply` again.

   

   

   Information

   Cluster provision approximately takes **30 minutes**. You may want to keep reading the write-up at the same time. When the cluster provisioning has succeeded, terraform script will output the HTTP and SSH endpoints.
   If you used `terraform apply` and have been waiting for more than thirty minutes, then there might be some temporary issues with Azure and you may need to destroy the resources and create it again. You can try to run `terraform destroy` to clean up the broken resources and run `terraform apply` again.

   

## Resource Deletion

To clean up the resources, run `terraform destroy` from the same directory where you ran `terraform apply`. If your Azure Cloud Shell timed out, logging back in to Azure and opening Cloud Shell will allow you to access the same directory path. **You should delete your cluster and deploy a new one later whenever you want to take a long break to save costs.**

## SSH into Your Cluster

Before you SSH into your cluster, you need to export cluster_name (HDInsight name), which you can get from the Azure Portal under the resource group named `spark-resources` once it has been provisioned:

```
export cluster_name=cloud-spark-cluster<random-id>
```

You can find out the cluster public DNS for `ssh` in the output of `terraform apply` or via the below command.

```
az hdinsight show --resource-group spark-resources --name "$cluster_name" --query properties.connectivityEndpoints
```

Below is a snippet of the expected output.

```
[
  {
    "location": "cloud-spark-cluster<random-id>-ssh.azurehdinsight.net",
    "name": "SSH",
    "port": 22,
    "protocol": "TCP"
  },
  {
    "location": "cloud-spark-cluster<random-id>.azurehdinsight.net",
    "name": "HTTPS",
    "port": 443,
    "protocol": "TCP"
  }
]
```

## Development Environment Setup

Once the Apache cluster is deployed, you will follow these steps to set up the development environment for this project.

1. `ssh` into the master node (Password is the one in the provision section: `terraform.tfvars`)

   ```
   # ssh <username-from-tfvars>@cloud-spark-cluster<random-id>-ssh.azurehdinsight.net
   ssh "azureuser@${cluster_name}-ssh.azurehdinsight.net"
   ```

   

   Information

   It is expected that you cannot see anything while typing the password.

   

2. Install `maven`.

   ```
   sudo apt update && sudo apt install -y maven
   ```

3. Download the student template for this project

   ```
   wget https://sail.blob.core.windows.net/instance-launcher/f23-p41-template.tgz && tar -xvzf f23-p41-template.tgz
   ```

Now you are all set for the tasks! Please read to the following instructions below and refer back to them as you complete the tasks.



Information

You may encounter the error `WARNING: POSSIBLE DNS SPOOFING DETECTED!` when trying to SSH into the cluster. You can resolve this by deleting the entry starting with `cloud-spark-cluster` from `~/.ssh/known_hosts` and then trying to SSH again.



## Running Long Jobs in a Terminal Multiplexer

In the following tasks, you will use the submitter to run your Spark jobs, which could take a non-trivial amount of time to complete. We recommend that you run the submitter from inside a terminal multiplexer application, such as `tmux` or `screen`. These will preserve your running processes and terminal history on the VM if your terminal session times out.

Here are some essential commands that will get you started with `tmux`.

From the regular terminal:

- `tmux`: start a new session.
- `tmux a`: attach to the last session.

From inside tmux:

- `Ctrl+b d`: detach from the session.
- `Ctrl+b :set mouse on`: enable mouse control. Note that some shells do not support mouse events through ssh.
- `Ctrl+b [`: enter copy mode. If mouse control is not enabled, you can only scroll terminal output using arrow keys in this mode. `Esc` to return to the command line.

Beyond keeping your jobs running, `tmux` has a wealth of useful terminal management features that you can learn more about in [this quick reference](https://tmuxcheatsheet.com/).

## Accessing Various Cluster UIs

To monitor Spark applications on HDInsight with the web UI, visit [the HDInsight page on Azure portal](https://portal.azure.com/#blade/HubsExtension/Resources/resourceType/Microsoft.HDInsight%2Fclusters), click `Cluster dashboard -> Yarn`, and **sign in as admin (not sshuser)** using your password as your input as `clusterLoginPassword` when running the ARM template. You should follow [the Azure docs](https://docs.microsoft.com/en-us/azure/hdinsight/spark/apache-spark-job-debugging#track-an-application-in-the-spark-ui) for the definitive guide.



Information

If you don't see your cluster in the UI, make sure you have the right subscription selected.



------

# Background

If you are still waiting for the cluster to provision, you can either work on counting the vertices as in Figure 1.1 or continue reading the subsequent materials.

## Background: Apache Spark for Social Graph

![Twitter Graph](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/twitter-graph.jpg)**Figure 1.1**: Twitter's network is dominated by a small number of influential people and a large number of silent observers.

Say, you encounter a dataset and some research by Kwak [1], describing the analysis of a network of Twitter users. Some further digging reveals the PageRank algorithm for identifying influential vertices in a network. You downloaded the dataset and attempted to use your MapReduce skills to run PageRank to find the influential vertices to target.

Many network analysis and machine learning algorithms rely on multiple iterations of execution. This is where MapReduce will work poorly. After each iteration of the Map phase and the Reduce phase, MapReduce will write all the data to disks, which results in a significant performance penalty.

Fortunately, Spark is optimized for iterative jobs by storing intermediate results in memory. In this project, you will first use Spark to do data exploratory analysis on a Twitter Social Network dataset. Then, you will implement and run the PageRank algorithm on the Twitter dataset to find the most influential users.

> [1][ Kwak, H., Lee, C., Park, H., & Moon, S. (2010, April). What is Twitter, a social network or a news media?. In Proceedings of the 19th international conference on World wide web (pp. 591-600). ACM](http://law.di.unimi.it/webdata/twitter-2010/)

## Background: General Suggestions for Spark Programming

- Read the Scala, Spark and Zeppelin primers before attempting this project.

- Spark applications can be written in Python, Scala, Java and R. However, we only allow Scala in this project. Besides wanting you to become proficient in Scala, there are a few reasons for this:

  - Spark is written in Scala. Scala has the most comprehensive support and Spark applications written in Scala perfectly reflect the Spark way of thinking. There is a lack of documents and resources for Java Spark APIs which could easily slow down development.
  - Scala is built on a JVM, which means it works with any existing Java libraries. That means code written in Java can run in Scala code and the other way round.
  - Spark also has a Python API. Though Python code is easy to deploy in Spark (e.g., you don't need to worry much about dependencies, jar build, etc), Scala APIs are faster than PySpark, and performance matters in this project.

- Familiarize yourself with basic RDD operations before you start writing code. The [Spark Scala API](https://spark.apache.org/docs/latest/api/scala/scala/index.html) should be your go-to guide.

- RDDs support two types of operations: **transformations**, which create a new RDD from an existing one, and **actions**, which return a value to the driver program after running a computation on the dataset. Do not overuse RDD actions, which are both time-consuming and resource-demanding. For example, many students from previous semesters encountered out-of-memory problems when trying to copy a big RDD to the driver using `someBigRdd.collect()`. You should limit the use of actions unless it is necessary. You can debug your process by adding some actions (e.g. just check the first several rows `someBigRdd.take(5)`), but try to prevent using actions to get the result in the middle of processing stages.

- All transformations in Spark are lazy: they do not compute their results straight-away. Rather, they remember the transformations applied to some base dataset, which are computed when an action requires a result to be returned to the driver program. Through lazy evaluation, Spark can make many optimization decisions after having a chance to look at the DAG in its entirety. This would not be possible if it executed everything as soon as it got it.

- The shuffle operations are expensive as they add a burden on the disk I/O, data serialization, network I/O and memory pressure from garbage collection. For example, `join()` is one of the most expensive operations. You will see how long it takes from the visualization in the Web UI if there are `join()` operations in your application.

- Choose transformation methods carefully. For example, if you want to sort an RDD by value, you can use `sortBy()` instead of inverting the keys and values first to use `sortByKey()`.

- Use `reduceByKey()` instead of `groupByKey()` when the aggregation function is ***commutative and associative\***. Take a simple word count step as an example - the two approaches below have a large difference in performance.

  ```
  rdd.flatMap(x => x.split(" ")).map(x => (x, 1)).reduceByKey(_ + _)
  rdd.flatMap(x => x.split(" ")).map(x => (x, 1)).groupByKey().map(t => (t._1, t._2.sum))
  ```

- `groupByKey()` can cause a huge performance penalty, as all of the data is sent over the network to be collected at the reduce workers. `reduceByKey()` will combine the data at each partition locally first, and only one record per key at each partition will be sent over the network.

- Do not cache your RDDs everywhere. Cache RDDs only when necessary.

- Partitions are basic units of parallelism in Spark. Use repartition when it is necessary. You should be aware of the number of partitions of the RDDs.

Task 1

# Task 1: Data Exploratory Analysis

For this task, you need to find the number of edges and vertices in the Twitter social graph, as well as the number of followers of each user. The edges in the graph are directed, so if there are edges `(u, v)` and `(v, u)`, you should count them as two distinct edges. Also, you will explore two different approaches to working with Spark, running Spark commands progressively with Zeppelin Notebook, and submitting Spark applications.

Please follow these steps to set up the required directory structure for submitting task 1.

1. `cd` into the starter code folder `f23-p41-template` you downloaded and unzipped from `f23-p41-template.tgz`.

2. Download the task 1 submitter:

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/iterative-processing/project/task1-submitter.tgz && tar -xvzf task1-submitter.tgz
   ```

Please work inside the folder you created for Task 1.

## DataSet

The Twitter dataset (see **Table 1**) is provided on the Azure cloud.



| File Name | Location                                                     | Size   |
| :-------- | :----------------------------------------------------------- | :----- |
| Graph     | `wasb://datasets@clouddeveloper.blob.core.windows.net/iterative-processing/Graph` | 10.4GB |



**Table 1:** Dataset for this project

**Figure 3.1** shows the distribution of followers. It is a power tail distribution. Most users have a few followers and a few popular users have thousands of followers. You will explore this in Task 1.

![dist](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/power_tail.png)**Figure 3.1** Distribution of number of followers.

You can use an `wasb://` URI directly to retrieve and load the file. You should use `sc.textFile("<filepath>")` (where sc is the Spark context) API to create a RDD from external files.

```
val graphRDD = sc.textFile("wasb://datasets@clouddeveloper.blob.core.windows.net/iterative-processing/Graph")
```

The graph is stored as an edge list format. This provides the list of source and destination vertices for each edge of the graph. Each node represents a user in the Twitter social network, and an edge represents a “follows” relationship. `(u, v)` means user `u` follows user `v` on Twitter.

Below is a snippet of the dataset. (Note that fields are separated by `\t`).

```
5510    3430913
5510    3506997
5510    4289028
5510    11752536
5510    13433256
5510    14380596
5510    17413259
5510    25643118
5510    26929986
5510    30403395
```

## Part A: Counting via Zeppelin Notebook



Warning

We highly recommend you use Zeppelin to test your Scala code, as it will save a lot of time, even though you may still finish by using other IDEs.



In this task, you will first learn how to create a Zeppelin notebook for Apache Spark, and you will count the number of distinct vertices and distinct edges in the dataset using a Zeppelin notebook.



Warning

Safari is not supported for Zeppelin Notebook on Azure HDInsight. Use a different browser.



1. Visit the endpoint (i.e. `https://cloud-spark-cluster<random-id>.azurehdinsight.net/zeppelin/`) of the Zeppelin Notebook server, and sign in using `admin` as username, and the password you set up when running the provided template.
2. Click `Create new note`, name your notebook, and click `Create`. (see **Figure 3.2** and **Figure 3.3**). Leave the interpreter unchanged.![dist](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/zeppelin1.png)**Figure 3.2** Zeppelin Create new note.

![dist](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/zeppelin2.png)**Figure 3.3** New Notebook.

You will find this interactive approach very handy for iterative data processing.

1. In the Zeppelin notebook you created, you should write Scala code using Spark to count the number of **distinct edges** and **distinct vertices** by running code in cells.



Information

It takes a few dozen minutes to process the computation request.



For grading:

1. Copy the code you wrote in the Zeppelin Notebook and put it in `VerticesEdgesCounts.scala` - a class in the template code.

2. Copy and paste the count results you got from the Zeppelin Notebook cell output to the file named `answer`. The file should be in the following format:

   ```
   num_vertices=<number>
   num_edges=<number>
   ```

## Part B: Top 100 “Popular” Users via RDD and Dataframe

In this part, you will find the top 100 “popular” users using **two** different APIs, RDDs and DataFrames. “Popular” is measured by the number of followers. So, you need to count the number of followers for each user, **sort** by the number of followers, and take the top 100.

Note that the submitter will run the Spark applications and grade the output directly.



Information

DataFrame uses Catalyst to optimize code. Hence, DataFrame and DataSet programs with Catalyst will typically run faster than RDDs. In this task, because loading a text file into a DataFrame needs more time than into RDDs, you may not see a big difference in performance between these two APIs. According to our test, DataFrame can be 2-10 times faster than RDDs if counting out the loading time. You can use System.nanotime to print out the program execution time to see how fast DataFrame is.



You can also refer to the Apache Spark primer that has an experiment that compares the performance of RDD vs DataFrame.



### Project Structure

At this step, your project structure for Task 1 should look like this. Note that the `answer` file should have been created in part A.

```
f23-p41-template
|-- answer
|-- pom.xml
|-- references
|-- src
|   |-- main
|   |   |-- scala
|   |       |-- FollowerDF.scala
|   |       |-- FollowerRDD.scala
|   |       |-- PageRank.scala
|   |       |-- SparkUtils.scala
|   |       |-- VerticesEdgesCounts.scala
|   |-- test
|   |   |--scala
|   |   |  |-- LocalSparkSession.scala
|   |   |  |-- PageRankTest.scala
|   |   |  |-- TestingUtil.scala
|-- submitter-task1
```

### Requirements

In part B, you should write 2 Scala programs, one using the Spark RDD API (`FollowerRDD.scala`), and the other using the Spark DataFrame API (`FollowerDF.scala`). For the program using the Spark RDD API, each line of the output should be in plain text format and lines should have the following format.

```
[User_id]\t[num_followers]
```

The output should be saved via `top100RDD.saveAsTextFile(outputPath)`.

For the program using Spark DataFrame API, the output should be saved as a parquet file via `df.write.parquet(outputPath)`.



Information

The template contains a module `SparkUtils.scala` which can be used to create a SparkSession object.





Warning



1. For the RDD part, at some point you might have to pull an RDD to the master via an RDD action. This is acceptable (no grading penalties) though there exists a clever trick.
2. For the **DataFrame part**, you must read the input using the `read` method of the `SparkSession` class.
3. You can either transform the data using methods of the DataFrame class, such as `groupBy`, `count` `limit`, and `orderBy`, or creating a view of the DataFrame via `createOrReplaceTempView` and then writing SQL queries against the view.
4. **For the DataFrame part, you should not pull the DataFrame to the master node at any step**.
5. We will manually grade that each task is done with the appropriate API.







Information

Feel free to develop your solution in a Zeppelin notebook first.



You can use `df.show()` to print a DataFrame in a Zeppelin notebook. It will show the output in tabular format like below.

```
 +--------+-----+
 |followee|count|
 +--------+-----+
 |21513299|   27|
 |23934131|   18|
 |23933986|   15|
 |23934048|   15|
 |21448831|   14|
 |23933989|   12|
 +--------+-----+
```





## A Summary of Steps to Complete for Task 1

1. Use Zeppelin Notebook to count the number of distinct vertices and the number distinct edges, copy your code to `VerticesEdgesCounts.scala`, and the answers to a file called `answer`.

2. Implement the RDD program to get the top 100 vertices (in the format specified above).

3. Implement the DataFrame program that loads the data as a DataFrame and saves the top 100 records of this DataFrame as a parquet file.

   

   Information

   In step 2 and 3, you do not need to merge the output files on your program, the submitter will take care of that.

   

   

   Information

   You can use `mvn clean package -DskipTests` to build your code for Task1. Since the provided template contains unit tests for PageRank, you should skip them for Task1.

   

4. Modify the references file and note down all the help you got from the Internet and other students.

5. Once you are confident about your code run the following:

   ```
   $ export SUBMISSION_USERNAME=your_submission_username
   $ export SUBMISSION_PASSWORD=your_submission_password
   $ chmod +x submitter-task1
   $ ./submitter-task1
   ```



Warning

The submitter could take a non-trivial amount of time to complete. We recommend that you use a terminal multiplexer such as `tmux` or `screen` to keep your terminal session alive. Please refer to “Running Long Jobs in a Terminal Multiplexer” in the Resource Orchestration section for more information on how to use these.

Warning



Note that the job could very well take anywhere between 5-20 minutes (or more) depending on your implementation. Be patient.





Task 2

# Task 2: Twitter User PageRank

In this task, you will implement an iterative computation on the Twitter social graph to rank each user by their influence. The problem is to find the most influential or important vertices. Given a graph, which vertex is more “important”?

![PageRank](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/toy_graph.jpg)

**Figure 2.1**: Toy graph for PageRank calculation.

## The PageRank Algorithm

Influential vertices are those vertices that are followed by the most number of other people/vertices. Additionally, if the followers of a vertex are influential themselves, then the followed vertex becomes even more influential. For example, if A has 10 followers and B has 5 followers, then A is supposed to be more influential than B. If C also has 10 followers, then A and C are equally influential. However, if the followers of C are more influential than the followers of A (i.e., if the followers of C have more followers of their own than the followers of A), then C becomes more influential than A (simply because it is followed by more influential people).

We can solve the problem of measuring the influence of vertices quantitatively by using the PageRank algorithm. PageRank is a type of random walk algorithms. Imagine there is an agent walking on a graph. The agent can randomly jump from one vertex to another vertex over the edges in the graph. As the surfer proceeds in this random walk from vertex to vertex, he visits some vertices more often than others; intuitively, these are vertices with many links coming in from other frequently visited vertices. The idea behind PageRank is that pages visited more often in this walk are more important. Calculating the influence score for any given vertex involves transferring/propagating the influence scores of its followers to it.

The problem can be formulated in a mathematical way. Given a graph *G* of *n* vertices, we can represent the graph by the adjacent matrix, where if there is an edge from vertex *vi* to vertex *vj* we have *Gij = 1*, otherwise, we set *Gij = 0*. We define a transition matrix *M*, where *Mij* is the transition probability of jumping from vertex *vi* to vertex *vj*. Because the probability sums to *1*, we need to normalize *M* so that each row of *M* sums to 1.

So we have

![formula](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/task2-eq1.png)

Here *M* is the transition matrix, *X* is the probability of the agent being on each vertex, which may stand for the ranks.

Then the problem of figuring out the rank of each vertex can be broken down mathematically into the following equation:

![formula](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/task2-eq2.png)

Before using *X* as the result of vertex influence by solving the equation above, consider the scenario where we have only 2 vertices, A and B. And in such a graph, there is only one edge which is from A to B. In this case, the solution (*X*) to the equation above is a zero matrix. Make the problem more general, what if all elements of the *i*th row of *M* are *0*, which means that there are no edges from vertex *vi* to any other vertices in the graph? In this case, to ensure the row sum in matrix *M* is *1*, we can add a pseudo edge to all vertices (including the vertex itself) in the graph, which means the score of this vertex *vi* should be propagated equally to all vertices in the graph. So we might set

![formula](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/task2-eq3.png)Where *n* is the number of vertices in *G*, *ri* is the score for the vertex *vi*.

In this case, the algorithm is able to find the score for each vertex iteratively. When the score of every vertex does not change across iterations, we say the algorithm **converges**. When it converges, the final score of each vertex represents the probability of being visited. Therefore, **the bigger the score is, the more influential the vertex is**.

For this task, we will use the following algorithm to update the rank of a vertex in a graph:

![formula](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/formula.png)

**Figure 2.2**: PageRank Expression

If a vertex has no edges to other vertices, it becomes a sink and therefore terminates the random surfing process. The damping factor *d* in the formula above is used to prevent sinks (users who do not follow anyone) from "absorbing" the PageRanks of those users connected to the sinks. It is easy to see that an infinite surfer would have to end up in a sink given enough time, so the damping factor allows a heuristic to offset the importance of those sinks.

In this task, you will implement the PageRank algorithm to find influential users. Also, you will tune your Spark program to meet our performance tuning objectives.



Danger

We assign grades for both the correctness of your PageRank algorithm and the efficiency of your PageRank algorithm. Though it is okay to first implement a correct version, and then iteratively improve its performance, we highly encourage you to keep performance tuning in mind when working on your first PageRank implementation. A slow implementation will take a long time to run, scores 0 in performance, and can possibly leave you with insufficient Azure budget for performance tuning experiments.





Information

You may spend a significant amount of time debugging your PageRank algorithm for correctness on small test cases. Keeping in mind how expensive the HDInsight cluster is (~$2.62 per hour), it may be worth the budget save to start development in local Zeppelin Docker containers. Instructions for how to set this up can be found in the Zeppelin for Apache Spark primer.



## PageRank Correctness Rules

- **Rank initializations**. The initial value of the rank of each user should be `1/N` (N is the number of total vertices in the graph). This value needs to be assigned to every vertex, so it's easy to think of this as being a `map` operation.

- **Damping Factor**. There are many possible values for the damping factor, and in this task we set it to `0.85`. In other words, `d=0.85`.

- **Output Format**. The final output format is a plain text file where each line follows the format:

  ```
  [user_id]\t[pagerank_value]
  ```

- **Number of Iterations**. You must run **10** iterations of PageRank.

- **Dangling Users**. You need to handle the case of dangling vertices (vertices with zero out-degrees). The weight of the dangling users must be redistributed across all the users during each iteration (see Figure 2.2). **Remember, the sum of all PageRank scores should always be 1 in each iteration.**

### Examples



Information


Many prior students expressed that this example (especially the dangling nodes part) helped a lot in the task. You may want to visit this example multiple times until you fully understand the algorithm.



Consider the following example. You are given the following graph of people:

```
key: user1 rank: 1/3 = 0.3333 follows: user2 user3 
key: user2 rank: 1/3 = 0.3333 follows: user3 user1
key: user3 rank: 1/3 = 0.3333 follows:
```

After 1 iteration, the following contributions will be received by each user, as shown:

```
key: user1 contributions received: (0.3333)/2 = 0.1667 follows: user2 user3 
key: user2 contributions received: (0.3333)/2 = 0.1667 follows: user3 user1 
key: user3 contributions received: (0.3333)/2 +(0.3333)/2 = 0.3333 follows:
```

User3 is a dangling user. Dangling users are users whose follow information is unavailable. Unfortunately, the total aggregate of all rank values should be a constant (as per the formal definition of PageRank). However, dangling users do not emit any weight and hence, the system tends to lose weight at each iteration. The way to correct this is by redistributing the weight of dangling users across all the users at each iteration. In this example, there is only one dangling user (user3). Hence, its weight (0.3333, which is the initial value) should be distributed equally among user1, user2, user3. Hence, the new ranks are:

```
user1 = 0.1667 + 0.3333/3 = 0.2778
user2 = 0.1667 + 0.3333/3 = 0.2778
user3 = 0.3333 + 0.3333/3 = 0.4444
```

Considering the damping factor, the ranks after one iteration are:

```
user1 = 0.15 / 3 + 0.85 * (0.1667 + 0.3333/3) = 0.2861
user2 = 0.15 / 3 + 0.85 * (0.1667 + 0.3333/3) = 0.2861
user3 = 0.15 / 3 + 0.85 * (0.3333 + 0.3333/3) = 0.4278
```

![example](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/example.png)

**Figure 2.3**: One Single Iteration



Information



### PageRank Correctness Hints

- Unit tests for PageRank are provided. You can run the provided unit tests via `mvn test`. We strongly recommend adding more test cases and developing your page rank application locally or on a VM in order to save cost by avoiding a cluster running all the time. You will not be graded on the quality of tests you develop.
- Note that you **do not have to manually round the score** to have an exact match with the reference answer. Since both the local test and the secret test tolerate slight deviations.
- Spark offers official example programs which include the implementation of PageRank **without handling dangling users**. Please refer to the [Scala](https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/SparkPageRank.scala) implementation as the starter code. You are allowed to use and modify these official examples without citation.
- We also provide a [Jupyter notebook](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/notebooks/PageRank_Examples.ipynb) which implements a simple matrix-based PageRank example. You can run the example either on your computer or in HDInisight's Zeppelin UI.



## PageRank Performance Objectives

The efficiency of your PageRank algorithm is measured by the run time of your Spark job. Your goal is to develop a correct implementation of PageRank that runs as fast as possible.



Information

You will achieve a score of zero in performance if your PageRank implementation does not achieve the full correctness score.



**Table 4.1** explains the performance score. Our target runtime is 30 minutes. You will get the full performance score 30, if your PageRank runs within 30 minutes.



| Runtime t (in minutes)       | Score  |
| :--------------------------- | :----- |
| t <= 30 minutes              | 30     |
| 30 minutes < t <= 60 minutes | 60 - t |
| t > 60 minutes               | 0      |

**Table 4.1** Performance Rubrics.



We provide a list of performance optimization hints. The hints are not ordered by effectiveness or importance. But certain hints describe pitfalls which can cause significant inefficiency in your implementation. These hints have asterisks (*) next to them, and not implementing these hints can lead to failure to pass the performance optimization objectives.

The other hints require explorations. With careful tuning, they might improve your current implementation of PageRank substantially. On the other hand, you might not be able to improve your runtime with some of these hints or even increase your runtime. You may wish to experiment with these hints with multiple configurations to verify your hypothesis.

Lastly, these hints are by no means exhaustive and you may explore other performance optimization strategies.



Information

There are multiple correct implementations of PageRank, and you are free to use either RDD or DF API to implement the algorithm. Also, there are multiple implementations that pass the performance objectives. All hints we show here are valid Spark performance optimization strategies tailored to PageRank and we do not try to trick you in any of the hints, but their effectiveness depends on your approach to solve PageRank.





Information



## PageRank Optimization Hints

### Spark Operation Performance

- (*) Carefully read the General Suggestions section first. It contains a lot of useful information about the performance of specific Spark operations.
- (*) Parse numeric data from `String` to numeric. Twitter ID is one such example. When Spark shuffles an RDD during `join`, `cogroup` or `byKey` operations such as `reduceByKey`, Spark partitions data into buckets. By default, Spark uses a `HashPartitioner` so that keys with the same hash value are shuffled to the same partition. Since it is significantly more expensive to hash a `String` than to hash a numeric, parsing twitter IDs into a numeric, e.g., `Long`, can improve shuffle efficiency.

### Optimizing Data Storage

- (*) Use `cache()` or `persist()` thoughtfully. RDDs are lazily evaluated and are NOT data per se. Spark defers evaluation of RDDs to when an action is called. The evaluation may consist of a series of transformations. To create a new RDD from an existing RDD, the new RDD carries a pointer to the parent RDD. The dependency graph of all the parent RDDs of an RDD is called an RDD lineage. When an RDD action is called, the action triggers the Spark scheduler to build a directed acyclic graph (DAG) based on RDD transformations and Spark evaluates the RDD based on the DAG. When another action is called on the RDD, Spark has to re-evaluate the RDD from scratch based on the DAG unless the RDD has been materialized via `cache()` or `persist()`. On the other hand, you probably should not call `cache()` if there is only one action of your Spark job. Blindly overusing `cache()` or `persist()` increases memory pressure and slows down the job. You should NOT cache the following job, where the RDD lineage is linear with no branches.

  ```
  val textFile = sc.textFile("...") 
  val numberRDD = textFile.flatMap(line => line.split("\t")) 
  numberRDD.cache() # this is a bad example, you should NOT cache the RDD
  val numberCount = rdd.count()
  ```

  The following job will benefit from caching, where the RDD lineage branches out.

  ```
  val textFile = sc.textFile("...") 
  val numberRDD = textFile.flatMap(line => line.split("\t")) 
  numberRDD.cache() # this is a good example
  val positiveCount = numberRDD.filter(number => number > 0).count() 
  val negativeCount = numberRDD.filter(number => number < 0).count()
  ```

- (*) Reduce network shuffle. Network shuffle refers to sending data over the network. Shuffling a large volume of data can be expensive. Shuffles are triggered by `join`, `cogroup` or `byKey` operations (hence an obvious way to reduce shuffle is to minimize the use of these operations). For example, in the `rddZ = rddX.join(rddY)` operation, the default behavior is to hash `rddX` and `rddY` into buckets (partitions) using the same hash function so that tuples in `rddX` and `rddY` with the same key is hashed into the same bucket. However, the shuffle can cause substantial network IO and thus impact your code run time. The shuffle can be avoided if both rddX and rddY have the same key and the same `partitioner`. But how to modify the partitioner of an RDD? Spark provides the `partitioner` accessor function that returns the current partitioner of an RDD, and you can reset the partition of an RDD using the `repartition` transformation. For example, the following code snippet repartitions `rddY` using `rddX`’s partitioner, and thus `rddX.join(rddY)` will not induce a shuffle. The shuffle takes place at `rddY.partitionBy(rddX.partitioner.get)` instead.

  ```
  rddY.partitionBy(rddX.partitioner.get)
  val rddZ = rddX.join(rddY)
  ```

  Note that the above code snippet also ensures `rddZ` has the same partitioner as `rddX`. In most Spark functions that require a shuffle, you can pass a partitioner so that the shuffle will take place according to your partitioner instead of a default `HashPartitoner`. The code snippet below has the same effect as the previous code snippet.

  ```
  val rddZ = rddX.join(rddY, rddX.partitioner.get)
  ```

### Maximizing Compute Usage

- (*) Set spark configurations for your Spark job. You might want to understand the meaning of parameters such as spark.driver.memory, spark.executor.memory, and spark.executor.cores. The Spark-submit Optimization Hints later in this block go over some essential parameters, but check [Spark Configuration](https://spark.apache.org/docs/latest/configuration.html) to learn more about how to configure your Spark cluster to achieve better performance. Generally speaking, the configuration should be set in `run.sh` as command arguments to `spark-submit` instead of programmatically using the `SparkConf` object. Some properties may not be affected when set programmatically through `SparkConf` in runtime.

- (*) Make sure you know how to access the various cluster UIs, especially the YARN UI. Refer to the Cloud Resource Orchestration in the Azure Resource Manager section.

  When you are running a Spark job, the YARN UI view of a running Spark job should look something like this.

  ![YARN UI 1](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/p41-yarn1.png)**Figure 1.1**: YARN UI. Observe the `% of Cluster` column in the YARN UI, which indicates the percentage of the cluster being utilized for this task. You should figure out how to maximize this percentage in order to fully utilize your cluster’s resources.

  Clicking the `ApplicationMaster` will take you to the Spark UI where you can observe the progress of the Spark jobs and tasks. **Please refer to the Spark primer for more info about interpreting the Spark UI.**

  An example of the Spark UI while running the page rank application.

  ![YARN UI 1](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/p41-yarn2.png)**Figure 1.2**: Spark UI.

### Streamlining Program Logic

- (*) The distinct function running on repeated edges takes a lot of time to process. If you are getting correctness without accounting for repeated edges, then your solution is fine.
- (*) The sum of all page ranks is 1 since page rank is a probability. Dangling users are sinks in a random walk process. If you know the total page ranks sum to \alpha without dangling user contributions, then, the sum of dangling user contributions should be (1 - \alpha). It is the sum of dangling user weight that matters, instead of the breakdown per dangling user.

### Spark-submit Optimization Hints



Warning



This section is critical to the performance of your Spark job, so please read it carefully.





You will need to run `spark-submit` (configured in `run.sh`) after you design the PageRank algorithm. You need to configure the parameters of `spark-submit` to make the most of the computing power of your cluster. If you properly configure the parameters, then the performance of your Spark job may be improved dramatically.

Here are hints for several critical parameters, but you can also try other parameters on the [official documentation](https://spark.apache.org/docs/latest/configuration.html).

#### executor-memory

The parameter to configure the memory size for each executor. You need to make sure `executor-memory * num-executors` is less than the total memory of your cluster.

#### executor-cores

The parameter to configure the CPU core number for each executor. This depends on the configuration of the nodes in your cluster.

#### num-executors

The parameter to configure how many executors you want for the Spark task. If the parameter is not configured, then the number of executors may be too small by default. You need to make sure `executor-cores * num-executors` should not exceed the total CPU core numbers in your cluster.

#### spark.driver.memory

The parameter to configure how much memory you want to assign to the driver. If you need to pull the data (e.g. `bigRDD.collect()`) to driver, you need to make sure the driver memory is able to store the entire RDD in memory.

#### spark.default.parallelism

The parameter to configure the default task number of each stage. If you do not configure parallelism, then the default value will be the number of HDFS block, which may be much smaller than your executor numbers. In such conditions, most of the executors could be starving without available tasks. It is recommended to configure the parallelism as a factor of `num-executors * executor-cores`.





## Summary of Steps to Complete Task 2

1. Develop a Spark application that computes the PageRank value for each vertex in the Twitter social graph in `PageRank.scala` in the provided code template. Your program should follow the implementation rules described above and produce the following output for the entire graph by running 10 iterations of the computation.

2. The template code provides a sample `run.sh` script, which should be used to execute your page rank application. The submitter will run this `run.sh` to submit your program. You *need* to uncomment one or more lines in `run.sh` and add/modify your own parameters.

   

   Warning

   All Spark configuration parameters like `spark.driver.memory` MUST be specified in the `run.sh` rather than in your source code. Settings these parameters through source code is a bad practice, as all the parameters might not be reflected on the cluster.

   

3. Download the submitter on the master vertex of your Spark cluster.

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/iterative-processing/project/task2-submitter.tgz && tar -xvzf task2-submitter.tgz
   ```

   

   Information

   You do not need to merge the output files on your program, the submitter will take care of that.

   

4. Once you are confident about your code, you can run:

   ```
   $ export SUBMISSION_USERNAME=your_submission_username
   $ export SUBMISSION_PASSWORD=your_submission_password
   $ chmod +x submitter-task2
   $ ./submitter-task2
   ```

5. Remember to terminate the HDInsight cluster after completing this task.



Danger

The submission will take at least 30 minutes to run. Please make sure you submit with enough time before the deadline for the job to finish running or your score will not be saved.



Bonus Task

## Bonus Task: PageRank on Azure Databricks

In this task, you will run the same PageRank application on [Azure Databricks](https://docs.microsoft.com/en-us/azure/azure-databricks/) to compare the differences between this new service and Azure HDInsight.

Azure Databricks is a combination of the Apache Spark analytics platform and the Azure cloud. Apache Spark is a powerful platform to run analytics algorithms at scale and in real-time to drive business insights in recent years. However, managing and deploying Spark at scale is a challenging problem, especially for enterprise use cases with large numbers of users and strong security requirements.

Founded by the original creators of Apache Spark, [Databricks](https://databricks.com/) provides an end-to-end, managed Apache Spark platform optimized for the cloud. Featuring one-click deployment, autoscaling, and an optimized Databricks Runtime that can improve the performance of Spark jobs in the cloud by 10-100x, Databricks makes it simple and cost-efficient to run large-scale Spark workloads. Moreover, Databricks includes an interactive notebook environment, monitoring tools, and security controls that make it easy to leverage Spark in enterprises with thousands of users.

Azure Databricks is one step beyond the base Databricks platform by integrating closely with Azure services through collaboration between Databricks and Microsoft. Azure Databricks features optimized connectors to Azure storage platforms (e.g., Data Lake and Blob Storage) for fast data access, and one-click management directly from the Azure console.

Azure Databricks differs from HDInsight in that HDInsight is a PaaS-like experience that allows working with many tools at a reduced cost. Databricks is a Software-as-a-Service-like experience (or Spark-as-a-Service) that is easier to use, has native Azure AD integration (HDInsight security is via Apache Ranger and is Kerberos based), has auto-scaling and auto-termination (like a pause/resume), has a workflow scheduler, allows for real-time workspace collaboration, and has performance improvements over vanilla Apache Spark.



Information

Your solution must achieve 100% correctness and run within 30 minutes (on Databricks) in order to receive any marks for this bonus task.



## Steps to Complete the Bonus Task

1. Create an Azure Databricks workspace. Log in to the [Azure portal](https://portal.azure.com/). Select **+ Create a resource** > **Analytics** > **Azure Databricks**. After providing the following workspace configuration, and tagging the resource with the appropriate tags, click **Create**. The portal should display “Deployment in progress”.

   

   

   

   

   

   

   

   

   

   

   

   

   | Workspace Name | Enter a name for your Azure Databricks workspace.    |
   | -------------- | ---------------------------------------------------- |
   | Subscription   | Select your Azure subscription.                      |
   | Resource Group | Create a new resource group named ‘*databricks-rg*’. |
   | Location       | East US                                              |
   | Pricing Tier   | Trial (Premium - 14-Days Free DBUs)                  |

   

   ![img](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/databricks-create-workspace.png)

2. **After a few minutes** the Azure Databricks Service page displays.

3. Select your Databricks workspace and click Launch Workspace.

4. Generate a [personal access token](https://docs.databricks.com/dev-tools/api/latest/authentication.html) for accessing Azure Databricks resources using the Databricks CLI.

   1. Click the **drop down** containing your account information in the upper right corner of your Azure Databricks workspace.
   2. Click **User Settings**.
   3. Go to the **Developer** section on the sidebar.
   4. Click **Manage** in the **Access Tokens** section.
   5. Click the **Generate New Token** button.
   6. Optionally enter a description (comment) and expiration period.

5. On Azure cloud shell, Generate a SSH pair using the following command:

   ```
   az sshkey create --location "eastus" --resource-group "databricks-rg" --name "databricks-rg-ssh-key"
   ```

   After it is created, the output of the above command is of the following format

   ```
   No public key is provided. A key pair is being generated for you.
   Private key is saved to "/Users/xxx/.ssh/1664046703_009666".
   Public key is saved to "/Users/xxx/.ssh/1664046703_009666.pub"
   ```

   You must note down the **Private key file path**, because you will need that to connect to your VM.

   Change the permission of the private key file

   ```
   chmod 400 <private key file path>
   ```

6. Create a new VM in Azure using the following command:

   ```
   az vm create  --resource-group databricks-rg --name SubmitterVM --image Ubuntu2204 --admin-username azureuser --ssh-key-name "databricks-rg-ssh-key" --tags project=iterative-processing
   ```

   After the Databricks VM is successfully created, the output of the above command is of the following format

   ```
   {
   ...
   "location": "eastus",
   "powerState": "VM running",
   "privateIpAddress": "10.0.0.4",
   "publicIpAddress": "xx.xx.xx.xxx",
   "resourceGroup": "databricks-rg",
   ...
   }
   ```

   Use the `publicIpAddress` field to retrieve the public IP of your VM and SSH into it as `ssh -i /Users/xxx/.ssh/<private key> azureuser@<publicIpAddress>`.



Information



1. If you get `Permission denied (publickey)` issue when you are trying to SSH to the Submitter VM, then make sure:

   1. The SSH key is created: `az sshkey list`
   2. Correct username: **azureuser** (not root or anything else).
   3. Fetch the latest public IP address of you VM.

   

   If you want to manually set the location of **id_rsa**, you can use `ssh -i <PATH_TO_id_rsa> azureuser@<VM Public IP Address>`. For example, `ssh -i ~/.ssh/id_rsa azureuser@1.2.3.4`.

2. If you get `Permissions 0777 for 'id_rsa' are too open` issue, then you can assign new permissions by `chmod 600 <PATH_TO_id_rsa>`. For example, `chmod 600 ~/.ssh/id_rsa`.

3. For PuTTY users, you can import **private key file** into PuTTY Key Generator (PuTTYgen) and click "Save private key" to export **.ppk** key format for PuTTY connections.







1. SSH into the created Databricks VM and set up the [Databricks CLI](https://docs.databricks.com/dev-tools/cli/index.html).

   1. Install virtualenv and jq:

      ```
      sudo apt update && sudo apt install -y virtualenv jq
      ```

   2. Create a Python virtual environment in which you can install Databricks CLI:

      ```
      virtualenv -p /usr/bin/python3 databrickscli
      ```

   3. Switch to the virtualenv and install Databricks CLI:

      ```
      source databrickscli/bin/activate && pip install databricks-cli
      ```

   4. Establish authentication to connect to the Databricks workspace using the token you’ve downloaded before:

      ```
      databricks configure --token
      ```

      Enter `https://eastus.azuredatabricks.net` for the Databricks Host, and then the token that you’ve saved from the previous step. Note that you need to paste the token and hit enter. The value will not be displayed.

   5. To verify that the setup is correct, you can try a simple command like:

      ```
      databricks workspace list
      ```

2. Go to the Databricks workspace and create a notebook named “pagerank” at the **root** directory.

   Warning

   Please **make sure** that there is a notebook called pagerank at the **root** directory of the workspace. You can check this by running `databricks workspace ls` on Databricks CLI, whose output should contain the pagerank notebook.

   

   - In the sidebar, click the **Workspace** button.
   - The default directory will be a user directory. Navigate to the **Workspace** directory using the file navigation on the left.
   - In the Workspace folder, select the **drop-down** icon > **Add** > **Notebook**.
   - A new notebook will be created with default settings. Change the name to `pagerank` and select **Scala** in the Language drop-down.
   - Please do not use a different name for the notebook as the submitter looks specifically for the notebook named `pagerank` at the root directory.
   - You can verify this step by running `databricks workspace list`. The result should contain `pagerank`.

   ![img](https://clouddeveloper.blob.core.windows.net/assets/iterative-processing/project/images/databricks-create-notebook.png)

3. Insert your page rank code for task 2 into the “pagerank” notebook:

   - Your code **MUST** read the graph from `wasb://datasets@clouddeveloper.blob.core.windows.net/iterative-processing/Graph` - you should hardcode that value when reading the graph.
   - Your code **MUST** write the output to `dbfs:/pagerank-output` - you should hardcode that value when saving the result of your PageRank.
   - Databricks provides SparkContext and SparkSession objects for the notebook. Hence, you MUST remove parts of your code creating or destroying these objects. You can directly refer to the SparkContext object as `sc` and SparkSession object as `spark`.
   - Remember that Databricks notebook is an interactive notebook like Jupyter/Zeppelin. Hence, you should modify your code into a sequence of commands rather than expecting the main function to be called.

4. In the Databricks VM, download the databricks setup scripts:

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/iterative-processing/project/databricks-setup.tgz && tar -xvzf databricks-setup.tgz
   ```

5. Run `databricks-setup.sh` to create a cluster and a [job](https://docs.databricks.com/workflows/jobs/jobs.html) in Databricks:

   ```
   chmod +x databricks-setup.sh
   ./databricks-setup.sh
   ```

   This will create a cluster with the same configuration as that of task2 i.e., 1 master and 5 workers of type D12_v2.



Information

It takes around **8-10 minutes** to create the cluster and the job. Please be patient.



Running the above script outputs the cluster ID and job ID. You’ll be needing the job ID for submitting your task.

\12. Upgrade the databricks CLI version to 2.1 using the following command:



```
databricks jobs configure --version=2.1
```

1. Attach the pagerank notebook to the cluster.

2. Download the submitter.

   ```
   wget https://clouddeveloper.blob.core.windows.net/f23-cloud-developer/iterative-processing/project/task3-submitter.tgz && tar -xvzf task3-submitter.tgz
   ```

3. The submitter will run the job and look for output in `dbfs:/pagerank-output`. Again, do not merge and sort your output files on your own, the submitter will take care of that.



Warning



Please **make sure** that there is a notebook called pagerank at the **root** directory of the workspace. You can check this by running `databricks workspace ls` on Databricks CLI, whose output should contain the pagerank notebook.

Refer to the image above.





1. Modify the references file and note down all the help you got from the Internet and other students.

2. Once you are confident about your code, you can run:

   

   Danger

   Please ensure you read from the **correct input** and output to the **correct place with the correct filename**.

   Information

   You must re-enter a `job ID` (which will display on the terminal) when the submitter requires you to do so.

   

   ```
   $ export SUBMISSION_USERNAME=your_submission_username
   $ export SUBMISSION_PASSWORD=your_submission_password
   $ chmod +x submitter-task3
   $ ./submitter-task3
   ```

3. You will be prompted to enter the job id. The job id will be printed when you execute the submitter. Copy and paste the job id and click enter.

4. After a successful submission, delete the resources of this task by running `az group delete --name "databricks-rg"`.



Danger

Remember to terminate **ALL** the active resources after the completion of the project. Run `terraform destroy`, and double-check the Azure console to verify that you have no active resources after completing the project.