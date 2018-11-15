#!/usr/bin/env python

import json
import re
import requests
import string
from datetime import timedelta, datetime
from random import randint, choice
from slipstream.api import Api

api = Api()

data_formats = ["feCapture", "ionMessage", "cotsData", "sdrData"]

service_offer_template = {"resourceURI": "http://sixsq.com/slipstream/1/ServiceOffer",
                          "acl": {"owner": {"principal": "ADMIN",
                                            "type": "ROLE"},
                                  "rules": [{"principal": "USER",
                                             "right": "VIEW", "type": "ROLE"}]},

                          "connector": {"href": "..."},

                          "description": "...",
                          "name": "...",

                          "gnss:bits": 8,
                          "gnss:chain": 1,
                          "gnss:hgt": 373.0,
                          "gnss:lat": 46.2044,
                          "gnss:lon": 6.1432,

                          "gnss:timestamp": "19721008T102530Z",
                          "gnss:type": "feCapture",
                          "gnss:unit_id": "prototype",

                          "resource:bucket": "...",
                          "resource:object": "...",
                          "resource:protocol": "S3",
                          "resource:type": "DATA",

                          "data:location": "...",
                          "data:contentType": "...",
                          "data:bytes": 123,
                          "data:timestamp": "...",
                          "data:protocols": ["http+s3"]
                          }

t_start = datetime(2018, 11, 1)

credentials = {"connector/exoscale-at-vie": {"href": "credential/e9ecf035-c997-43ac-8bc7-fa872e0e9f88"},
               "connector/exoscale-ch-gva": {"href": "credential/ecbd467b-0249-4093-b708-790024f21bc5"}}

clouds = {"exoscale-ch-gva": {"gnss:hgt": 373.0,
                              "gnss:lat": 46.204391,
                              "gnss:lon": 6.143158,
                              "data:location": "46.204391, 6.143158"},
          "exoscale-at-vie": {"gnss:hgt": 188.0,
                              "gnss:lat": 48.210033,
                              "gnss:lon": 16.363449,
                              "data:location": "48.210033, 16.363449"}}


def generate_service_offer(tmpl, i):
    cloud_name = clouds.keys()[randint(0, len(clouds) - 1)]
    gnss_info = clouds[cloud_name]
    timestamp = (t_start + timedelta(seconds=i)).isoformat() + 'Z'
    timestamp_gnss = re.sub('[^a-zA-Z0-9]', '',
                            (t_start + timedelta(hours=i / 3600)).isoformat('t') + 'z')

    rand_type = data_formats[randint(0, len(data_formats) - 1)]
    content_type = "application/x-{}".format(rand_type)

    prefix = ''.join(choice(string.ascii_lowercase) for _ in range(8))
    bucket_name = "gnss-%s-%s" % (cloud_name, timestamp_gnss)
    object_name = "%s_%s_%s" % (prefix, rand_type, timestamp)
    name = object_name
    description = bucket_name + "/" + object_name + "    " + content_type

    gnss_info.update({"name": name,
                      "description": description,
                      "connector": {"href": ("connector/" + cloud_name)},
                      "gnss:type": rand_type,
                      "gnss:timestamp": timestamp_gnss,
                      "resource:bucket": bucket_name,
                      "resource:object": object_name,
                      "data:contentType": content_type,
                      "data:bytes": randint(1024, 4096),
                      "data:timestamp": timestamp})
    tmpl.update(gnss_info)
    return tmpl


def create_and_fill_external_object(service_offer):
    name = service_offer["name"]
    description = service_offer["description"]
    bucket_name = service_offer["resource:bucket"]
    content_type = service_offer["data:contentType"]
    object_name = service_offer["resource:object"]
    bytes = service_offer["data:bytes"]
    connector = service_offer["connector"]["href"]
    credential = credentials[connector]

    resource_id = _create_external_object(name, description, bucket_name, content_type, object_name, credential)
    upload_url = _generate_upload_url_external_object(resource_id)
    _upload_data(upload_url, content_type, bytes)
    _set_ready(resource_id)


def _create_external_object(name, description, bucket_name, content_type, object_name, credential):
    resp = api.cimi_add('externalObjects',
                        {'name': name,
                         'description': description,
                         'externalObjectTemplate': {'href': 'external-object-template/generic',
                                                    'bucketName': bucket_name,
                                                    'contentType': content_type,
                                                    'objectName': object_name,
                                                    'objectStoreCred': credential}})
    return resp.json['resource-id']


def _set_ready(resource_id):
    resp = api.cimi_operation(resource_id, "http://sixsq.com/slipstream/1/action/ready")


def _generate_upload_url_external_object(resource_id):
    resp = api.cimi_operation(resource_id, "http://sixsq.com/slipstream/1/action/upload")
    return resp.json['uri']


def _upload_data(url, content_type, bytes):
    body = ''.join(choice(string.ascii_lowercase) for _ in range(bytes))
    headers = {"Content-Type": content_type}
    resp = requests.put(url, data=body, headers=headers)


seconds = 5 * 24 * 3600  # 5 days in seconds
step_sec = seconds + 1000 # 15 * 60  # every 15 minutes

for i in xrange(0, seconds, step_sec):
    service_offer = generate_service_offer(service_offer_template, i)
    create_and_fill_external_object(service_offer)
    api.cimi_add("serviceOffers", service_offer)
    #print(json.dumps(service_offer, indent=2))
