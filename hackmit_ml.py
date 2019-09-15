import json
import requests
from io import BytesIO
from ibm_watson import VisualRecognitionV3, ApiException
from PIL import Image, ExifTags


key='YrXg2RiYDQD1P0ilymV-DB3p4YxJp5RteJb9297w7UxN'
url='https://hackmit19.herokuapp.com/parse/files/abe/3f0b64478db2235082eef626d2dc7df2_photo.jpg'
endpoint='https://gateway.watsonplatform.net/visual-recognition/api'


visual_recognition = VisualRecognitionV3(
                                         version='2016-05-17',
                                         iam_apikey=key
                                         )
CROC_MAP=set(['lizard', 'crocodile', 'gator', 'croc', 'alligator', 'reptile', 'crocodiles', 'crocs', 'gators', 'lizard', 'reptile', 'gecko'])
FIRE_MAP=set(['smoke', 'fire', 'ash', 'burn', 'ember',])
FIRETRUCK_MAP=set(['firetruck', 'policecar', 'ambulance', 'siren', 'army'])
FLOOD_MAP=set(['flood', 'pool', 'drown'])
HOSPITAL_MAP=set(['hospital', 'bed', 'safe'])

def lambda_handler(event, context):
    # TODO implement
    url = event.url
    try:
        response = requests.get(url)
        img = Image.open(BytesIO(response.content))
        exif = { ExifTags.TAGS[k]: v for k, v in img._getexif().items() if k in ExifTags.TAGS}
        LAT=(exif['GPSInfo'][2][0][0]/exif['GPSInfo'][2][0][1]+exif['GPSInfo'][2][1][0]/(60*exif['GPSInfo'][2][1][1])+exif['GPSInfo'][2][2][0]/(3600*exif['GPSInfo'][2][2][1]))
        LONG=-(exif['GPSInfo'][4][0][0]/exif['GPSInfo'][4][0][1]+exif['GPSInfo'][4][1][0]/(60*exif['GPSInfo'][4][1][1])+exif['GPSInfo'][4][2][0]/(3600*exif['GPSInfo'][4][2][1]))
    except:
        LAT=0.0
        LONG=0.0
    
    
    try:
        results=visual_recognition.classify(images_file=None, images_filename=None, images_file_content_type=None, url=url, threshold=None, owners=None, classifier_ids=None, accept_language=None)
    except ApiException as ex:
        set_of_classes=set()

    set_of_classes=set([i['class'] for i in results.get_result()['images'][0]['classifiers'][0]['classes']])


    tag=None
    for classification in set_of_classes:
        if classification in CROC_MAP:
            tag="crocodile"
            break
        elif classification in FIRE_MAP:
            tag="fire"
            break
        elif classification in FIRETRUCK_MAP:
            tag="firetruck"
            break
        elif classification in FLOOD_MAP:
            tag="flood"
            break
        elif classification in HOSPITAL_MAP:
            tag="hospital"
            break

    return {
        'statusCode': 200,
            'body': json.dumps({'tag':tag, 'lat':LAT, 'long':LONG})
    }






