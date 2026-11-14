# Tidewater Freight — Shipping Cost Service

This is the starter repository for the **Code Quality & Testing** applied lab.
Read the lab brief alongside this repository; it explains the business, what you
are being asked to deliver, and how your work is assessed.

## What this is

A small shipping-cost service that was written quickly with an AI assistant, together with the
tests it produced. Everything compiles and the tests pass, so at a glance it
looks finished. Your job is to bring it up to AmaliTech standard: judge what is
actually wrong with the code **and** its tests, refactor it, make it genuinely
testable, handle its failures properly, and replace the tests that only look
like tests.

## Refactored solution

- [ANALYSIS.md](ANALYSIS.md): what was wrong with the code and the tests, ranked.
- [RATIONALE.md](RATIONALE.md): the design decisions, how a new zone is added, and the improvement beyond the brief.

## How to run

Requires JDK 21 or newer and Maven 3.9+.

```bash
mvn test                                                    # full JUnit 5 + Mockito suite
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # mutation testing, fails below 85%
mvn compile exec:java -Dexec.mainClass=com.tidewater.freight.Demo
```

Without the exec plugin, run the demo with `java -cp target/classes com.tidewater.freight.Demo`
after `mvn compile`. It prints two quotes and a clear `Unknown zone: MOON` error (not `0.0`).

## Running it

You need JDK 21 and Maven.

**See the service run (a small demo, no tests):**

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.tidewater.freight.Demo
```

**Run the existing tests:**

```bash
mvn test
```

**Measure your tests** (coverage and mutation testing are already set up):

```bash
mvn verify                                                  # tests + JaCoCo report: target/site/jacoco/index.html
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # PIT report: target/pit-reports/index.html
```

Run the demo and the tests before you change anything. Note what the demo prints for an unknown zone,
and look carefully at whether the tests that pass actually prove anything.

## What to hand in

See the lab brief for the full list: your written analysis (of the code and the
tests), the refactored code, robust exception handling, a real test suite, a
short rationale, and one improvement beyond the brief. You will talk through your
work in a live review.
