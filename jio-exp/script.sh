java -javaagent:agent/target/type-pollution-agent-0.1-SNAPSHOT.jar=io.other -jar example/target/type-pollution-example-0.1-SNAPSHOT.jar

# IT WOULD PRINT...NOTHING! No packages starting with io.other exists :P