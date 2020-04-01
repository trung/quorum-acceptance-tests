# Pluggable Architecture with hello-world plugin

 Tags: networks/plugins, plugin-hello-world

Plugin hello world exposes an API which is delegated via JSON RPC

## `plugin@helloworld_greeting` API is successfully invoked in all nodes

* Calling `plugin@helloworld_greeting` API in "Node1" with single parameter "ACCTEST Node1" must return "Hello ACCTEST Node1!"
* Calling `plugin@helloworld_greeting` API in "Node2" with single parameter "ACCTEST Node2" must return "Hello ACCTEST Node2!"
* Calling `plugin@helloworld_greeting` API in "Node3" with single parameter "ACCTEST Node3" must return "Hello ACCTEST Node3!"
* Calling `plugin@helloworld_greeting` API in "Node4" with single parameter "ACCTEST Node4" must return "Hello ACCTEST Node4!"