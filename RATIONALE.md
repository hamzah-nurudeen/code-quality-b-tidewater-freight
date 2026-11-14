# Rationale: Tidewater Freight Refactor

This document explains the architectural and design decisions behind the refactored service.
The defects in the starter code and tests (and their [priority ranking](ANALYSIS.md#ranking)) are diagnosed in [ANALYSIS.md](ANALYSIS.md).

## 1. Key decisions

- **Unchecked custom exceptions for domain & caller failures vs. standard JDK exceptions for wiring bugs.**
  A failed quote is a data or availability problem that the caller cannot "retry around" on the spot, so
  checked exceptions would only add `throws` noise everywhere. Every domain/caller shipment failure has its
  own named type (`UnknownZoneException`, `InvalidWeightException`, `InvalidShipmentException`,
  `RateUnavailableException`) under one base, `TidewaterException`, and each message contains the offending
  value. By contrast, programmer configuration mistakes when wiring the service or constructing rate data
  (`ZoneRate` given a null/blank zone or negative rate/fee, or `ShippingCalculator` constructed with a null
  dependency, empty rule list, or null rule element) throw standard `IllegalArgumentException` or
  `NullPointerException` at startup/construction time. Similarly, unexpected runtime defects inside a
  `PricingRule` propagate untouched rather than being disguised as domain errors.
- **One try/catch, only at the repository boundary.** The repository is the one place where an outside
  system (later a database or remote service) can fail in ways we do not control.
  `ShippingCalculator.lookUpRate` lets `TidewaterException` subtypes (`UnknownZoneException`) pass through
  unchanged, wraps unexpected repository `RuntimeException`s in `RateUnavailableException` keeping the
  original cause, and rejects a `null` return from `repository.findRate(zone)` with
  `RateUnavailableException`. Nothing returns `0` or any other default value.
- **`ServiceLevel` is an enum with `fromText`, while zone is a validated string.** There are two service
  levels, fixed by the business, and each carries its own multiplier, so an enum makes a typo such as
  `"OVERNIGHT"` impossible and removes the `if` on names. `ServiceLevel.fromText(String)` is provided as
  part of the public API for callers (CLI, CSV, or HTTP adapters) that receive raw text at system boundaries.
  Zones are data that will grow (the coming new zone), so they live in the repository; an enum would force a
  code change for every new zone.
- **Exact, case-sensitive zone matching across layers.** `ShipmentRequest` and `ZoneRate` both reject
  `null` or blank (`isBlank()`) zone strings, while keeping non-blank strings verbatim without trimming or
  case-folding. `RateRepository` matches zones by exact equality (`"domestic"` and `" domestic "` are
  unknown zones rather than silently guessed).
- **`ZoneRate` holds rate and fee together (`final` and immutable).** In the original code the rate was in
  the repository and the fee was in an `if/else` in the calculator, so a zone added only to the repository
  was silently charged no fee. Keeping both in one `final` immutable object means a zone cannot exist
  without its fee.
- **A non-empty, unmodifiable list of pricing rules.** Each rule (`BasePriceRule`, `ZoneFeeRule`,
  `HeavyItemSurchargeRule`, `ServiceLevelRule`) does one job and is tested on its own. `ShippingCalculator`
  validates that the rule list is non-null, non-empty (so an empty list can never silently produce a
  `0.00` quote), and contains no null elements, storing an unmodifiable copy via `List.copyOf`.
- **`BigDecimal`, scale 2, `HALF_UP`.** `double` cannot represent money exactly. Amounts are built from
  strings, and the result is rounded **once**, at the end, to whole pesewas using standard commercial
  rounding (`1.665 → 1.67`). Rounding inside each rule would let small errors build up.
- **Positive finite weight with no arbitrary upper cap.** A shipment with zero, negative, `NaN`, or infinite
  weight is rejected with `InvalidWeightException` because pricing zero weight would charge only the flat
  fee and hide a data-entry error. A tiny positive weight such as `0.001` kg is accepted, and no upper
  weight limit is hardcoded in `ShipmentRequest` because physical carrier limits vary by transport mode and
  belong in upstream booking rules rather than the tariff calculator.
- **The express rule must be last.** The business rule (kept from the original) is that express multiplies
  everything accumulated so far, including the zone fee and the heavy surcharge. `DefaultPricingRules.create()`
  returns an unmodifiable list in that exact order, verified both by
  `DefaultPricingRulesTest.defaultPricingRulesAreInExpectedOrderWithServiceLevelLast` and by
  `ShippingCalculatorTest.ruleOrderMattersExpressMustComeAfterFee` (`75.00` vs `70.00`).
- **Sub-packages under `com.tidewater.freight`.** Classes are grouped into `exception`, `model`, `rate`,
  and `pricing`, with `ShippingCalculator` and `Demo` at the root. Lower packages never import higher ones
  (`exception` ← `model` ← `rate` / `pricing` ← root). Adding a zone touches only `rate`, and adding a
  kind of charge touches only `pricing`.

## 2. How the design absorbs the new zone

[`NewZoneTest`](src/test/java/com/tidewater/freight/NewZoneTest.java) stubs the repository to return an
`ARCTIC` zone (rate 20, fee 40) and runs it through the **unchanged** `DefaultPricingRules.create()`:

- ARCTIC 10 kg STANDARD → `240.00` (200 + 40)
- ARCTIC 30 kg STANDARD → `640.00` (600 + 40, no surcharge at the 30 kg boundary)
- ARCTIC 35 kg EXPRESS → `1140.00` ((700 + 40 + 20) × 1.5)

To add the zone for real, the only change is one line in the
[`InMemoryRateRepository`](src/main/java/com/tidewater/freight/rate/InMemoryRateRepository.java) constructor
(or the same data in any other `RateRepository` implementation):

```java
addZone("ARCTIC", "20", "40");
```

No edit to `ShippingCalculator`, any rule class, or `DefaultPricingRules` is needed. Adding one row to the
parameterized table in `ShippingCalculatorTest` then covers it end to end.

## 3. Kept / fixed / rewrote

| Item | Decision | Reason |
|---|---|---|
| `RateRepository` | **Changed** | Now returns `ZoneRate` (rate and fee) instead of a `double` rate, and documents that it throws `UnknownZoneException`. |
| `InMemoryRateRepository` | **Kept the idea, fixed** | Kept the map-based lookup. The map now holds `ZoneRate` with the fee, amounts are built from strings, and a missing zone throws `UnknownZoneException` instead of `IllegalArgumentException`. |
| `ShippingCalculator` | **Rewritten** | Constructor injection of repository and non-empty unmodifiable rules, `BigDecimal` result, no `println`, no zone or level `if`s, no swallowed exception, and explicit check against a null repository result. |
| `Demo` | **Fixed** | Uses the new API, consistent labels, and prints a clear `Unknown zone: MOON` message instead of `0.0`. |
| T1 `calculatesACost` | **Rewritten** | `assertTrue(cost > 0)` accepted almost any value. Replaced by exact-value scenarios in `costTableMatchesReferenceValues`. |
| T2 `calculatesExpressCost` | **Deleted** | It mocked the class under test and asserted its own stub; no real code ran. Replaced by the `INTERNATIONAL, 40, EXPRESS, 967.50` row in `ShippingCalculatorTest`, which runs the real calculator. |
| T3 `returnsACostForAShipment` | **Deleted** | `assertNotNull` on a primitive `double` can never fail. Replaced by the `REGIONAL, 5, STANDARD, 50.00` row in `ShippingCalculatorTest`. |

## 4. The improvement beyond the brief

1. **Parameterized cost table.** `ShippingCalculatorTest.costTableMatchesReferenceValues` uses
   `@ParameterizedTest` and `@CsvSource` to list every reference scenario plus the rounding case, run against
   the real `InMemoryRateRepository`. Adding a zone or a price case later is one more row, and every
   pricing mistake shows up as a failing row with a readable name such as `REGIONAL 31.0kg EXPRESS costs 417.00`.
2. **Enforced mutation-score and coverage gates in `mvn verify`.** Both `jacoco-maven-plugin:check`
   (minimum 90% line coverage) and `pitest-maven:mutationCoverage` (`<mutationThreshold>95</mutationThreshold>`,
   with `Demo` excluded because it is a console entry point) are bound directly to the `verify` lifecycle
   phase in `pom.xml`. The original suite was green while testing nothing; binding PIT with a 95% threshold
   to `verify` ensures the build fails automatically if tests stop killing mutants.

## 5. What the AI got wrong (what to look for next time)

- **Swallowing exceptions:** `catch (Exception e) { return 0; }` turned every failure into a free quote.
- **Hollow assertions:** `assertTrue(cost > 0)` passes for wrong values.
- **Mocking the class under test:** the express test stubbed `ShippingCalculator` and asserted the stub.
- **Assertions that can never fail:** `assertNotNull` on an autoboxed primitive.
- **Hardwired dependencies:** `new InMemoryRateRepository()` inside the calculator, despite the interface.
- **Logging with `println`** inside a calculation.
- **Magic numbers** (`10, 25, 30, 20, 1.5`), `double` for money and an empty `if` branch.

A green build is not evidence on its own: read what each test asserts and ask whether it could fail.

## 6. Verified test, coverage, and mutation results

Measured with `mvn verify` (72 JUnit 5 tests, 0 failures; JaCoCo `check` passed; PIT 1.16.1 passed against the 95% threshold):

- **Total tests run:** 72 (0 failures, 0 errors, 0 skipped)
- **Line coverage (mutated classes, excluding `Demo`):** 117 / 119 lines (**98%**) — the only 2 unexecuted lines are the `private` utility constructors on `DefaultPricingRules` and `Demo`.
- **Mutations generated / killed:** **49 generated, 49 killed (100% mutation score, 100% test strength)**.

| Package | Mutated Classes | Line Coverage | Mutation Coverage | Test Strength |
|---|---:|---:|---:|---:|
| `com.tidewater.freight` | 1 | 100% (28/28) | 100% (6/6) | 100% (6/6) |
| `com.tidewater.freight.exception` | 1 | 100% (4/4) | 100% (1/1) | 100% (1/1) |
| `com.tidewater.freight.model` | 3 | 100% (55/55) | 100% (29/29) | 100% (29/29) |
| `com.tidewater.freight.pricing` | 5 | 90% (18/20) | 100% (8/8) | 100% (8/8) |
| `com.tidewater.freight.rate` | 1 | 100% (12/12) | 100% (5/5) | 100% (5/5) |
| **Total** | **11** | **98% (117/119)** | **100% (49/49)** | **100% (49/49)** |
