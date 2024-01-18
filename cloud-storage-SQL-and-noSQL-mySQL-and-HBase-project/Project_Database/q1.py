# You can solve the problem by filling in the correct method or parameters

import MySQLdb
import pandas as pd
import sys
import os

dbuser = os.getlogin()
passwd = os.environ.get('MYSQL_PWD')

conn = MySQLdb.connect(
      host="127.0.0.1",
      user=dbuser,
      password=passwd,
      database="yelp_db",
      port=3306,
  )
query = "SELECT review_count, stars FROM businesses"  # the SQL query
df = pd.read_sql(query, con=conn)
df.describe().to_csv(
    sys.stdout,
    encoding='utf-8',
    float_format='%.2f',
    header=False
)
conn.close()
