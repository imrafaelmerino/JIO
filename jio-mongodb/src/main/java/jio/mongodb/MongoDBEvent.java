package jio.mongodb;

import jdk.jfr.*;

@Label("jio-mongodb-op")
@Name("jio.mongodb")
@Category({"JIO", "DATABASE", "MONGODB"})
@Description("MongoDB CRUD operations like find, replace, delete, insert...")
final class MongoDBEvent extends Event {
    @Label("operation")
    public final String operation;
    @Label("result")
    public String result;
    @Label("exception")
    public String exception;

    public MongoDBEvent(OP operation) {
        this.operation = operation.name();
    }

    enum OP {
        AGGREGATE,
        COUNT,
        DELETE_MANY,
        DELETE_ONE,
        FIND,
        FIND_ONE_AND_DELETE,
        FIND_ONE_AND_REPLACE,
        FIND_ONE_AND_UPDATE,
        INSERT_MANY,
        INSERT_ONE,
        REPLACE_ONE,
        UPDATE_MANY,
        UPDATE_ONE
    }

    enum RESULT {SUCCESS, FAILURE}


}
