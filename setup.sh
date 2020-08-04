#!/bin/env sh

curl -v --request PUT --data '50' http://localhost:8500/v1/kv/is/rate/limit/rps
curl -v --request PUT --data '50' http://localhost:8500/v1/kv/sus/rate/limit/rps
curl -v --request PUT --data '50' http://localhost:8500/v1/kv/rate_limit
