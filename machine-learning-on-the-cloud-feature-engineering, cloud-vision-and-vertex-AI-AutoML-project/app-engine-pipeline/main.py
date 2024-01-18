import json
import logging
import os

import requests
import pandas as pd
import math
import numpy as np
from flask import Flask, request, jsonify, Response
from clients.vertex_ai import VertexAIClient
import datetime
import time
import base64
from clients.google_maps import GoogleMapsClient
from clients.vertex_ai_auto_ml import VertexAIAutoMLClient
from clients.speech_to_text import SpeechToTextClient
from clients.text_to_speech import TextToSpeechClient
from clients.natural_language import NaturalLanguageClient
from clients.vertex_ai import VertexAIClient
from clients.cloud_vision import CloudVisionClient
from clients.predict_image_classification import predict_image_classification_with_encoded_content
from google.cloud import vision


app = Flask(__name__)

project_id = "ml-fare-prediction-1106"
endpoint_name = "7244418232625397760"
location = "us-east1"
google_maps_api_key = "AIzaSyBJFlFJUwl306c2qQjCeQ-3JI0qK7osW5U"
auto_ml_model_endpoint_id = "2089399747239477248"

vertex_ai_client = VertexAIClient(project_id, endpoint_name, location)

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

    raw_df = raw_df.drop(columns=['pickup_datetime'])

    # Calculate distance feature
    raw_df['haversine_distance'] = raw_df.apply(
        lambda row: haversine_distance((row['pickup_latitude'], row['pickup_longitude']),
                                       (row['dropoff_latitude'], row['dropoff_longitude'])), axis=1)

    return raw_df


@app.route('/')
def index():
    return "Hello"


@app.route('/predict', methods=['POST'])
def predict():
    raw_data_df = pd.read_json(request.data.decode('utf-8'),
                               convert_dates=["pickup_datetime"])
    predictors_df = process_test_data(raw_data_df)
    predictors_df = predictors_df.drop(['pickup_datetime'], axis=1, errors='ignore')
    for col in predictors_df.columns:
        predictors_df[col] = pd.to_numeric(predictors_df[col], errors='coerce')
    # return the predictions in the response in json format
    return json.dumps(vertex_ai_client.predict(predictors_df.values.tolist()))

@app.route('/farePrediction', methods=['POST'])
def fare_prediction():
    # Initialize Google Maps client
    google_maps_client = GoogleMapsClient()

    # Process the request based on speech
    # if len(request.form) != 2:
    # Extract text from speech
    audio_data = request.data
    stt_client = SpeechToTextClient()
    text = stt_client.recognize(base64.b64decode(audio_data))

    # Extract entities (origin, destination) from text
    nlp_client = NaturalLanguageClient()
    entities_res = nlp_client.analyze_entities(text)
    entities = [entity.name for entity in entities_res]
    # else:  # Assuming image input
    #     # Process image input and predict landmarks or stores
    #     input_data = request.form
    #     source_data, dest_data = input_data.get('source'), input_data.get('destination')
    #     automl_client = VertexAIAutoMLClient(project_id, auto_ml_model_endpoint_id)
    #     source_pred = automl_client.get_prediction(base64.b64decode(source_data))
    #     dest_pred = automl_client.get_prediction(base64.b64decode(dest_data))
    #     entities = [source_pred, dest_pred]

    # Get directions from Google Maps
    directions_result = google_maps_client.directions(entities[0], entities[1])
    start_loc = directions_result[0]['legs'][0]['start_location']
    end_loc = directions_result[0]['legs'][0]['end_location']

    # Prepare data for fare prediction
    fare_prediction_data = {
        "pickup_datetime": datetime.datetime.now().isoformat(),
        "pickup_longitude": start_loc['lng'],
        "pickup_latitude": start_loc['lat'],
        "dropoff_longitude": end_loc['lng'],
        "dropoff_latitude": end_loc['lat'],
        "passenger_count": 1
    }

    # Predict fare
    df = pd.DataFrame([fare_prediction_data])
    df['pickup_datetime'] = pd.to_datetime(df['pickup_datetime'])

    predictors_df = process_test_data(df)
    prediction = vertex_ai_client.predict(predictors_df.values.tolist())
    predicted_fare = round(prediction[0], 2)

    # Create response string and convert to speech
    response_string = f"Your expected fare from {entities[0]} to {entities[1]} is ${predicted_fare}"
    tts_client = TextToSpeechClient()
    speech_audio = tts_client.synthesize_speech(response_string)
    speech_base64 = base64.b64encode(speech_audio).decode('utf-8')

    # Return the response
    return jsonify({
        "predicted_fare": str(predicted_fare),
        "entities": entities,
        "text": response_string,
        "speech": speech_base64
    })


