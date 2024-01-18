ls
cd azure-mysql
ls
terraform init
exit
ls
cd social_network_backend
mvn clean package exec:java -Dmaven.test.skip=true
ls
cd sn2
cd SN2
ls
npm strat
npm start
cd ..
mkdir azure-mysql && cd azure-mysql
wget https://clouddeveloper.blob.core.windows.net/assets/social-network/project/azure-mysql.tgz
tar -xvzf azure-mysql.tgz
terraform init
cd ..
export MYSQL_HOST=social-network-mysqlm0pbwuhn.mysql.database.azure.com
export MYSQL_NAME=clouduser@social-network-mysqlm0pbwuhn
export MYSQL_PWD=Jql1106750612@
mysql -h ${MYSQL_HOST} -u ${MYSQL_NAME} --password=${MYSQL_PWD}
cd social_network_backend
ls
wget https://clouddeveloper.blob.core.windows.net/datasets/social-network/project/users.csv
mysql -h ${MYSQL_HOST} -u ${MYSQL_NAME} --password=${MYSQL_PWD} --local-infile=1 < load_users.sql
mysql -h ${MYSQL_HOST} -u ${MYSQL_NAME} --password=${MYSQL_PWD}
ls
cd src
ls
cd main
ls
cd java
ls
cd edu
ls 
cd cmu
ls
