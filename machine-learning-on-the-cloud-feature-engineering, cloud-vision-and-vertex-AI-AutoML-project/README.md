# P6. Machine Learning on the Cloud

## Introduction

The availability of large-scale computing capacity has accelerated the efficacy of Machine Learning (ML). The next major disruption to how humans live and work will be largely due to AI and Machine Learning. The cloud’s computational power and ease of use continue to enable the ongoing progression and adoption of machine learning in a wide variety of domains. In this project, you will explore some of the aspects of using the cloud to build and deploy ML models. Moreover, you will deploy a complex pipeline of ML models to construct an end-to-end solution.

There are different types of ML algorithms, unsupervised, supervised and reinforcement learning. In supervised learning, we construct a model from labeled data. In unsupervised learning, we find hidden patterns in unlabeled data. In reinforcement learning, we build an agent to achieve a desired goal through trial and error by applying rewards and penalties.

As described in the Introduction to Feature Engineering primer, feature engineering plays an important role in selecting discriminating features in the data set to improve the accuracy of the ML model through training. Hence, developing sound and versatile feature engineering skills is necessary to build effective models.

The cloud service providers (CSPs) are rapidly innovating to simplify the process to train, deploy and update ML models on the cloud. Many of the innovations target non-experts in order to widen the reach of ML. In this project, we will experiment with one such cloud service and utilize an important feature, hyperparameter tuning, to improve the accuracy of our ML predictor model.

Furthermore, a variety of ML models can be stitched together in order to build a complex end-to-end solution. We will build such a complex solution and attempt to extend its functionality.

We are at the infancy of this rapidly evolving domain. We expect to continue to experience accelerated innovation and adoption. As a cloud computing student, you have the opportunity to play an important role in both the innovation and adoption of ML on the cloud.

## Scenario

You have been hired by a company called KarPhare which owns an application that allows passengers to hail a ride and drivers to charge fares and get paid. In recent years, a large number of cab companies have come up that deliver services to a large number of customers daily. Therefore, it becomes really critical for the company to manage data properly and estimate the fare prices accurately.

![img](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/fare_prediction_app.jpg)**Figure 1**: Fare prediction application.

Your task is to help the company build a car fare prediction application that involves building a modular system through a specific set of steps. This process involves four major tasks:

1. In the first task, you will explore and visualize the data set so that you can construct effective features through the feature engineering process.
2. In the second task, you are expected to train an ML model using a cloud-based training and tuning solution.
3. In the third task, you will build an end-to-end application that accepts speech queries about the fare of car rides and you will respond with a speech-based answer. In this task, you will have to utilize multiple APIs to build this application including the ML model you built in the second task.
4. (Bonus task) In the fourth task, you will be expanding your `farePrediction` API to support accepting images of landmarks as the trip pickup and drop off locations.

Feature Engineering

## Feature Engineering Task

### Description

In this task, you will complete two critical steps as prerequisites to build a well-performing model to predict cab fare prices in New York City (NYC). First, you will explore a dataset, visualize existing features to understand trends, outliers, and patterns in data, and then clean outliers or missing values from the dataset according to the visualization. Second, you will perform feature engineering to extract or construct meaningful features from the dataset, based on your knowledge of the problem domain and the desired objective of the intended solution. Performing these two steps should significantly improve your accuracy over the baseline model which simply uses the raw features from the training set.

