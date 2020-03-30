Containing customized networks. Folder names indicate network names which are tagged correspondingly with specs and scenarios.

Customized networks are provisioned before running the tests. Test reports are collected independently.

```
mvn clean verify \
    -Dnetworks=plugins -DTF_VAR_consensus=raft \
    -Dtags=plugin-security
```
