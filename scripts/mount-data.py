import ConfigParser 
from slipstream.api import Api 

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

for so in service_offers:
  so_doc = api.cimi_get(so)
  so_bucket = so_doc.json['resource:bucket']
  so_object = so_doc.json['resource:object']
  print(so_bucket)
  print(so_object)

print(credential)
print(object_store_endpoint)
print(credential_key)
print(credential_secret)

passwd_file = '/tmp/slipstream/passwd-s3fs'
passwd = '{}:{}'.format(credential_key, credential_secret)
print(passwd)

cmd = 's3fs {0} /mnt/buckets/{0} -o passwd_file={1} -o url={2} -o use_path_request_style'.format(so_bucket, passwd_file, object_store_endpoint)
print(cmd)