You will use a popular algorithm [XGBoost (eXtreme Gradient Boosting)](https://arxiv.org/pdf/1603.02754.pdf) to train your model. XGBoost belongs to a family of machine learning algorithms known as ensemble methods, which builds a strong classifier by combining the predictions of multiple classifiers. This allows the final model to be more generalizable and robust compared to a single classifier, allowing your model to learn subtle and deep interactions between features. XGBoost is also fast since the algorithm is designed to optimize its computing workload and its memory allocation.

In the first part of Task 1, you are visualizing the existing features to explore the dataset and get ideas for feature engineering. In the second part of Task 1, you are provided with the model training code so that you can focus on engineering informative features. **You should NOT modify the code that trains the model in the provided code skeleton.** The accuracy of your model will be determined by the quality of the features you create.

In this project, you need to launch a GCP Compute Engine instance using the student image provided by us. Let’s get started.

### Orchestrate and Manage Architecture with Terraform

You need to create a submitter VM to complete this project. Although you are allowed to use the Web UI to provision your resources in this project, we strongly recommend that you use terraform.

You will be provisioning resources using Google Cloud Shell, refer to the GCP Into Primer for more information.

1. Create a GCP project called `ml-fare-prediction` from the Web UI. We suggest that you create a new project instead of using existing projects so that you start from a clean state and avoid unnecessary errors.
2. Open GCP cloud shell and set up Google Cloud SDK. We will configure resources for this project through GCP cloud shell.

### Setup Google Cloud SDK

1. [Initialize Cloud SDK](https://cloud.google.com/sdk/docs/initializing#run_gcloud_init).

   ```
   gcloud init
   ```

2. Note down the project ID of the project you created as you will use the ID very often later. Please note that project ID is different from project name, which is `ml-fare-prediction-xxxxxx`, e.g., `ml-fare-prediction-123456`.

   ```
   $ gcloud projects list
   PROJECT_ID: ml-fare-prediction-xxxxxx
   NAME: ml-fare-prediction
   PROJECT_NUMBER: xxx
   ...
   ```

3. Go to [https://console.cloud.google.com/billing/](https://console.cloud.google.com/billing/projects). Click the "Actions" list at the end of the GCP project, choose "Change Billing" from the dropdown list. Select the billing account and click "SET ACCOUNT".

   ![Billing Account](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/set-billing-account.png)

   **Figure 2**: Set the billing account

   

   Information

   

   #### Billing Account for Project 6

   You can use “Billing Account for Education” or Free Tier in this project.

   In case you have insufficient credits in your GCP coupon, the GCP free trial can be enough to complete the project, but keep in mind that there is a limit on the number of calls for certain APIs and services used in the project (in Task 3 and Task 4). The free tier for Natural Language API ends after 5k requests, Google Maps provides $200 as free usage, and text-to-speech has a free tier of 1M requests. We believe that Free Tier is sufficient to complete the project **if you exercise caution**.

   It is highly recommended that you monitor your budget closely during the process. You may stop running instances used in your project if you are temporarily leaving the project and will get back after. Terminate all resources using `terraform destroy` and delete the GCP project to prevent incurring of additional costs when you finish the project.

   

   

4. The resources used in this project can be very expensive. Therefore, we strongly recommend that you estimate and plan your resource usage before getting started. Budget your usage and track your expenses within a Google Cloud Platform project or billing account. Your budget can be a specified amount or based on prior spending. On the billing page, select the billing account name you enabled, and click "Budgets & alerts".

   ![Budget](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/gcp_billing_budget.png)

   **Figure 3**: The "Budgets and Alerts" page

5. Click "CREATE BUDGET", select the budget amount as $50 (or lower) and click "FINISH". Make sure to uncheck "Discounts" and "Promotions and others" in the "Credits" Section. You will receive emails when you exceed certain thresholds.

6. Enable the APIs for the project by open the link, remember to choose the project you just created: [Register your application for Google Vertex AI API, Compute Engine API, Google Cloud Storage JSON API, Cloud Resource Manager API in Google Cloud Platform](https://console.cloud.google.com/flows/enableapi?apiid=aiplatform.googleapis.com,compute,storage_api,cloudresourcemanager.googleapis.com)

7. Configure gcloud and set the default region, zone, and project:

   ```
   gcloud config set project ml-fare-prediction-xxxxxx
   gcloud config set compute/region us-east1
   gcloud config set compute/zone us-east1-b
   ```

8. Configure Application Default Credentials (ADC) to allow the Google Auth library to view and manage your data across Google Cloud Platform services

   ```
   gcloud auth application-default login
   ```

9. At the top bar of the cloud shell, you can click the Open `Editor` button to edit files in an editor. Create a new folder for terraform configurations in project 6 and enter this folder.

   ```
   mkdir p6_ml_terraform && cd p6_ml_terraform
   ```

10. Download the terraform template

    ```
    wget https://clouddeveloper.blob.core.windows.net/assets/machine-learning/terraform.tgz && tar zxvf terraform.tgz
    ```

11. Create a file named `terraform.tfvars` and set the values of the variables defined in `variables.tf`:

    ```
    project = "ml-fare-prediction-xxxxxx" # your ml-fare-prediction project ID
    ```

### Setting up a Submitter Instance

1. Launch the submitter instance with Terraform.

   ```
   terraform init # initialize a Terraform working directory if you have not done so
   terraform apply # it may take several minutes
   ```

2. When `terraform apply` completes, Terraform will output the HTTP endpoint of the instance and the command to SSH into the instance. **Note that after `terraform apply` completes, it may take 1-2 minutes before you can access port 80. Please be patient.** If you failed at `terraform apply`, make sure you have followed the writeup to enable necessary APIs and completed all required authentications.

3. Visit `http://<frontend-external-ip>` and launch your instance by entering your submission username, submission password, and click the **Launch Project** button under the **Machine Learning on the Cloud** section. Please monitor the logs until you see the message “You may now SSH in as the clouduser user”.

4. Use the `gcloud compute ssh` command to connect to the instance. You may get prompted to create a new SSH key, if this is the first time you run this command.

   ```
   gcloud compute --project ml-fare-prediction-xxxxxx ssh --zone us-east1-b clouduser@workspace-vm
   ```



Information



- **Always destroy the resources launched by terraform through `terraform destroy`**.

- Be careful when you use `terraform destroy`. It may destroy all your resources previously provisioned by your script, not only the latest one! To destroy a particular resource, you can use `terraform destroy --target=RESOURCE_TYPE.NAME`.

- When working on GCP, if you get any errors related to credentials or permissions, it is likely that you omitted some authentication or configuration step, e.g.

  ```
  gcloud auth application-default login
  gcloud config set project ml-fare-prediction-xxxxxx
  gcloud config set compute/region us-east1
  gcloud config set compute/zone us-east1-b
  ```





### Solving Machine Learning Problems Progressively with Jupyter Notebook

When you solve machine learning problems, it is good practice to start off with a smaller subset of the data, so that you can iterate on feature engineering, model training, and performance evaluation with a short cycle time. The interactive approach to programming provided by Jupyter Notebook, as well as the ease of visualizing your data, makes it a great fit for this task.

To solve the task, please create a Python 3 virtual environment to install packages and start a Jupyter server (Note: a virtual environment is required, or you may not be able to run `pip install`):

Note: When you make a submission, `/home/clouduser/ProjectMachineLearning` will be packaged and submitted. Hence, **DO NOT** create a virtual environment under `/home/clouduser/ProjectMachineLearning`.

```
export LC_ALL=en_US.utf-8                         # set the locale to support UTF-8
python3 -m venv /home/clouduser/virtualenv        # create a virtualenv
source /home/clouduser/virtualenv/bin/activate    # activate a virtualenv
cd /home/clouduser/ProjectMachineLearning         # change working directory to the project folder
pip install --upgrade pip                         # upgrade pip to the latest version
pip3 install -r requirements.txt --use-pep517     # install packages
jupyter notebook --no-browser                     # start a Jupyter server
```

You will get prompted with the following message:

```
copy and paste one of these URLs:
    http://localhost:8888/?token=<token>
 or http://127.0.0.1:8888/?token=<token>
```

As you have learned in this course, most ports should not be made open to the public. You may open a new terminal and start an SSH tunnel on your machine on a local port, such as `2222`, that connects to a GCE instance on its remote port `8888`:

```
gcloud compute ssh clouduser@workspace-vm -- -L 2222:localhost:8888
```

1. You can now access the Jupyter server by visiting `http://localhost:2222/?token=<token>` in your browser.

2. You can find the project files including the code skeleton for Task 1 under `/home/clouduser/ProjectMachineLearning/`.

3. The dataset is under the `/home/clouduser/ProjectMachineLearning/data`.

   ```
   # a small portion of the raw training dataset you need to work on in this task
   cc_nyc_fare_train_small.csv
   # a tiny test dataset with only 10 records to test q3-q4 data_analysis.ipynb
   cc_nyc_fare_train_tiny.csv
   # a tiny test dataset to test q1 in data_analysis.ipynb
   NA_boundary_box.csv
   # the test dataset for feature engineering
   cc_nyc_fare_test.csv
   ```

In this task, you will need to implement the following two scripts to finish Data Exploration and Feature Engineering:

1. **Data Exploration** in `data_analysis.ipynb`: This script will lead you to explore the data through visualization, and will give you some ideas about core procedures in feature engineering.
2. **Feature Engineering** in `Task1.ipynb`: In this script you will need to perform feature engineering based on the implementation in the data analysis script. You are expected to extract meaningful features which can be useful to train a model.

### Explore Data by Visualization

The questions in `/home/clouduser/ProjectMachineLearning/data_analysis.ipynb` will lead you to explore the dataset by visualizing spatial and time-related features and help you get an idea of how the following feature engineering task could be done.

You should follow the instructions in `data_analysis.ipynb` and visualize the dataset. You must finish q1-q4 based on your visualizations. You can check your answers using the `runner.sh` script in the `ProjectMachineLearning` folder by following these steps:

```
  # Activated the Python 3 virtual environment
  source /home/clouduser/virtualenv/bin/activate
  # Enter ProjectMachineLearning folder
  cd ~/ProjectMachineLearning/
  # Run all four questions
  ./runner.sh
  # Or you can use the following command to get the answer for a specific question
  python3 data_analysis.py -r questionId(e.g., q1)
```

**Note**: Make sure that the Python 3 virtual environment is activated when submitting. Please learn more details in `runner.sh`.

### How to Submit

Once you have completed all the questions, you can submit the answers to the evaluation system using the `submitter` executable. **Note: Make sure that the Python 3 virtual environment is activated when submitting.**

You may need to create yet another SSH connection. For example, you may need at least 3 SSH connections at the same time: one for the Jupyter server, one for the SSH tunnel, and one for the submission.

```
source /home/clouduser/virtualenv/bin/activate  # make sure that you activated the same virtualenv before you make submissions
export SUBMISSION_USERNAME=your_submission_username
export SUBMISSION_PASSWORD=your_submission_pwd
./submitter -t task1_viz
```

### Feature Engineering

1. Open `Task1.ipynb` with Jupyter Notebook. In `Task1.ipynb`, you are provided with the boilerplate code that loads the training data, trains the model and evaluates the performance. The missing part is feature engineering. Your job is to implement `process_train_data` and `process_test_data`, that take in a raw DataFrame, transform the data, and output a predictor DataFrame with features.
2. Recall that a predictor variable is a variable which represents a feature and whose values will be used to predict the value of the target variable. In many cases, the original data cannot be directly used as the predictor variables and data transformation is needed as a major part of feature engineering. Therefore, you may need to refer to your exploration of data in q1-q4, and train the data to derive the predictor DataFrame with features.
3. Feel free to use additional code cells to explore and visualize the data using libraries such as [matplotlib](https://matplotlib.org/) and [seaborn](https://seaborn.pydata.org/).
4. **Note:** the submitter for task 1 will export your notebook as an executable Python script for grading. You must tag any cells containing exploration code with the tag `excluded_from_script`, so that the submitter can exclude them in the converted Python script. Otherwise, unnecessary code (such as data visualization) will get executed during the submission, which could take a long time to finish. You can display the tags for each cell in the Jupyter Notebook: `View > Cell Toolbar > Tags`.

### Ideas for Feature Engineering

The purpose of feature engineering is to create informative features that are meaningful in the problem domain to help your model learn the problem better. Human interpretation of the problem is often required.

You may want to reflect on your exploration in q1-q4 when doing feature engineering.

1. Visualize the training data, find outliers and handle them properly.
2. While the coordinates of each pickup and drop-off point contain implicit information about the distances per trip, you may want to transform geospatial data into actual distances using the formula to calculate [haversine distance](https://en.wikipedia.org/wiki/Haversine_formula). In general, the model can learn better if you define features in an explicit way.
3. Think about time-based features, which could be correlated with traffic conditions that may influence the fare. Note that pickup timings are recorded in the dataset as a timestamp, while XGBoost only accepts numerical variables.
4. Think about features that are correlated to the users’ willingness to pay higher fees where [price discrimination](https://en.wikipedia.org/wiki/Price_discrimination) takes effect. For example, there could be certain hotspots for cab pickups, that might be a good predictor of price. **One great example is pickups from airports.** Think about how you can engineer features that hold information about the locations of pickups.

### How to Submit

Similar to the data visualization part, run the executable using the command `./submitter -t task1_fe` from the ProjectMachineLearning folder.

```
source /home/clouduser/virtualenv/bin/activate  # make sure that you activated the same virtualenv before you make submissions
export SUBMISSION_USERNAME=your_submission_username
export SUBMISSION_PASSWORD=your_submission_pwd
./submitter -t task1_fe
```

The submitter will export `Task1.ipynb` as a Python script, execute the script, and run `process_train_data` to extract features from the training set to train the model. The model will then be evaluated by making predictions using the test set with the `fare_amount` column (i.e., the target) withheld. The predicted values produced by your model will be compared to the actual values, and your model is evaluated by the Root Mean Squared Error (RMSE) metric.

The submitter will upload the predictions generated by your model for grading. You can check your score for this task and the RMSE of your model on the Sail() platform.

If you run `task1_viz` after `task1_fe`, it will erase the score for the feature engineering part. To get both scores for data visualization and feature engineering, remember to re-run `task1_fe` after running `task1_viz`.



### What to Submit

To get a full score for task1, ensure that your source code is located in your `ProjectMachineLearning` folder when submitting. Additionally, remember to keep your working solution for q1-q4 in the same folder.

### Hints

1. Be very careful about removing outliers from the training set, if you choose to do so. Removing outliers can definitely improve your cross-validation score, since you are removing the data points that disagree the most with your model. However, a model trained this way will not generalize well to the test set. Remember, the purpose of your model is to make good predictions on data that are unseen in training.
2. If you cannot achieve a full score in this task, read the "Ideas for Feature Engineering" section and make sure each bullet point is reflected in your design.

### Warnings

You must write your transformation code to **be stateless** if you choose to use methods such as [pandas.get_dummies](https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.get_dummies.html), [pandas.DataFrame.quantile](https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.DataFrame.quantile.html), or [pandas.qcut](https://pandas.pydata.org/pandas-docs/stable/reference/api/pandas.qcut.html).

**Also, the order of the predicted values should be the same as the order of the records in the original test dataset. You should not produce the predicted values in the wrong size or the wrong order.**

### Troubleshooting

If you get the following error `jupyter: command not found` when executing the submitter, it is because you did not activate the virtual environment so `jupyter` cannot be resolved. Run `source /home/clouduser/virtualenv/bin/activate` to resolve it.

Google Vertex AI Training and Hyperparameter Tuning

## Google Vertex AI Training and Hyperparameter Tuning

### Description

After you crafted a good and robust set of features from a small portion of the raw training data, the next step is to scale up and train your model using the full (and more representative) training set.

In this task, you will take advantage of the elastic scaling features offered by [Google Vertex AI](https://cloud.google.com/vertex-ai/) to train your model, and use the built-in [automatic hyperparameter tuning](https://cloud.google.com/vertex-ai/docs/training/hyperparameter-tuning-overview?hl=en) feature to improve the accuracy of the predictions. After training your model, you will need to upload the model to Google Vertex AI, which will allow you to host your trained machine learning models in the cloud, and use the Vertex AI prediction service to make predictions using your uploaded model. The prediction service uses managed computing resources in the cloud to run the model.

### Serverless Machine Learning Service

Machine learning with large datasets places high demands on the computing power and memory capacity of the machines used for training. Without having advanced IT infrastructure on-premise, an increasingly affordable option is to run machine learning workloads using managed machine learning services like [Google Vertex AI](https://cloud.google.com/vertex-ai/).

Google Vertex AI (formerly known as AI Platform) is a managed service that offers various features to simplify the process of building, training, and deploying machine learning models to production. One of the key benefits of Vertex AI is that it eliminates the need to manage infrastructure, allowing data scientists and developers to focus on the development of their models.

Vertex AI provides support for a variety of popular machine learning frameworks, including scikit-learn, XGBoost, Keras, and TensorFlow, which can be used to develop and train models. The service also offers automatic scaling, which means that as the demand for your model increases, Vertex AI will automatically provision resources to handle the additional traffic, allowing your model to scale seamlessly without manual intervention.

In summary, Google Vertex AI is a comprehensive managed machine learning service that offers developers and data scientists a variety of features, including support for popular machine learning frameworks, automated model training, and deployment to production with auto-scaling capabilities.

### Hyperparameter Tuning

In machine learning, **model parameters** are variables that are internal to the model, and are required by the model when making predictions. Parameters are properties learned by the model from the training data during the training phase. They are often not set manually by practitioners.

**Hyperparameters**, on the other hand, cannot be learned during training directly but are set before the learning process begins. Hyperparameters are external variables and are often set by practitioners. When creating a machine learning model, you'll be presented with design choices as to how to define your model architecture. Oftentimes, we don't immediately know what the optimal model architecture should be for a given model, and thus we'd like to be able to explore a range of possibilities. Parameters that define the model architecture are called hyperparameters. Hence, this process of searching for the ideal model architecture is called hyperparameter tuning. Hyperparameters affect the training thus the model performance, and therefore there exists a need to tune hyperparameters to improve the accuracy of the models. For example, you could train your model, measure its accuracy (e.g., RMSE in our case), and adjust the hyperparameters until you find a combination that yields good accuracy. The scikit-learn framework provides a systematic way of exhaustively searching all the combinations of the hyperparameters with an approach known as [Grid Search](http://scikit-learn.org/stable/modules/generated/sklearn.model_selection.GridSearchCV.html). While hyperparameter tuning can help to improve your model, it comes at the cost of additional computation, since you have to re-train the model for each combination of hyperparameters. Many hyperparameters are set using heuristics, rules of thumb and trial and error, which can be a very difficult and demanding process.

The model improvement as a result of hyperparameter tuning is often only a few percentage points. However, you can imagine that even a slight improvement in the prediction capabilities for large consumer-facing companies like Amazon and Netflix will enable them to understand and serve their customers better and help them gain a competitive edge over their competitors.

### HyperTune (Hyperparameter Tuning as a Service)

One of the key benefits of using Vertex AI is the built-in support for hyperparameter tuning. You only need to specify a YAML configuration following [the specification](https://cloud.google.com/vertex-ai/docs/reference/rest/v1/projects.locations.hyperparameterTuningJobs#resource:-hyperparametertuningjob), with no need to write your own solution to perform tuning. Under the hood, HyperTune uses [Bayesian Optimization](https://cloud.google.com/blog/products/gcp/hyperparameter-tuning-cloud-machine-learning-engine-using-bayesian-optimization), a cutting-edge method of efficiently searching through the hyperparameter space, compared to the basic Grid Search approach. If you are interested, you may want to learn more about [the technology behind this](https://storage.googleapis.com/pub-tools-public-publication-data/pdf/bcb15507f4b52991a0783013df4222240e942381.pdf), though it is not required to complete this project.

With the elastic computing power provided by Vertex AI, HyperTune can help test different hyperparameter configurations in parallel to accelerate the search of an optimal set of hyperparameters that can improve model performance.

### Your Task

#### Model Training and Hyperparameter Tuning on Google Vertex AI

1. Create a [Google Storage Bucket](https://cloud.google.com/storage/docs/buckets) to store the training code that you will deploy to Vertex AI in the following tasks. Run the following command in GCP Cloud Shell (make sure you have run all GCP authentication or configuration steps):

   ```
   # You bucket ID should be universally unique, e.g. you GCP project ID ml-fare-prediction-xxxxx
   export BUCKET_ID=<your_bucket_id>
   # Be sure to create your bucket in us-east1 region which will be the same with the training job
   export REGION=us-east1
   # Create the bucket
   gsutil mb -l $REGION gs://$BUCKET_ID
   ```

2. Start the Jupyter server and view the files in your local browser. `~/ProjectMachineLearning/vertex_ai_trainer/train.py` contains the starter code for packaging a machine learning job for Vertex AI. You should copy the implementations of the methods you created in the feature engineering task into this script to replicate the feature engineering process. You may edit the Python script using Jupyter Notebook.

3. Your main objective for this task is to select the hyperparameters to be tuned by HyperTune. In the code skeleton, you are provided with the code to run HyperTune that you do not need to change.

4. As you are dealing with a large dataset, it is practical to start by randomly sampling a smaller portion when doing training. You should start with a low portion while testing model training, and increase the data size after you are able to better estimate how long the training would take under different settings and with different proportions of the training set. In `~/ProjectMachineLearning/vertex_ai_trainer/train.py`, there is a variable that defines the percentage of sampling. You should leave it as `0.2` when you are testing your solution to reduce the time and spending per iteration. After you have finished tuning and decided your final parameter values, you **MUST** update it to `1.0` in the last training job before you submit to Sail, so that the whole training dataset is used.

   ```
   SAMPLE_PROB = 0.2 # For testing
   SAMPLE_PROB = 1.0 # For submission
   ```

5. To upload your trained ML model to the bucket you just created, you should set your bucket name in the training script. Open `~/ProjectMachineLearning/vertex_ai_trainer/train.py`, the `OUTPUT_BUCKET_ID` variable indicates where the model will be uploaded after the ML training job. Set the variable to your bucket name:

   ```
   OUTPUT_BUCKET_ID = 'YOUR_OUTPUT_BUCKET'
   ```

6. You are also provided with a HyperTune configuration file, `config.yaml`. You need to add more parameters that will be tuned by HyperTune to improve the performance of the model, by adding more entries to `studySpec.parameters`. Additionally, you need to set the two `<your_bucket_id>` in the `trialJobSpec` section to the bucket id you created. You should NOT change the rest of the YAML file. You may refer to the [HyperTune documentation](https://cloud.google.com/vertex-ai/docs/reference/rest/v1/projects.locations.hyperparameterTuningJobs#resource:-hyperparametertuningjob) to learn the available options.

7. You may want to read the [XGBoost Parameter Documentation](https://xgboost.readthedocs.io/en/latest/parameter.html) to get a better understanding about the various parameters that are used by the candidates to tune, and learn about [how to use the parameters in Python](https://xgboost.readthedocs.io/en/stable/python/python_api.html#module-xgboost.sklearn). In this task, you need to find a balance between increasing the accuracy during cross-validation and avoiding overfitting. Overfitting happens when a model learns the detail and noise in the training data to the extent that it negatively impacts the performance of the model on new data. This means that the noise or random fluctuations in the training data is picked up and learned as concepts by the model, which we want to avoid. You may want to explore the following parameters:

   1. `max_depth`: This parameter controls the depth of the trees. With greater depth, the model can capture more complex interactions between features, but will become more likely to overfit.
   2. `learning_rate`: This parameter controls the step size to take at each boosting step. Lowering this parameter causes XGBoost to take more conservative steps which may help prevent overfitting.
   3. `subsample`: This parameter controls the proportion of the training set that XGBoost will sample in each boosting iteration. A proper value of this parameter may help prevent overfitting.
   4. `n_estimators`: This parameter controls the number of trees used in the ensemble. More estimators improve the performance of the model at the cost of longer training time. There is a diminishing marginal benefit to the performance as the number of estimators increases.

8. After setting the parameters in `config.yaml` file, you should also implement the section `Improve model performance with hyperparameter tuning` in `train.py` to add your parameters to the training code. You can search “TODO” in `train.py` to make sure you have completed all required parts in this task.

9. After you finish `~/ProjectMachineLearning/vertex_ai_trainer/train.py` and `~/ProjectMachineLearning/config.yaml`, you can now submit your training package to Vertex AI using your GCP VM. Please first authorize the Cloud SDK to access your GCP project and configure the default region by running the following commands on the VM.

   ```
   gcloud auth login
   gcloud config set project ml-fare-prediction-xxxxxx
   gcloud config set compute/region us-east1
   ```

10. Enable the following APIs (Vertex AI API, Cloud Storage API, Notebooks API, Dataflow API and Artifact Registry API) by running following commands on the VM.

    ```
    gcloud services enable aiplatform.googleapis.com storage.googleapis.com notebooks.googleapis.com dataflow.googleapis.com artifactregistry.googleapis.com
    ```

11. For custom jobs in Vertex AI, the training application runs in a [pre-built container](https://cloud.google.com/vertex-ai/docs/training/create-python-pre-built-container). Before you can perform custom training with a pre-built container, you must create a Python source distribution that contains the training application and upload it to a Cloud Storage bucket you created previously where your Google Cloud project can access. Run the following commands to package the Python training job and upload it to your bucket:

    ```
    # Enter ProjectMachineLearning folder
    cd ~/ProjectMachineLearning/
    # Package the python training app
    python setup.py sdist
    # Upload your python training app to the cloud storage bucket
    gsutil cp dist/vertex_ai_trainer-0.1.tar.gz "gs://"$BUCKET_ID"/fare-prediction/dist/trainer-0.1.tar.gz"
    ```

    You can go to your Google Cloud Storage Bucket console to check if you have successfully uploaded your training app to the bucket:

    ![Packeged python app on Google Cloud Storage Bucket](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/packaged-app-storage.png)

    **Figure 4**: Packaged python app on Google Cloud Storage Bucket

    Now you are able to derive the link to the Python training app and you can use it in your training job. The link is already defined in `~/ProjectMachineLearning/config.yaml` at `packageUris`. Now you should replace the 2 `<your_bucket_id>` in the `trialJobSpec.workerPoolSpecs.pythonPackageSpec.packageUris` and `trialJobSpec.baseOutputDirectory.outputUriPrefix` with the bucket id you created.

12. A bash script `create_training_job.sh` is provided for your convenience to create a hyperparameter tuning job to Vertex AI. Update the value of `JOB_NAME` in the script. You can edit any file using Jupyter Notebook. You can then execute the script to submit the training job to Vertex AI. Note that the training may take tens of minutes.

    ```
     ./create_training_job.sh
    ```

13. Please note the job id from the output of above script. You can also find the job ID on the console as shown in Figure 5 and you can monitor the progress of the training jobs on [the Vertex AI dashboard](https://console.cloud.google.com/vertex-ai/training/hyperparameter-tuning-jobs). Vertex AI automatically pipes any print statements in your code to the logs to help you debug your code.

    ![Google Vertex AI Job Logs](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/vertex-ai-job.png)

    **Figure 5**: Google Vertex AI Job Logs

14. You can also use `gcloud` to view the progress of the job. This will show you the values of the hyperparameters that were tried at each trial of HyperTune, as well as the associated objective score. You will be able to observe that the objective score improves quickly at the beginning, with a diminishing marginal benefit the more trials you run. Through experimentation, you should be able to find the sweet spot between cost and model accuracy.

    ```
    export JOB_ID=<your_job_id>
    gcloud ai hp-tuning-jobs describe $JOB_ID --region=us-east1
    ```

15. You can find the best model from the web console. Alternatively, you can run the following command to retrieve the lowest RMSE model:

    ```
    gcloud ai hp-tuning-jobs describe $JOB_ID --region=us-east1 --format=json | jq '([ .trials[].finalMeasurement.metrics[].value ] | min) as $m | .trials | map(select( .finalMeasurement.metrics[].value == $m))'
    ```

    Here is an example output that contains the best score for your reference. To further improve your model, you can analyze the best-performing parameters listed under `parameters`:

    ```
    [
       {
          "endTime":"2023-02-04T04:42:44Z",
          "finalMeasurement":{
             "metrics":[
                {
                   "metricId":"nyc_fare",
                   "value":3.29871 # the best score
                }
             ],
             "stepCount":"1"
          },
          "id":"4",
          "parameters":[
             {
                "parameterId":"learning_rate",
                "value":<learning_rate_value>
             },
             {
                "parameterId":"max_depth",
                "value":<max_depth_value>
             },
             {
                "parameterId":"n_estimators",
                "value":<n_estimators_value>
             },
             {
                "parameterId":"subsample",
                "value":<subsample_value>
             }
          ],
          "startTime":"2023-02-04T04:39:37.957347897Z",
          "state":"SUCCEEDED"
       }
    ]
    ```

16. When the training job is completed, the trained models will be saved to the bucket you have provided. You can go to the Google Storage bucket to find the best model (with the minimal object value). The GS path is `gs://$BUCKET_ID/$JOB_ID` and the model is named as `<trialId>_rmse<rounded_objectiveValue>_model.bst`. You can also use `gsutil ls gs://$BUCKET_ID/$JOB_ID` to list all trained models in this job. Among the models, you can find the best model with the best objective value rounded to 3 decimal digits, e.g.,

    ```
     gs://$BUCKET_ID/$JOB_ID/1_rmse3.299_model.bst
    ```

#### Upload the Model to Google Vertex AI

Vertex AI offers an online prediction service that will manage computing resources in the cloud to make predictions with your models. You can now deploy the best model trained with hyperparameter tuning.

1. Download the best saved model file (`.bst`) from Google Storage, and rename it as `model.bst` which is required by Vertex AI.

   ```
   gsutil cp gs://$BUCKET_ID/$JOB_ID/<trialId>_rmse<rounded_objectiveValue>_model.bst model.bst
   ```

2. Create and export a `MODEL_DIR` variable to store the `model.bst` file

   ```
   export MODEL_DIR=<your_model_directory>
   # e.g., export MODEL_DIR=model
   ```

3. Upload the downloaded model file to the above folder on Google Storage.

   ```
   gsutil cp model.bst gs://$BUCKET_ID/$MODEL_DIR/
   # e.g., gsutil cp model.bst gs://ml-fare-prediction-xxxxxx/model/
   ```

4. The Vertex AI Model Registry is a centralized platform where you can manage the lifecycle of your machine learning models. With this tool, you can organize, track, and manage different versions of each model. When you want to deploy a specific version, you can assign it to an endpoint directly from the registry. To upload your model to the Vertex AI Model Registry, use the following command:

   ```
   gcloud ai models upload \
   --container-image-uri="us-docker.pkg.dev/vertex-ai/prediction/xgboost-cpu.1-5:latest" \
   --display-name=<your_model_name> \
   --artifact-uri=gs://$BUCKET_ID/$MODEL_DIR \
   --region=us-east1
   
   # e.g.
   # Example:
   # gcloud ai models upload \
   # --container-image-uri="us-docker.pkg.dev/vertex-ai/prediction/xgboost-cpu.1-5:latest" \
   # --display-name=nyc_model \
   # --artifact-uri=gs://$BUCKET_ID/model \
   # --region=us-east1
   ```

5. After running the above command, you can check the model in your Vertex AI Dashboard:

   ![Google Vertex AI Model Registry](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/vertex-ai-model-registry.png)

   **Figure 6**: Google Vertex AI Model Registry

6. You can now deploy your model to an endpoint using the following steps, so that you can use the model for online prediction.

   1. Click “Deploy to endpoint”

      ![Vertex AI Model deployment step 1](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/model-endpoint-1.png)

      **Figure 7**: Vertex AI Model deployment step 1

   2. Create an Endpoint

      ![Vertex AI Model deployment step 2](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/model-endpoint-2.png)

      **Figure 8**: Vertex AI Model deployment step 2

   3. In Model settings, set the Machine type as `n1-standard-2` and keep other settings as default

      ![Vertex AI Model deployment step 3](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/model-endpoint-3.png)

      **Figure 9**: Vertex AI Model deployment step 3

   4. Click the “DEPLOY” button at left. Note that the deployment might take around 15 minutes to complete. After the deployment is finished, check the `Online Prediction` section from Vertex AI Dashboard to get the Model endpoint ID.

      ![Vertex AI Model deployment step 4](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/model-endpoint-4.png)

      **Figure 10**: Vertex AI Model deployment step 4

#### Fare Prediction Service with Google App Engine

With the model version deployed, you will now build a web service that handles web requests which contain `pickup_datetime, pickup_longitude, pickup_latitude, dropoff_longitude, dropoff_latitude, passenger_count` (i.e., there are no `key` or `fare_amount` columns), and returns the fare predictions. In `/home/clouduser/ProjectMachineLearning/app-engine-pipeline`, you will find the code skeleton of a Flask app with a `/predict` endpoint. Your job is to implement this endpoint and deploy it to Google App Engine (GAE), which is a fully managed, serverless platform for developing and hosting web applications at scale.

Implement the application to:

1. Read the web request which is in JSON format
2. Transform the web request into features using your feature engineering code. Note: now you are handling web requests and making predictions using your model, hence you should NOT drop outliers as you did in training.
3. Make an API call to your model hosted on Google Vertex AI, and return the fare prediction.

Your application will make use of the following environment variables. You should ensure that these environment variables are set in your local environment for testing and in the Google App Engine environment:

| Environment Variable       | Description                                                  |
| :------------------------- | :----------------------------------------------------------- |
| `GOOGLE_CLOUD_PROJECT`     | The full GCP project ID that your application / Google Vertex AI model is running in |
| `VERTEX_AI_MODEL_ENDPOINT` | The ID of the Google Vertex AI model endpoint that predictions will be made against |
| `LOCATION`                 | The region to host your resources. Make sure you keep as `us-east1`, the same as the region of your bucket and model |



To test locally, set the environment variables for your GCP project, Google Vertex AI model endpoint ID, and the location, retrieve the GCP credentials and start the Flask application.

```
gcloud auth application-default login # acquire new user credentials to use for Application Default Credentials

source /home/clouduser/virtualenv/bin/activate  # make sure that you activate the same virtualenv if you have not done so
cd /home/clouduser/ProjectMachineLearning/app-engine-pipeline
pip install -r requirements.txt
export GOOGLE_CLOUD_PROJECT=<your_project_id>
export VERTEX_AI_MODEL_ENDPOINT=<your_model_endpoint_id>
export LOCATION=us-east1
python main.py
# * Serving Flask app "main" (lazy loading)
# * Environment: production
#   WARNING: Do not use the development server in a production environment.
#   Use a production WSGI server instead.
# * Debug mode: off
# * Running on http://127.0.0.1:5000/ (Press CTRL+C to quit)
```

#### Functional Testing before Production Deployment

Functional testing is a type of software testing by passing input and validating the output. Functional testing is a type of [black-box testing](https://en.wikipedia.org/wiki/Black-box_testing) as it is not concerned about how the input is processed or how the output is produced, but rather, the final results that simulate actual user usage of the system.

Note: functional testing does not imply that you are testing a “function” in your code (which is generally in the scope of a unit test), functional testing tests a part of functionality of your whole system.

Here is a simplified version of the functional testing process:

1. Identify functions of how the software should behave in compliance with the requirement specification
2. Create input data based on the functions specifications
3. Compute the expected output
4. Create functional test cases
5. Run the test cases to validate the actual and expected output

You are provided with a ready-to-use Python script to test the server, `test_script.py`. The script implements functional tests for each of the APIs using unit tests. `test_script.py` reads from the test input file `predict_input.csv`, converts the rows into web requests, and sends them to the server and validates the response.

Please open a new terminal to run `test_script.py`. You should have the previous terminal running the server as mentioned above and run `test_script.py` in another terminal as follows:

```
python test_script.py -v
# a sample test that reports if your application passed the first case and failed the rest 6
# in this task, you only need to pass the first test case for this task
# .FFFFFF
# ----------------------------------------------------------------------
# Ran 7 tests in 6.315s

# you can also use the following command to run a specific test
python -m unittest test_script.TestAPIMethods.test_predict
```

The only test case you need to pass in this task is the first test case `test_predict`, which is for the `/predict` endpoint. You can ignore the other test cases as they are for the endpoints which will be implemented in the next tasks. After the functional test, you can deploy the application to GAE.

1. Create an `env_vars.yaml` file in the home directory (`/home/clouduser/`) of your student VM with the following content:

   ```
   env_variables:
     GOOGLE_CLOUD_PROJECT: <your_project_id>
     VERTEX_AI_MODEL_ENDPOINT: <your_model_endpoint_id>
     LOCATION: us-east1
   ```

   Make sure that you **DO NOT** place this yaml file in your submission folder. This yaml file is referenced by `app.yaml` and ensures that the specified variables are available in the Google App Engine environment.

2. Deploy the Flask application to App Engine using `app.yaml` file provided in `/home/clouduser/ProjectMachineLearning/app-engine-pipeline/`

   ```
   gcloud app deploy app.yaml
   # ...
   # Deployed service [default] to [https://ml-fare-prediction-xxxxxx.appspot.com]
   ```

   When you deploy the app for the first time, you will be asked to choose a region for your GAE application. Pay attention that the GAE region CANNOT be changed once it is set. To avoid deployment failure caused by regional insufficient resources, we recommend you to choose `us-east1`.

   The app deployment takes around 3 minutes to build and deploy. Please closely monitor your deployment from the console until completed.

3. Note down the endpoint of the GAE application from the console output after deployment completion.

4. You may test the GAE application using the `test_script.py`.

   ```
   # update endpoint to 'https://ml-fare-prediction-xxxxxx.appspot.com'
   python test_script.py
   # a sample test report if your application passed the first case and failed the rest 6
   # in this task, you only need to pass the first test case
   # .FFFFFF
   # ----------------------------------------------------------------------
   # Ran 7 tests in 6.315s
   ```

### How to submit

1. Make sure that your Flask app is running on Google App Engine.

2. Run the submitter to package your code for submission.

   ```
   cd ~/ProjectMachineLearning
   source /home/clouduser/virtualenv/bin/activate
   export SUBMISSION_USERNAME=your_submission_username
   export SUBMISSION_PASSWORD=your_submission_pwd
   ./submitter -t task2
   ```

3. To get a full score in this task, you need to:

   1. Enable HyperTune, add **at least 3** additional parameters to tune, run the hypertuning job and create a model on Google Vertex AI.
   2. Deploy the fare prediction application to GAE that uses the model created above and serves web requests correctly.
   3. The predictions should achieve a target accuracy, measured by RMSE.

Your prediction application will not be able to achieve the target accuracy goal if you have not improved the model using hyperparameter tuning. We will check if you used hyperparameter tuning to improve your model while manually grading your solution.

### Hints

1. In `config.yaml`, you can tune the number and the type of worker instances by adjusting `workerPoolSpecs` to run more trials in parallel and complete training jobs in a shorter amount of time.
2. If you find errors with your application after deploying it to App Engine, you should consult the [Stackdriver Logs](https://cloud.google.com/logging/docs/view/overview).
3. If your model is overfitting, as mentioned before, you may want to adjust your parameters for models. You may also want to modify the features. If your training set has a high dimension and/or improper features design, your model is more likely to overfit.

### Troubleshooting

1. If you get the following error `TypeError: Timestamp(...) is not JSON serializable` when testing the Flask server, you should drop the column(s) (with `dtype` as `datetime64`) from the predictor DataFrame. In task 1, the code skeleton helped to drop the `pickup_datetime` column if you did not do so in `process_train_data` and `process_test_data`. In this task, you need to write your own code to deal with datetime column(s).

2. If you get any errors related to credentials or permissions, make sure you did not omit any authentication or configuration steps **on the submitter VM**, e.g.,

   ```
     gcloud auth login                                   # for `gcloud`
     gcloud auth application-default login               # for the Google Cloud SDK
     gcloud config set project ml-fare-prediction-xxxxxx
     gcloud config set compute/region us-east1
     gcloud config set compute/zone us-east1-b
   ```

3. If you get the following error when running the Flask application as a *local* server: `'Failed to retrieve http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/?recursive=true from the Google Compute Enginemetadata service.'`, deploying the application to GAE will bypass this issue.

4. If you face errors in Google App Engine Deployment, viewing the logs can be helpful. You can stream logs from the command line by running:

   ```
   gcloud app logs tail -s default
   ```

ML Application

## Machine Learning Application

One way to make our machine learning models useful to others is to incorporate them into user-facing applications. Consider a common scenario in our daily lives: you want to hail a ride to a destination. What you need to do is to open the ride-hailing app on your mobile phone, and record audio saying your original location and your destination. Then the app will automatically make a prediction about the fare of this ride in a very short time, and even return the result to you as speech. In this task, we will develop an interface that accepts speech queries and responds with a speech result. The architecture diagram below provides a high-level interaction between your application and cloud services. You will notice that this application is able to complete a complex task by integrating with machine learning models that you trained and deployed to Google Vertex AI, as well as machine learning services offered by the cloud provider (specifically NLP, Speech to Text, and Text to Speech).

![Machine Learning Application](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/P42Pipeline.jpg)**Figure 11**: High-level architecture and interactions of the Taxi Fare prediction application

### GCP APIs to Enable

This task will require calling a number of GCP APIs - ensure that the following APIs are enabled in your project to prevent exceptions when trying to call them from your application:

| API                                                          |
| :----------------------------------------------------------- |
| [Vertex AI API](https://console.cloud.google.com/apis/library/aiplatform.googleapis.com) |
| [Cloud Text-to-Speech API](https://console.cloud.google.com/apis/library/texttospeech.googleapis.com) |
| [Cloud Speech-to-Text API](https://console.cloud.google.com/apis/library/speech.googleapis.com) |
| [Cloud Natural Language API](https://console.cloud.google.com/apis/library/language.googleapis.com) |
| [Directions API](https://console.cloud.google.com/apis/library/directions-backend.googleapis.com) |



To enable the APIs above, open your gcloud CLI or Cloud Shell, make sure you have set the correct project using `gcloud config set project ml-fare-prediction-xxxxxx`. Run the following command to enable APIs above:

```
gcloud services enable aiplatform.googleapis.com texttospeech.googleapis.com speech.googleapis.com language.googleapis.com directions-backend.googleapis.com
```



### Creating a Service Account

As mentioned in the introduction, we will be using the Text-to-Speech API in this task. The Text-to-Speech API only accepts service account authentication. A service account is a Google account that represents an application, as opposed to representing an end user. The procedure to create a service account is listed in the [Creating Service Accounts](https://cloud.google.com/iam/docs/service-accounts-create) documentation. We recommend that you follow these steps for creating a service account:

1. In the GCP Console, go to the [Service Accounts](https://console.cloud.google.com/apis/credentials/serviceaccountkey?_ga=2.83149078.-1860728044.1554299234) key page.
2. Select the appropriate project.
3. Click "CREATE SERVICE ACCOUNT" at the top of the page.
4. In the "Service account name field", enter a name for your account.
5. Click "CREATE AND CONTINUE" to grant this service account access to the project.
6. From the Role list, select `Basic` > `Owner`.

![service_account_create.png](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/service_account_create.png)**Figure 12**: Grant the service account access to project.

1. Click "CONTINUE". Click "DONE" to create the service account.

2. Click on the service account name you just created, and navigate to the "KEYS" tab.

3. Click ADD KEY > Create New Key, and pick JSON as the key type.

   ![service_account_create.png](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/create_service_account_key.png)**Figure 13**: Creating a new key for a service account.

4. Hit "CREATE" , and a JSON file will download to your computer.

Please copy the JSON file to the **home directory** (`/home/clouduser/`) of your student VM. Make sure that you **DO NOT** place the JSON file in your submission folder. If you plan to use a service account, you need to set an environment variable. Provide the authentication credentials to your application code by setting the environment variable `GOOGLE_APPLICATION_CREDENTIALS`.

For example:

```
export GOOGLE_APPLICATION_CREDENTIALS="/home/clouduser/[FILE_NAME].json"
```

Do not add this variable to the `/home/clouduser/env_vars.yaml` created earlier, it will cause errors. You will have to add other environment variables to this file laater on, but **not** `GOOGLE_APPLICATION_CREDENTIALS`

### Create Google Maps Directions API Key

As part of this task, you will be using the Google Maps [Directions API](https://developers.google.com/maps/documentation/directions/start). Usage of the Directions API will require an API separate from your GCP credentials or service account. To generate an API key, follow the steps outlined in the [Directions API - Get API Key](https://developers.google.com/maps/documentation/directions/get-api-key) documentation. Once you have obtained an API key, you will need to export the `GOOGLE_MAPS_API_KEY` environment variable and set `GOOGLE_MAPS_API_KEY` in `/home/clouduser/env_vars.yaml` to make the key available in App Engine.

### Working with WAV data

In this project, you will be working with WAV audio and while it is not required to write the WAV files to disk as part of your web service, it will be greatly helpful for validating your API output.

### Notes on WAV audio data

1. All input and output WAV audio data for your APIs should be Base64 encoded.
2. Base64 implementations are available as part of the [Python standard library](https://docs.python.org/3/library/base64.html).
3. Python provides the [wave](https://docs.python.org/3/library/wave.html) module for working with WAV data.

#### Writing WAV data to a file

In Python, you may use the following listing to invoke your APIs and store the resulting `speech` to a file on disk. You should take note of when you are base64 encoding / decoding the audio data and whether that data is currently represented as a string or as a byte array - certain methods will require a byte representation of your data.

```
import base64
import requests
import wave

file = 'the_cooper_union_the_juilliard_school.wav'

with open(file, 'rb') as audio:
    data = base64.b64encode(audio.read()).decode('utf-8')
    r = requests.post('http://localhost:5000/farePrediction', data=data)

    with wave.open('reply-' + file, 'wb') as reply:
        speech_wave_frames = base64.b64decode(r.json()['speech'].encode('utf-8'))
        reply.setnchannels(1)
        reply.setsampwidth(2)
        reply.setframerate(16000)
        reply.writeframes(speech_wave_frames)
```

### Flask App

Flask is a Python-based web framework that you will be using in this task to create the application. Below is the code for a basic flask application:

```
from flask import Flask  // 1

app = Flask(__name__)    // 2

@app.route('/hello')          // 3
def hello():
    return 'Hello, World!'
```

1. Import the `Flask` module
2. Create a Flask application instance and call it `app`. You can use the app instance to handle incoming HTTP requests.
3. `@app.route()` is a decorator that turns the function `hello()` into a flask **view** function which converts the return value into an HTTP response to the client. The `route()` decorator to tell Flask what URL should trigger our function. Passing `/hello` to `@app.route()` indicates that the function `hello()` responds to the URL `/hello`.

In short, the implementation process of an individual function in a Flask web application is to first match the URL in `@app.route()`, then implement functional logic code inside the function, and finally return the required results in the format of JSON response.

Normally at the beginning of each function implementation, you should first get the data you need to handle in this function from the HTTP request. Flask parses incoming request data into a global request object that you can use to access incoming request data from within a view function. You can read more about it here: [Flask](https://flask.palletsprojects.com/en/1.1.x/api/#flask.request). Flask gets data from HTTP requests differently according to the HTTP method:

- For POST request:

  ```
    variable = request.data
  ```

- For GET request:

  ```
    variable = request.args.get('key')
  ```

### API Definitions

For this task, you will develop multiple APIs, each of which implements a small portion of the larger application’s functionality. Here are the API Definitions that you will need to refer to when you implement the Flask application.

| API              | HTTP Method | Input                                                        | Output                                                       |
| :--------------- | :---------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `farePrediction` | **POST**    | POST body contains base64 encoded audio data for a single fare request | **predicted_fare** - The numeric fare predicted by the model, to two decimal places **entities** - A list of named entities identified in the request **text** - The reply text of the fare request. In the format of "**Your expected fare from** X **to** Y **is $** Z" **speech** - Base64 speech of the response text field. (e.g. `{"predicted_fare": "23.78", "entities": ["Charging Bull", "Carnegie Hall"], "text": "Your expected fare from Charging Bull to Carnegie Hall is $23.78", "speech": <BASE64 ENCODED AUDIO>}`) |
| `predict`        | **POST**    |                                                              |                                                              |
| `speechToText`   | **POST**    | POST body contains base64 encoded audio data                 | **text** - The text of the provided speech file (e.g. `{'text': "I would like to go from Central Park Zoo to Yankee Stadium"}`) |
| `textToSpeech`   | **GET**     | **text** - An English sentence containing a trip request (e.g. `I would like to go from Central Park Zoo to Bronx Zoo.`) | **speech** - Base64 encoded WAV file (generated by GCP text to speech) (e.g. `{"speech": <BASE64 ENCODED AUDIO>}`) |
| `namedEntities`  | **GET**     | **text** - Sentence containing NYC landmarks (e.g. `American Museum of Natural History and Bryant Park`) | **entities** - List of entities identified (e.g. `["American Museum of Natural History", "Bryant Park"]`) |
| `directions`     | **GET**     | **origin** - NYC landmark text (e.g. `Pennsylvania Station`) **destination** - NYC landmark text (e.g. `Times Square`) | **start_location** - Object containing latitude and longitude (e.g. `{"lng": -74.0133372, "lat": 40.7055432}`) **end_location** - Object containing latitude and longitude (e.g. `{"lng": -74.0133372, "lat": 40.7055432}`) |



### Your Task

1. Understand the **API Definitions** you need to implement in this task.

2. A sample Flask application that can be directly deployed to the App Engine is provided in `ProjectMachineLearning/app-engine-pipeline`. You can continue using the same application from the previous task.

3. You will need to develop multiple APIs in `main.py`. This application has API stubs that should be implemented in accordance with the **API definitions** provided above.

4. The `farePrediction` API will make use of the code required for the remaining APIs. It is recommended to complete other individual APIs prior to attempting the `farePrediction` API. It is also suggested that you arrange your code in a modular fashion to enable re-use.

5. While implementing the `farePrediction` API, you can assume `pickup_datetime` to be the current system datetime. Also, you can assume `passenger_count` to be 1 or 2.

6. The **starter code** includes **client classes** for each of the APIs at `ProjectMachineLearning/app-engine-pipeline/clients`. Please review and get familiar with those client classes before you start coding.

7. You will need to utilize the client classes in your functions to realize functionalities. Please take the `predict()` function in `main.py` as an example, which uses the `ai_platform_client` client class.

8. We also provided you with test cases in `ProjectMachineLearning/app-engine-pipeline/test_script.py` script. You can learn from the related functions in the test script about the HTTP request content, and thus get the data you need from the HTTP requests in your implementation.

9. You may use the WAV file `the_cooper_union_the_juilliard_school.wav` in `/ProjectMachineLearning/app-engine-pipeline/test_dataset` to test your farePrediction and speechToText implementations:

   To help you test your application, a small number of `curl` requests will be provided to demonstrate POSTing the base64 encoded WAV data. Additionally, it is suggested that you develop a simple Python or Java client to test your APIs instead of using `curl` directly.

- **`textToSpeech` API**

  The `textToSpeech` will return a JSON object containing the translated speech base on the input text:

  ```
  $ curl -s -G localhost:5000/textToSpeech --data-urlencode text="I would like to go from the Cooper Union to the Juilliard School" | jq
  ```

  Sample output:

  ```
  {
  "speech": <BASE64 ENCODED AUDIO>
  }
  ```

  Note that the Base64 encoded audio output will be extremely long.

  To save the WAV output directly from the command line, you must first decode it using `base64 -d` and redirect the content to a file:

  ```
  $ curl -s -G localhost:5000/textToSpeech --data-urlencode text="I would like to go from the Cooper Union to the Juilliard School" |\
  jq .speech |\
  tr -d '"' |\
  base64 -d > converted_text_to_speech.wav
  ```

  After running the command, you can find a WAV file named `converted_text_to_speech.wav` in the folder in which you run this command.

- **`speechToText` API**

  The `speechToText` API will return a JSON object containing the translated text based on the input speech WAV file.

  ```
  $ cd ~/ProjectMachineLearning/app-engine-pipeline/test_dataset
  $ cat the_cooper_union_the_juilliard_school.wav |\
  base64 |\
  curl -s -H "Content-Type: application/octet-stream" --data @- localhost:5000/speechToText |\
  jq
  ```

  Sample output:

  ```
  {
  "text": "I would like to go from the Cooper Union to the Juilliard School"
  }
  ```

### Testing the Flask application locally

In this task, your application will make use of additional environment variables. You should ensure that these environment variables are set in your local environment for testing and in the App Engine environment for grading (by updating the /home/clouduser/env_vars.yaml file you created earlier):

| Environment Variable       | Description                                                  |
| :------------------------- | :----------------------------------------------------------- |
| `GOOGLE_CLOUD_PROJECT`     | The full GCP project id that your application / Vertex AI model is running in |
| `VERTEX_AI_MODEL_ENDPOINT` | The ID of the Google Vertex AI model endpoint that predictions will be made against |
| `LOCATION`                 | The region to host your resources. Make sure to stick with the `us-east1` region, i.e. the same region of your bucket and model deployment |
| `GOOGLE_MAPS_API_KEY`      | The API key of the Google Maps Directions API                |



Finally, you should ensure that you have valid GCP credentials by running `gcloud auth application-default login` and make sure you are using a service account as described earlier.

To run the Flask application locally you may use python main.py as shown below:

```
$ python main.py
* Serving Flask app "main"
* Environment: production
WARNING: Do not use the development server in a production environment.
Use a production WSGI server instead.
* Debug mode: off
```

Test your application locally using `test_script.py`.

```
python test_script.py -v
# a sample test report if your application passed the first 6 test cases
# in this task, you only need to pass the first six test cases
# ......F
# ----------------------------------------------------------------------
# Ran 7 tests in 6.315s
```

You can also use the following command to run a specific test in `test_script.py` after you have implemented a single function:

```
python -m unittest test_script.TestAPIMethods.test_predict
```

You should pass the first 6 of the 7 test cases in this task. You can ignore the last test case as it is for the endpoint which will be implemented in the next task. After the functional test, you can deploy the application to GAE. Once you are confident about your implementation, you may deploy the application to the App Engine using the following command:

```
cd ~/ProjectMachineLearning/app-engine-pipeline
gcloud app deploy app.yaml
```

### Task submission

For this task, you will need to submit a completed `main.py` file (or equivalent App Engine application) and all associated files, including a `reference` file.

```
$ source /home/clouduser/virtualenv/bin/activate  # make sure that you activate the same virtualenv before you make submissions

$ export SUBMISSION_USERNAME=your_submission_username
$ export SUBMISSION_PASSWORD=your_submission_pwd

$ ./submitter -t task3
```

### Hints and warnings

1. If you find errors with your application after deploying it to the App Engine, utilize `gcloud app logs tail` to stream the logs. Alternatively, you may consult the [Stackdriver Logs](https://cloud.google.com/logging/docs/view/overview) or access the logs via the debug option on the [App Engine Dashboard](https://console.cloud.google.com/appengine).
2. In the case calling GCP or Google Maps APIs results in an unauthorized exception, ensure that you have enabled all of the required APIs in your project.
3. If you are unable to deploy to the App Engine, you may have exceeded the quota for the number of services. You can delete previous versions of the App Engine services and check your quotas in the [IAM & admin](https://console.cloud.google.com/iam-admin/quotas?usage=USED) console.
4. Test each API independently in order to pinpoint the logical errors in the application.
5. Write the WAV output generated by the APIs to a file. You can then open these files in a media play to verify if they are correctly formatted as WAV data.
6. Every API will return its output in a JSON object (i.e. `{"start_location": "lng": ..., "lat": ...}, "end_location": {"lng": ..., "lat": ...}}`).
7. WAV audio data should be sampled at 16 KHz. This encoding is referred to as `LINEAR16` in the GCP speech services.

Cloud Vision and Vertex AI AutoML Bonus

## Cloud Vision and Vertex AI AutoML

There are some cases in our real life scenarios that we may not know the exact name of the location, but we have a photo of it. At this time, we wish that we can simply upload the photo to the app, and the app can automatically recognize the location in the photo. In this task, you will be expanding your `farePrediction` API to support accepting images of landmarks as the trip pickup and drop off locations.

### Task to complete

In this bonus task, you have the opportunity to earn up to 10 bonus points. The first 5 points will be assigned based on the `farePredictionVision` API's response to common New York City landmarks. An additional 5 points will be assigned if your `farePredictionVision` implementation is able to correctly identify specific New York City restaurants and provide the correct trip fare.

It is possible to use GCP's Cloud Vision API to identify common NYC landmarks and their map coordinates. However, the default Cloud Vision API may not accurately identify specific NYC restaurants. For this use case, you will have to train a Vertex AI AutoML model that can be trained to identify new restaurants. To achieve the full 10 points it is suggested to start with Vertex AI AutoML or build a hybrid solution that queries both Cloud Vision and Vertex AI AutoML.



### GCP API to Enable

Ensure that the following API is enabled in your project to prevent exceptions when trying to call it from your application:

| API                                                          |
| :----------------------------------------------------------- |
| [Cloud Vision API](https://console.cloud.google.com/apis/library/vision.googleapis.com) |



To enable the API above, open your gcloud CLI or Cloud Shell, make sure you have set the correct project using `gcloud config set project ml-fare-prediction-xxxxxx`. Run the following command to enable APIs above:

```
gcloud services enable vision.googleapis.com
```

**The price of Vertex AI AutoML model training is $3.465 per node hour and the deployment for online prediction service is $1.375 per node hour after your free trial.** Please plan your budget very carefully. You can refer to [Pricing for AutoML models](https://cloud.google.com/vertex-ai/pricing#automl_models) for more details. Do not deploy the model if you do not plan to start the subsequent tasks right away, and remove the deployment as soon as you finish the task.

### Train a Vertex AI AutoML Model

In preparation for using Vertex AI AutoML, you may review the [AutoML beginner's guide](https://cloud.google.com/vertex-ai/docs/beginner/beginners-guide).

1. To get started, download the `restaurants_train_set.zip` dataset and extract it into the `/home/clouduser/ProjectMachineLearning` directory of your Student VM. You can use the following command for the same:

   ```
   # Enter ProjectMachineLearning folder
   cd ~/ProjectMachineLearning
   # Download and unzip dataset
   wget https://clouddeveloper.blob.core.windows.net/s21-cloud-developer/machine-learning/restaurants_train_set/restaurants_train_set.tgz && tar zxvf restaurants_train_set.tgz
   ```

2. You can use the following command to upload the images that you have downloaded to your GCP bucket:

   ```
   export BUCKET_ID=<your_bucket_name>
   gsutil -m cp -R restaurants_train_set gs://$BUCKET_ID
   ```

3. To create a dataset on the Vertex AI platform, an Import file is required in addition to your image dataset. This file is used for importing data for image classification with a single label into Vertex AI. It will create an 'import_file_restaurants.csv’ file which contains a path and label for each image in the training image set.

   To help implement this Import file, you can use the `create_import_file.py` script provided in `/home/clouduser/ProjectMachineLearning`. You will need to specify the name of your bucket by assigning it to the `BUCKET` variable in the `create_import_file.py` script. To execute the script and generate the csv file, use the following command:

   ```
   python create_import_file.py
   ```

4. Once the `import_file_restaurants.csv` is generated, you can upload it to your GCP bucket, so that Vertex AI can use the file to create image set with correct labels:

   ```
   gsutil cp import_file_restaurants.csv gs://$BUCKET_ID/
   ```

5. Open [Vertex AI Datasets](https://console.cloud.google.com/vertex-ai/datasets) and click `CREATE`.

6. Enter dataset name (i.e. restaurants), choose `Image classification (Single-label)` (**DO NOT** select `Multi-Label Classification`) and select Region as `us-central1 (Iowa)` (**DO NOT** select any other region). Finally, click on `CREATE`.

7. Choose "Select import files from Cloud Storage" option, select the uploaded `import_file_restaurants.csv` file from the GCP bucket, then click `CONTINUE` to initiate the import process, which may take a few minutes. You'll receive an email once the processing is done.

8. Once the import is finished, you will be able to browse and analyze both the images and their corresponding labels.

9. To configure the training process, click on the `TRAIN NEW MODEL` button. Choose `AutoML` as the model training method and select `Cloud` as the location to use the model.

10. Give a name to your model. Enter the maximum number of node hours as 8 for training which is cost efficient and sufficient for our project. Finally, click on `START TRAINING` to initiate the model training.

    Keep in mind that the training process may take a couple of hours to complete. Please manage your time effectively and patiently wait until the training is finished. You will receive an email once the training is finished.

11. Once the training is completed, you can locate the model in the [Vertex AI Model Registry](https://console.cloud.google.com/vertex-ai/models). Click on the name of the model you want to deploy to direct to its details page.

12. To deploy your model to an endpoint, navigate to the `Deploy & Test` tab and click on `Deploy to endpoint`. Then, choose `Create new endpoint` and assign a suitable name for the new endpoint. It is recommended to keep the `Number of compute nodes` as 1 in this project to save your budget.

13. After completing the previous step, you can start developing the Flask application to provide fare predictions based on the pickup and drop-off locations identified by landmarks in the input files.

You should ensure that these environment variables are set in your local environment for testing and in the App Engine environment for grading (by updating the `/home/clouduser/env_vars.yaml` file you created earlier).



| Environment Variable        | Description                                                  |
| :-------------------------- | :----------------------------------------------------------- |
| `GOOGLE_CLOUD_PROJECT`      | The full GCP project ID that your application / Google Vertex AI model is running in |
| `VERTEX_AI_MODEL_ENDPOINT`  | The ID of the Google Vertex AI model endpoint that predictions will be made against |
| `LOCATION`                  | The region to host your resources. Make sure you keep as `us-east1`, the same as the region of your bucket and model |
| `GOOGLE_MAPS_API_KEY`       | The API key of the Google Maps Directions API                |
| `AUTO_ML_MODEL_ENDPOINT_ID` | The ID for the Vertex AI Auto ML model that resturant image classification predictions will be made against |



The `AUTO_ML_MODEL_ENDPOINT_ID` can be obtained from the [Vertex AI Endpoints](https://console.cloud.google.com/vertex-ai/endpoints) as shown in the screenshot below:

![AutoML Model Endpoint ID](https://clouddeveloper.blob.core.windows.net/s23-cloud-developer/machine-learning-on-the-cloud/images/Extract_AUTO_ML_MODEL_ID.png)

**Figure 14**: AutoML Model Endpoint ID

### Implementation and Test

After trained your AutoML model, you can now implement `test_fare_prediction_vision()` stub in `main.py` according to the below `farePredictionVision` API definition:

| API                    | HTTP Method | Input                                                        | Output                                                       |
| :--------------------- | :---------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `farePredictionVision` | **POST**    | POST body contains base64 encoded image data for a source and destination of a taxi trip (you will have access to this data via `request.form` as it is sent as form data) **source** - Base64 encoded image data of a location to be used as the trip pickup location **destination** - Base64 encoded image data of a location to be used as the trip drop off location | **predicted_fare** - The numeric fare predicted by the model, to two decimal places **entities** - A list of image labels identified in the request **text** - The reply text of the fare request. In the format of "**Your expected fare from** X **to** Y **is $** Z" **speech** - Base64 speech of the response text field. (e.g. `{"predicted_fare": "23.78", "entities": ["Charging Bull", "Carnegie Hall"], "text": "Your expected fare from Charging Bull to Carnegie Hall is $23.78", "speech": <BASE64 ENCODED AUDIO>}`) |



The results you get from the client class are the labels we defined, but in the response we will need to return the full name of the landmarks. Therefore, don't forget to use the following map to convert the restaurant label into the full name.

```
label_mapping = {"Jing_Fong": "Jing Fong", "Bamonte": "Bamonte's", "Katz_Deli": "Katz's Delicatessen", "ACME": "ACMENYC"}
```



To run the Flask application locally you may use `python main.py` as shown below:

```
$ python main.py
 * Serving Flask app "main"
 * Environment: production
   WARNING: Do not use the development server in a production environment.
   Use a production WSGI server instead.
 * Debug mode: off
```

Test your application locally using `test_script.py`.

```
python test_script.py -v
# a sample test report if your application passed all test cases
# .......
# ----------------------------------------------------------------------
# Ran 7 tests in 6.315s
```

You need to pass all the 7 test cases in this task.

Once you are confident about your implementation, you may deploy the application to the App Engine using `gcloud app deploy app.yaml`.

### Task submission

For this task, you will need to submit a completed `main.py` file (or equivalent App Engine application) and all associated files, including a `reference` file.

```
$ source /home/clouduser/virtualenv/bin/activate  # make sure that you activate the same virtualenv before you make submissions

$ export SUBMISSION_USERNAME=your_submission_username
$ export SUBMISSION_PASSWORD=your_submission_pwd

$ ./submitter -t task4
```

### Hints

Note that, the entities in the response are not the same as the named entities in the previous task. You do NOT need to invoke Natural Language API to generate the entity list.



### Monitor Your Budget

Please monitor your budget closely. If you are leaving the project temporarily and will come back later, we recommend you to delete the AutoML deployment and stop the App Engine to save budget.