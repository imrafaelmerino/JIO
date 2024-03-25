package jio.test.junit;

class EventFields {

  public static final String LABEL = "label";
  public static final String FETCH_SIZE = "fetchSize";

  private EventFields() {
  }

  static final String EXCEPTION = "exception";
  static final String RESULT = "result";
  static final String SQL = "sql";
  static final String ROWS_AFFECTED = "rowsAffected";

  static final String ROW_RETURNED = "rowsReturned";

  static final String EXPRESSION = "expression";
  static final String VALUE = "value";
  static final String CONTEXT = "context";

  static final String STATUS_CODE = "statusCode";
  static final String METHOD = "method";
  static final String URI_HOST = "host";
  static final String URI = "uri";
  static final String URI_PATH = "path";
  static final String REQ_COUNTER = "reqCounter";

  static final String OPERATION = "operation";
  static final String OPERATION_COUNTER = "opCounter";

  static final String EXECUTED_BATCHES = "executedBatches";
  static final String BATCH_SIZE = "batchSize";
  static final String TOTAL_STMS = "totalStms";
  static final String PROTOCOL = "protocol";
  static final String REMOTE_HOST_ADDRESS = "remoteHostAddress";
  static final String REMOTE_HOST_PORT = "remoteHostPort";
  static final String REQ_HEADERS = "reqHeaders";
}
