#!/bin/bash
###### TEMPLATE run.sh ######
###### YOU NEED TO UNCOMMENT THE FOLLOWING LINE AND INSERT YOUR OWN PARAMETERS ######

spark-submit --class PageRank \
  --master yarn \
  --executor-memory 20G \
  --executor-cores 3 \
  --num-executors 5 \
  --driver-memory 5G \
  --conf spark.default.parallelism=45 \
  --conf spark.yarn.am.memory=5g \
  target/project_spark.jar \
  wasb://datasets@clouddeveloper.blob.core.windows.net/iterative-processing/Graph \
  wasbs:///pagerank-output
  
# For more information about tuning the Spark configurations, refer: https://spark.apache.org/docs/2.3.0/configuration.html
