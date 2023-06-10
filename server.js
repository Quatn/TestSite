const http = require('http')
const fs = require('fs')
const express = require('express')
const app = express()
const path = require('path')
const port = 5000

app.use(express.static(__dirname + '/public'))
console.log(__dirname)
app.get('/', function(req, res) {
	res.sendFile(path.join(__dirname + '/index.html'))
	res.sendFile(path.join(__dirname + '/pyro.png'))
} )
app.listen(port)
