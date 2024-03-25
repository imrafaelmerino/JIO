package jio.test.pbt;


/**
 *  Class tha represents an unhandled failure during the execution of a Property and no
 *  report is generated. It's usually something to be fixed in the property by the developer.
 */
@SuppressWarnings("serial")
public final class ReportNotGenerated extends RuntimeException {


  ReportNotGenerated(final Exception e) {
    super(e);
  }
}
