#! /bin/bash

mongod --fork --logpath /var/log/mongodb.log && npm start
