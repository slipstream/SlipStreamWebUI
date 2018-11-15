#!/usr/bin/env python

from slipstream.api import Api
from random import random, randint, choice
from datetime import timedelta, datetime
import json
import requests
import string
import re

api = Api()


types = ["feCapture", "ionMessage", "cotsData", "sdrData"]
cloud = "exoscale-ch-gva"

so_tmpl = {"resourceURI": "http://sixsq.com/slipstream/1/ServiceOffer",
           "acl": {"owner": {"principal": "ADMIN",
                             "type": "ROLE"},
                   "rules": [{"principal": "USER",
                              "right": "VIEW", "type": "ROLE"}]},

           "connector": {"href": "..."},

           "description": "GNSS-TEST",
           "name": "GNSS",

           "gnss:bits": 8,
           "gnss:chain": 1,
           "gnss:hgt": 373.0,
           "gnss:lat": 46.2044,
           "gnss:lon": 6.1432,

           "gnss:timestamp": "19721008T102530Z",
           "gnss:type": "feCapture",
           "gnss:unit_id": "prototype",

           "resource:bucket": "gnss-nuvlabox-foo-bar-20181108t150000z",
           "resource:object": "if_raw_19721008T102530Z",
           "resource:objectStoreEndpoint": "...",
           "resource:protocol": "S3",
           "resource:type": "DATA",

           "data:location": "46.2044, 6.1432",
           "data:contentType": "...",
           "data:bytes": 123,
           "data:timestamp": "...",
           "data:protocols": ["http+s3"]
           }

t_start = datetime(2018, 11, 1)


credentials = {"exoscale-at-vie": "credential/e9ecf035-c997-43ac-8bc7-fa872e0e9f88",
               "exoscale-ch-gva": "credential/ecbd467b-0249-4093-b708-790024f21bc5"}

clouds = {"exoscale-ch-gva": {"gnss:hgt": 373.0,
                              "gnss:lat": 46.204391,
                              "gnss:lon": 6.143158,
                              "data:location": "46.204391, 6.143158",
                              "resource:objectStoreEndpoint": "https://sos-ch-dk-2.exo.io"},
          "exoscale-at-vie": {"gnss:hgt": 188.0,
                              "gnss:lat": 48.210033,
                              "gnss:lon": 16.363449,
                              "data:location": "48.210033, 16.363449",
                              "resource:objectStoreEndpoint": "https://sos-at-vie-1.exo.io"}}

def gen_gnss_so(tmpl, i):
    cloud_name = clouds.keys()[randint(0, len(cloud_names)-1)]
    gnss = clouds[cloud_name]
    timestamp = (t_start + timedelta(seconds=i)).isoformat() + 'Z'
    timestamp_gnss = re.sub('[^a-zA-Z0-9]', '',
                            (t_start + timedelta(hours=i/3600)).isoformat('t') + 'Z'
    bucket_name = "gnss-%s-%s" % (cloud_name, timestamp_gnss)
    rand_type = types[randint(0, len(types)-1)]
    content_type = "application/x-{}".format(rand_type)
    prefix = ''.join(choice(string.ascii_lowercase) for _ in range(8))

    gnss.update({"gnss:type": rand_type,
                 "connector": {"href": choice([cloud_name, cloud])},
                 "gnss:timestamp": timestamp_gnss,
                 "resource:bucket": bucket_name,
                 "resource:object": "%s_%s_%s" % (prefix, rand_type, timestamp),
                 "data:contentType": content_type,
                 "data:bytes": 1024,
                 "data:timestamp": timestamp
                 })
    tmpl.update(gnss)
    return tmpl


def create_and_fill_external_object(so):
    bucket_name = so["resource:bucket"]
    content_type = so["data:contentType"]
    object_name = so["resource:object"]
    credential = {"href": "credential/ecbd467b-0249-4093-b708-790024f21bc5"}
    
    resource_id = _create_external_object(bucket_name, content_type, object_name, credential)
    upload_url = _generate_upload_url_external_object(resource_id)
    _upload_data(upload_url, content_type)
    _set_ready(resource_id)

def _create_external_object(bucket_name, content_type, object_name, credential):
    resp = api.cimi_add('externalObjects',
                             {'externalObjectTemplate': {'href': 'external-object-template/generic',
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

def _upload_data(url, content_type):
    print('Uploading data to: %s' % url)
    body = ''.join(choice(string.ascii_lowercase) for _ in range(1024))
    headers = {"Content-Type" : content_type}
    resp = requests.put(url, data=body, headers=headers)


seconds = 7 * 24 * 3600  ## 7 days in seconds
step_sec = seconds + 1000## 3600 ## every hour

for i in xrange(0, seconds, step_sec):
    so = gen_gnss_so(so_tmpl, i)
    create_and_fill_external_object(so)
    api.cimi_add("serviceOffers", so)
    #print(json.dumps(gen_gnss_so(so_tmpl, i)))
