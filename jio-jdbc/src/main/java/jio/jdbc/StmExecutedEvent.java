package jio.jdbc;

import jdk.jfr.Event;

import jdk.jfr.StackTrace;

@StackTrace(value = false)
abstract class StmExecutedEvent extends Event {

  static final String RESULT_FIELD = "result";
  static final String SQL_FIELD = "sql";
  static final String EXCEPTION_FIELD = "exception";
  static final String LABEL_FIELD = "label";

  /**
   * the method of the request
   */
  String sql;

  /**
   * the result of the exchange: a success if a response is received or an exception
   */
  String result;
  /**
   * the exception in case of one happens during the exchange
   */
  String exception;

  /**
   * Short label to identify the statement
   */
  String label;


  enum RESULT {
    SUCCESS, FAILURE, PARTIAL_SUCCESS
  }
}
