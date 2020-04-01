#!/bin/bash

set -e

echo "Waiting 10s ..." && sleep 10
all_up="false"
while [ "$all_up" == "false" ]; do
  all_up="true"
  for c in `docker ps -a --format "{{ .Names }}"`; do
    s=$(docker inspect -f "{{ .State.Status }}" $c)
    h=$(docker inspect -f "{{ .State.Health.Status }}" $c)
    echo "   -> $c (status=$s, health=$h)"
    if [ "$h" == "starting" ]; then
      all_up="false"
    fi
  done
  if [ "$all_up" == "true" ]; then
    break
  fi
  echo "Waiting 3s ..." && sleep 3
done
echo "" && docker ps -a && echo ""
unhealthy_containers=$(docker ps -a --format "{{ .Names }},{{ .Status }}" | grep -v healthy || echo "")
if [ "$unhealthy_containers" != "" ]; then
  echo "Unhealthy containers: $unhealthy_containers"
  SAVEIFS=$IFS                  # Save current IFS
  IFS=$'\n'                     # Change IFS to new line
  names=($unhealthy_containers) # split to array $names
  IFS=$SAVEIFS                  # Restore IFS
  for (( i=0; i<${#names[@]}; i++ ))
  do
    c=${names[$i]%%,*} # extract the container names only
    echo "> Logs for container $c"
    docker logs $c
  done
  exit 1
fi
