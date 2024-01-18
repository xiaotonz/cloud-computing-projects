import os
from google.cloud import aiplatform
from google.cloud.aiplatform.gapic.schema import predict

def predict_image_classification_with_encoded_content(
    encoded_content: str,
    project: str = "ml-fare-prediction-1106",
    endpoint_id: str = "2089399747239477248",
    location: str = "us-central1",
    api_endpoint: str = "us-central1-aiplatform.googleapis.com",
):
    # The AI Platform services require regional API endpoints.
    client_options = {"api_endpoint": api_endpoint}
    client = aiplatform.gapic.PredictionServiceClient(client_options=client_options)

    instance = predict.instance.ImageClassificationPredictionInstance(
        content=encoded_content,
    ).to_value()
    instances = [instance]

    parameters = predict.params.ImageClassificationPredictionParams(
        confidence_threshold=0.5,
        max_predictions=5,
    ).to_value()
    endpoint = client.endpoint_path(
        project=project, location=location, endpoint=endpoint_id
    )
    response = client.predict(
        endpoint=endpoint, instances=instances, parameters=parameters
    )

    predictions = response.predictions
    if predictions and len(predictions) > 0 and 'displayNames' in predictions[0]:
        return predictions[0]['displayNames'][0]
    else:
        return None