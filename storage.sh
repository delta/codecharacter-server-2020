#!/bin/bash

if [ -d /tmp/codechar-storage ] ; then
    rm -rf /tmp/codechar-storage
fi

git clone git@github.com:delta/codecharacter-storage-2021.git /tmp/codechar-storage
rm -rf /tmp/codechar-storage/.git

if [ ! -d ./storage ] ; then
    mkdir ./storage
fi
cp -r /tmp/codechar-storage/storage/* ./storage