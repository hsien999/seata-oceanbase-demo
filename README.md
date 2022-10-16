# Seata OceanBase Demo

> A simple integration tests of Seata support for OceanBase database

## Test

Services: euraka(registry), order, storage

Others: Seata server, DB server

Note: For convenience, instead of using Nacos or Dubbo as service registries and Seata TC as a database to store global
and branch state, we use eureka and file

### Build Seata

1. clone the [branch](https://github.com/hsien999/seata/tree/feature_support_oceanbase) for OceanBase supporting
2. config server
   in [application.yml](https://github.com/hsien999/seata/blob/feature_support_oceanbase/server/src/main/resources/application.yml)

```yaml
seata:
  registry:
    type: eureka
    eureka:
      serviceUrl: http://localhost:8761/eureka/
      application: default
      weight: 1
```

3. install and run seata-server(io.seata.server.ServerApplication)

### InitialSize DB

1. initialize seata client data by [DDL Script](scripts/seata-client)
2. initialize business data by [DDL Script](scripts/business)

### API tests

1. test [mysql mode](api-tests/mysql-requests.http)
2. test [oracle mode](api-tests/oracle-requests.http)