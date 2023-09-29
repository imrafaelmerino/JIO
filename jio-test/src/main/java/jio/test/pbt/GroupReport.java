package jio.test.pbt;

import jsonvalues.JsArray;
import jsonvalues.JsObj;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a collection of individual test reports for a group of properties. This class allows you
 * to perform assertions and generate a JSON representation of the test reports for the group.
 */
public class GroupReport {

    List<Report> reports;

    String groupName;

    GroupReport(List<Report> reports, String groupName) {
        this.reports = reports;
        this.groupName = groupName;
    }
    /**
     * Asserts that all individual test reports within this group report are successful.
     *
     * @throws AssertionError If any of the individual test reports contains failures or exceptions.
     */

    public void assertAllSuccess() {

        reports.forEach(Report::assertAllSuccess);
    }

    /**
     * Asserts that there are no failures in any of the individual test reports within this group report.
     *
     * @throws AssertionError If any of the individual test reports contains failures or exceptions.
     */
    public void assertNoFailures() {

        reports.forEach(Report::assertNoFailures);

    }

    /**
     * Asserts a condition for all individual test reports within this group report and provides a custom message
     * in case of failure.
     *
     * @param condition The condition to be applied to each individual test report.
     * @param message   A supplier function to provide a custom message in case the condition fails.
     * @throws AssertionError If the condition fails for any individual test report.
     */
    public void assertThat(Predicate<Report> condition,
                           Supplier<String> message
                          ) {

        reports.forEach(r -> r.assertThat(condition, message));

    }

    public JsObj toJson() {
        return reports.stream().reduce(
                JsObj.empty().set(groupName, JsObj.empty()),
                (json, report) -> json.set(groupName, json.getObj(groupName).set(report.getPropName(), report.toJson())),
                (a, b) -> JsObj.of(groupName, a.getObj(groupName).union(b.getObj(groupName), JsArray.TYPE.LIST)
                                  )
                                      );
    }

    public String toString() {
        return toJson().toString();
    }



}