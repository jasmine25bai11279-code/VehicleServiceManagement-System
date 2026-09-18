package com.vehicleservice;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Headless CLI Test Suite Runner to run all JUnit 5 tests and print summary reports.
 */
public class TestSuiteRunner {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   VEHICLE SERVICE MANAGEMENT SYSTEM - JUNIT 5 TEST RUNNER     ");
        System.out.println("===============================================================");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("com.vehicleservice"))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("\n---------------------------------------------------------------");
        System.out.println("                       TEST RESULTS SUMMARY                    ");
        System.out.println("---------------------------------------------------------------");
        System.out.println("Tests found:     " + summary.getTestsFoundCount());
        System.out.println("Tests started:   " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed:    " + summary.getTestsFailedCount());
        System.out.println("Tests aborted:   " + summary.getTestsAbortedCount());
        System.out.println("Total time:      " + summary.getTimeFinished() + " ms");
        System.out.println("---------------------------------------------------------------");

        if (summary.getTestsFailedCount() > 0) {
            System.err.println("FAILED TESTS:");
            summary.getFailures().forEach(failure -> {
                System.err.println("• " + failure.getTestIdentifier().getDisplayName() + ": " + failure.getException().getMessage());
            });
            System.exit(1);
        } else {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY! ✓");
            System.exit(0);
        }
    }
}
