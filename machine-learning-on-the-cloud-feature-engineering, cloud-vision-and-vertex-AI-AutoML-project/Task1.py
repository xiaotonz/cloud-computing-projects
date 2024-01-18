#!/usr/bin/env python
# coding: utf-8

# In[2]:


# Imports
import pandas as pd
import math
import numpy as np
from sklearn.model_selection import cross_val_score
from xgboost.sklearn import XGBRegressor
import xgboost as xgb


# In[3]:


def haversine_distance(origin, destination):
    """
    # Formula to calculate the spherical distance between 2 coordinates, with each specified as a (lat, lng) tuple

    :param origin: (lat, lng)
    :type origin: tuple
    :param destination: (lat, lng)
    :type destination: tuple
    :return: haversine distance
    :rtype: float
    """
    lat1, lon1 = origin
    lat2, lon2 = destination
    radius = 6371  # km

    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = math.sin(dlat / 2) * math.sin(dlat / 2) + math.cos(math.radians(lat1)) * math.cos(
        math.radians(lat2)) * math.sin(dlon / 2) * math.sin(dlon / 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    d = radius * c

    return d


# In[14]:


def process_train_data(raw_df):
    """
    TODO: Implement this method.
    
    You may drop rows if needed.

    :param raw_df: the DataFrame of the raw training data
    :return:  a DataFrame with the predictors created
    """
    # Create a copy of the DataFrame
    raw_df = raw_df.copy()
    
    # Fill missing values
    raw_df.fillna(method='ffill', inplace=True)
    raw_df.fillna(method='bfill', inplace=True)

    mask = (raw_df['pickup_latitude'] >= 40.49) & (raw_df['pickup_latitude'] <= 40.92) & (raw_df['pickup_longitude'] >= -74.25) & (raw_df['pickup_longitude'] <= -73.7)
    raw_df = raw_df.loc[mask]

    # Calculate high and low fare thresholds
    fare_h = raw_df['fare_amount'].quantile(0.999)
    fare_l = raw_df['fare_amount'].quantile(0.001)

    # Drop rows where fare amount is outside the high and low thresholds
    raw_df = raw_df[(raw_df['fare_amount'] > fare_l) & (raw_df['fare_amount'] < fare_h)]

    # Extract time-related features from 'pickup_datetime'
    raw_df['year'] = raw_df['pickup_datetime'].dt.year
    raw_df['month'] = raw_df['pickup_datetime'].dt.month
    raw_df['day'] = raw_df['pickup_datetime'].dt.day
    raw_df['hour'] = raw_df['pickup_datetime'].dt.hour
    raw_df['weekday'] = raw_df['pickup_datetime'].dt.weekday

    # Calculate distance feature
    raw_df['haversine_distance'] = raw_df.apply(
        lambda row: haversine_distance((row['pickup_latitude'], row['pickup_longitude']),
                                       (row['dropoff_latitude'], row['dropoff_longitude'])), axis=1)
    
    return raw_df


def process_test_data(raw_df):
    """
    TODO: Implement this method.
    
    You should NOT drop any rows.

    :param raw_df: the DataFrame of the raw test data
    :return: a DataFrame with the predictors created
    """
    # Create a copy of the DataFrame
    raw_df = raw_df.copy()
    
    raw_df.fillna(method='ffill', inplace=True)
    raw_df.fillna(method='bfill', inplace=True)

    # Extract time-related features from 'pickup_datetime'
    raw_df['year'] = raw_df['pickup_datetime'].dt.year
    raw_df['month'] = raw_df['pickup_datetime'].dt.month
    raw_df['day'] = raw_df['pickup_datetime'].dt.day
    raw_df['hour'] = raw_df['pickup_datetime'].dt.hour
    raw_df['weekday'] = raw_df['pickup_datetime'].dt.weekday

    # Calculate distance feature
    raw_df['haversine_distance'] = raw_df.apply(
        lambda row: haversine_distance((row['pickup_latitude'], row['pickup_longitude']),
                                       (row['dropoff_latitude'], row['dropoff_longitude'])), axis=1)

    return raw_df


# In[15]:


# Load data
raw_train = pd.read_csv('data/cc_nyc_fare_train_small.csv', parse_dates=['pickup_datetime'])
print('Shape of the raw data: {}'.format(raw_train.shape))
raw_train.head(5)


# In[16]:


# Transform features using the function you have defined
df_train = process_train_data(raw_train)

# Remove fields that we do not want to train with
X = df_train.drop(['key', 'fare_amount', 'pickup_datetime'], axis=1, errors='ignore')

# Extract the value you want to predict
Y = df_train['fare_amount']
print('Shape of the feature matrix: {}'.format(X.shape))


# In[13]:


# Build final model with the entire training set
final_model = XGBRegressor(objective ='reg:squarederror')
final_model.fit(X, Y)

# Read and transform test set
raw_test = pd.read_csv('data/cc_nyc_fare_test.csv', parse_dates=['pickup_datetime'])
df_test = process_test_data(raw_test)
X_test = df_test.drop(['key', 'pickup_datetime'], axis=1, errors='ignore')

# Make predictions for test set and output a csv file
# DO NOT change the column names
df_test['predicted_fare_amount'] = final_model.predict(X_test)
df_test[['key', 'predicted_fare_amount']].to_csv('predictions.csv', index=False)


# In[ ]:




