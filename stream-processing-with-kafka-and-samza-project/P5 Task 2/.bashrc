# Source global definitions
if [ -f /etc/bashrc ]; then
  . /etc/bashrc
fi

# set the default region for the AWS CLI
export AWS_DEFAULT_REGION=$(curl --retry 5 --silent --connect-timeout 2 http://169.254.169.254/latest/dynamic/instance-identity/document | grep region | awk -F\" '{print $4}')
export JAVA_HOME=/etc/alternatives/jre
export HOME=/home/hadoop
 
export HADOOP_YARN_HOME=/usr/lib/hadoop-yarn
export PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/opt/aws/bin:/home/hadoop/apache-maven-3.8.8/bin
export PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/opt/aws/bin:/home/hadoop/apache-maven-3.8.8/bin:/home/hadoop/hello-samza/deploy/kafka/bin
