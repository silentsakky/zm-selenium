#!/bin/bash

set -x
set -e
set -o pipefail

export DEBIAN_FRONTEND=noninteractive

# SYSTEM
apt-get -qq update

# JOB
apt-get -qq install -y git
apt-get -qq install -y wget
apt-get -qq install -y openjdk-8-jdk ant ant-optional ant-contrib

mkdir -p ~/.zcs-deps
mkdir -p ~/.ivy2/cache

cd ~/.zcs-deps
wget https://files.zimbra.com/repository/ant-contrib/ant-contrib-1.0b1.jar