@app.route('/speechToText', methods=['POST'])
def speech_to_text():
    # Get the audio file from the POST request
    audio_data = request.data
    # Decode the audio data from base64 encoding to its original format
    audio_bytes = base64.b64decode(audio_data)
    
    # Process the audio data using SpeechToTextClient
    stt_client = SpeechToTextClient()
    text = stt_client.recognize(audio_bytes)

    # Return the recognized text
    return jsonify({'text': text})

@app.route('/textToSpeech', methods=['GET'])
def text_to_speech():
    # Retrieve the text from the GET request's query parameter
    text = request.args.get('text')
    
    # Process the text using TextToSpeechClient
    tts_client = TextToSpeechClient()
    speech_audio = tts_client.synthesize_speech(text)

    # Convert the audio data to base64 encoding
    speech_base64 = base64.b64encode(speech_audio).decode('utf-8')

    # Return the base64 encoded audio data
    return jsonify({'speech': speech_base64})

def get_landmark(client, encoded_image):
    # Decode the base64 image
    image_bytes = base64.b64decode(encoded_image)
    image = vision.Image(content=image_bytes)

    # Perform landmark detection
    response = client.landmark_detection(image=image)
    landmarks = response.landmark_annotations

    if landmarks:
        return landmarks[0].description
    else:
        return None

