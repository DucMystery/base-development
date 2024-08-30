#!/bin/bash

# Define namespace and label selector
NAMESPACE="default"
LABEL_SELECTOR="app=my-app"

# Delete pods
kubectl delete pods -l ${LABEL_SELECTOR} -n ${NAMESPACE}