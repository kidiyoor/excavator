import json
import requests

numberOfServers = 0

class Balancer:
	def __init__(self, serverAddressFile = 'servers.conf'):
		self.serverIndex = 0
		self.serverAddresses = []
		with open(serverAddressFile) as addressFile:
			for line in addressFile.read().split('\n'):
				if(line != '' and line[0] != '#' and ':' in line):
					self.serverAddresses.append('http://' + line.strip() + '/scrape')
		self.numberOfServers = len(self.serverAddresses)

	def getServer(self):
		currentServer = self.serverAddresses[self.serverIndex]
		self.serverIndex = (self.serverIndex + 1) % self.numberOfServers
		return currentServer

	def request(self, url, columns, rules):
		for i in range(self.numberOfServers):
			payload = {'columns': '*,'.join(columns), 'rules': '*,'.join(rules), 'url':url}
			try:
				r = requests.get(self.getServer(), params = payload)
				return json.loads(r.text)
			except:
				pass
		return {}
