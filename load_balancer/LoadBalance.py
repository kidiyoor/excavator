import json
import requests

class Balancer:
	serverIndex = 0
	def __init__(self, serverAddressFile = 'servers.conf'):
		self.serverAddresses = []
		with open(serverAddressFile) as addressFile:
			for line in addressFile.read().split('\n'):
				if(line != '' and line[0] != '#' and ':' in line):
					self.serverAddresses.append('http://' + line.strip() + '/scrape')
		self.numberOfServers = len(self.serverAddresses)

	def request(self, url, columns, rules):
		for i in range(self.numberOfServers):
			currentServer = self.serverAddresses[serverIndex]
			serverIndex = (serverIndex + 1) % self.numberOfServers
			payload = {'columns': '*,'.join(columns), 'rules': '*,'.join(rules), 'url':url}
			try:
				r = requests.get(currentServer, params = payload)
				return json.loads(r.text)
				# return r.text
			except:
				pass
		return {}
		# return {'status' : 'ERROR', 'reason' : 'SERVER_TIMEOUT'}


