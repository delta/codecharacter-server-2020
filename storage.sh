#!/bin/bash

git clone git@github.com:delta/codecharacter-storage-2021.git /tmp/codechar-storage
rm -rf /tmp/codechar-storage/.git
cp -r /tmp/codechar-storage/* ./storage