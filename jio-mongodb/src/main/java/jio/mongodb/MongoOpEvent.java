package jio.mongodb;

import jdk.jfr.*;

@Label("MongoDB Operation")
@Name("jio.mongodb.Op")
@Category({"JIO", "DATABASE", "MONGODB"})
@Description("MongoDB operations like find, replace, delete, insert, performed by jio-mongodb")
final class MongoOpEvent extends Event {

  static final String OPERATION_FIELD = "operation";
  static final String RESULT_FIELD = "result";
  static final String EXCEPTION_FIELD = "exception";

  public final String operation;

  public String result;
  public String exception;


  public MongoOpEvent(OP operation) {
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
    UPDATE_ONE,
    TX
  }

  enum RESULT {SUCCESS, FAILURE}


}