@app.route('/farePredictionVision', methods=['POST'])
def fare_prediction_vision():
    # Receive the source and destination images from the form data
    input_data = request.form
    source_image_data = input_data.get('source')
    destination_image_data = input_data.get('destination')

    if not source_image_data or not destination_image_data:
        return jsonify({'error': 'Missing image data'}), 400
    
    label_mapping = {
        "Jing_Fong": "Jing Fong",
        "Bamonte": "Bamonte's",
        "Katz_Deli": "Katz's Delicatessen",
        "ACME": "ACMENYC"
    }

    
    
    vision_client = vision.ImageAnnotatorClient()
    source_landmark = get_landmark(vision_client, source_image_data)
    destination_landmark = get_landmark(vision_client, destination_image_data)

    if source_landmark is None:
        source_prediction = predict_image_classification_with_encoded_content(
            encoded_content=source_image_data
        )
        source_landmark = label_mapping.get(source_prediction, source_prediction)
    else:
        source_prediction = source_landmark  # Set source_prediction to the found landmark
    
    if destination_landmark is None:
        destination_prediction = predict_image_classification_with_encoded_content(
            encoded_content=destination_image_data
        )
        destination_landmark = label_mapping.get(destination_prediction, destination_prediction)
    else:
        destination_prediction = destination_landmark  
    

    # Decode the base64 encoded images
    # source_image_bytes = base64.b64decode(source_image_data)
    # destination_image_bytes = base64.b64decode(destination_image_data)
    # print(f"Source image type: {type(source_image_bytes)}")
    # print(f"Destination image type: {type(destination_image_bytes)}")
    # print(f"Source image size: {len(source_image_bytes)}, first bytes: {source_image_bytes[:10]}")
    # print(f"Destination image size: {len(destination_image_bytes)}, first bytes: {destination_image_bytes[:10]}")

    # Initialize the AutoML client and the label mapping
    # automl_client = VertexAIAutoMLClient(project_id, auto_ml_model_endpoint_id)
    # label_mapping = {
    #     "Jing_Fong": "Jing Fong",
    #     "Bamonte": "Bamonte's",
    #     "Katz_Deli": "Katz's Delicatessen",
    #     "ACME": "ACMENYC"
    # }

    # Predict landmarks or labels for the images
    # source_prediction = automl_client.predict_image(source_image_data)
    # destination_prediction = automl_client.predict_image(destination_image_data)
    # Get predictions for the images
    # source_prediction = predict_image_classification_with_encoded_content(
    #     encoded_content=source_image_data
    # )
    # destination_prediction = predict_image_classification_with_encoded_content(
    #     encoded_content=destination_image_data
    # )

    # Map the predictions to full landmark names using label_mapping
    source_landmark = label_mapping.get(source_prediction, source_prediction)
    destination_landmark = label_mapping.get(destination_prediction, destination_prediction)

    # Get coordinates for the landmarks
    google_maps_client = GoogleMapsClient()
    start_location = google_maps_client.directions(source_landmark, destination_landmark)[0]['legs'][0]['start_location']
    end_location = google_maps_client.directions(source_landmark, destination_landmark)[0]['legs'][0]['end_location']

    # Prepare data for fare prediction
    fare_prediction_data = {
        "pickup_datetime": datetime.datetime.now().isoformat(),
        "pickup_longitude": start_location['lng'],
        "pickup_latitude": start_location['lat'],
        "dropoff_longitude": end_location['lng'],
        "dropoff_latitude": end_location['lat'],
        "passenger_count": 1
    }

    df = pd.DataFrame([fare_prediction_data])
    df['pickup_datetime'] = pd.to_datetime(df['pickup_datetime'])
    predictors_df = process_test_data(df)
    predictors_df = predictors_df.drop(['pickup_datetime'], axis=1, errors='ignore')


    for col in predictors_df.columns:
        predictors_df[col] = pd.to_numeric(predictors_df[col], errors='coerce')

    # Predict fare using Vertex AI client
    fare_prediction = vertex_ai_client.predict(predictors_df.values.tolist())

    fare_prediction = round(fare_prediction[0], 2)

    # Construct the response
    response_text = f"Your expected fare from {source_landmark} to {destination_landmark} is ${fare_prediction:.2f}"
    speech_client = TextToSpeechClient()
    response_speech = speech_client.synthesize_speech(response_text)
    # result = jsonify({
    #     "predicted_fare": f"{fare_prediction:.2f}",
    #     "entities": [source_landmark, destination_landmark],
    #     "text": response_text,
    #     "speech": base64.b64encode(response_speech).decode('utf-8')
    # })
    json_response = json.dumps({
        "predicted_fare": f"{fare_prediction:.2f}",
        "entities": [source_landmark, destination_landmark],
        "text": response_text,
        "speech": base64.b64encode(response_speech).decode('utf-8')
    })
    response = Response(json_response, content_type='application/json')

    return response

@app.route('/namedEntities', methods=['GET'])
def named_entities():
    # Retrieve the text from the GET request's query parameter
    text = request.args.get('text')

    # If no text is provided, return an error message
    if not text:
        return jsonify({'error': 'No text provided'}), 400

    # Process the text using NaturalLanguageClient to extract entities
    nlp_client = NaturalLanguageClient()
    entities_result = nlp_client.analyze_entities(text)

    # Extract entity names from the result
    entities = [entity.name for entity in entities_result]

    # Return the entities as a JSON response
    return jsonify({'entities': entities})


@app.route('/directions', methods=['GET'])
def directions():
    # Retrieve origin and destination from GET request's query parameters
    origin = request.args.get('origin')
    destination = request.args.get('destination')

    # Format the request URL for Google Maps Directions API
    url = f"https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&key={google_maps_api_key}"

    # Make the request to Google Maps Directions API
    response = requests.get(url)
    directions_data = response.json()

    # Extract start and end locations from the response
    if directions_data['status'] == 'OK':
        start_location = directions_data['routes'][0]['legs'][0]['start_location']
        end_location = directions_data['routes'][0]['legs'][0]['end_location']
        return jsonify({"start_location": start_location, "end_location": end_location})
    else:
        return jsonify({"error": "Unable to retrieve directions"}), 400



@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500


if __name__ == '__main__':
    app.run()
