#!/usr/bin/python

import ConfigParser
import sys
sys.path.insert(0, "/opt/slipstream/client/lib/")
from slipstream.api import Api
import os

config = ConfigParser.RawConfigParser()
config.read('/opt/slipstream/client/bin/slipstream.context')

api = Api(endpoint=config.get('contextualization', 'serviceurl'))
api.login_apikey(config.get('contextualization', 'api_key'),
                 config.get('contextualization', 'api_secret'))
deployment_id = config.get('contextualization', 'diid')
deployment = api.cimi_get(deployment_id)


depl_params = api.cimi_search('deploymentParameters', filter="deployment/href='{}' and name='{}'".format(deployment_id, 'credential.id'))

credential_id = depl_params.resources_list[0].json['value']

credential = api.cimi_get(credential_id)
credential_key = credential.key
credential_secret = credential.secret
connector_ref = credential.json['connector']['href']
connector = api.cimi_get(connector_ref)
object_store_endpoint = connector.json['objectStoreEndpoint']

service_offers = deployment.json['serviceOffers']

tmp_path = '/tmp/slipstream/'
if not os.path.exists(tmp_path):
  os.makedirs(tmp_path)
passwd_file_path = tmp_path + 'passwd-s3fs'
passwd = '{}:{}'.format(credential_key, credential_secret)
passwd_file = open(passwd_file_path, 'w+')
passwd_file.write(passwd)
passwd_file.close()
os.chmod(passwd_file_path, 0600)

buckets_base_path = '/mnt/buckets/'

for so in service_offers:
  so_doc = api.cimi_get(so)
  so_bucket = so_doc.json['resource:bucket']
  so_object = so_doc.json['resource:object']

  bucket_mount_point = buckets_base_path + so_bucket
  if not os.path.exists(bucket_mount_point):
    os.makedirs(bucket_mount_point)
  cmd = 's3fs {0} {1} -o passwd_file={2} -o url={3} -o use_path_request_style'.format(so_bucket, bucket_mount_point, passwd_file_path, object_store_endpoint)
  os.system(cmd)
