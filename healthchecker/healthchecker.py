import subprocess
import os
import json


#Execute in command line
def execute_command(command):
     proc = subprocess.Popen(command,shell=True,stdout=subprocess.PIPE)
     script_response = proc.stdout.read().split('\n')
     resp=json.loads(script_response[7])
     print resp[0]['name']
     print resp[0]['consumers']

if __name__ == '__main__':
  execute_command('curl -i -u guest:guest http://192.168.1.1:15672/api/queues/')