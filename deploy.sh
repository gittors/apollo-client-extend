#!/bin/sh

echo "============================================================="
echo "$                                                           $"
echo "$         Apollo Client Extend                              $"
echo "$                                                           $"
echo "$                                                           $"
echo "$                                                           $"
echo "$  gittors All Right Reserved                               $"
echo "$  Copyright (C) 2020                                       $"
echo "$                                                           $"
echo "============================================================="

echo "Apollo Client Extend"

mvn clean deploy \
-DskipTests -e -P release \
-pl \
apollo-client-extend-starter-gateway-adapter,\
apollo-client-extend-binder-starter,\
apollo-client-extend-admin/apollo-client-extend-admin-web,\
apollo-client-extend-admin/apollo-client-extend-admin-webflux,\
apollo-client-extend-support,\
apollo-client-extend-adapter/apollo-client-extend-higher-adapter,\
apollo-client-extend-adapter/apollo-client-extend-lower-adapter \
-am