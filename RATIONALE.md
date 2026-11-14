# Rationale: Tidewater Freight refactor

This file explains the decisions behind the refactor. The defects themselves are described in
[ANALYSIS.md](ANALYSIS.md).

**Priority order of the fixes:** 1. C1 (swallowed exception), 2. C3 (split rate rules that block the new
zone), 3. the hollow tests, then C2, C5, C4 and C6.

## 1. Key decisions

- **Unchecked custom exceptions.** A failed quote is a programming or data problem that the caller cannot
  "retry around" on the spot, so checked exceptions would only add `throws` noise everywhere. Every
  failure has its own named type (`UnknownZoneException`, `InvalidWeightException`,
  `InvalidShipmentException`, `RateUnavailableException`) under one base, `TidewaterException`, and each
  message contains the bad value. Callers can catch exactly the case they care about, or the base type.
- **One try/catch, only at the repository boundary.** The repository is the one place where an outside
  system (later a database or a service) can fail in ways we don't control. `ShippingCalculator.findRateSafely`
  lets our own exceptions (`UnknownZoneException`) pass through unchanged and wraps anything else in
  `RateUnavailableException`, keeping the original cause. Nothing returns `0` or any other default value.
  Validation errors are thrown before the calculator runs, so they need no catching.
- **`ServiceLevel` is an enum, zone is a validated string.** There are two service levels, fixed by the
  business, and each carries its own multiplier, so an enum makes a typo such as `"OVERNIGHT"` impossible and
  removes the `if` on names. Zones are data that will grow (the coming new zone), so they live in the
  repository. An enum would force a code change for every new zone. `ShipmentRequest` only checks the
  zone is present; the repository decides whether it exists. Zones are matched exactly (case-sensitive,
  not trimmed) so `"domestic"` is an unknown zone rather than being guessed.
- **`ZoneRate` holds rate and fee together.** In the original code the rate was in the repository and the
  fee was in an `if/else` in the calculator, so a zone added only to the repository was silently charged
  no fee. Keeping both in one immutable object means a zone cannot exist without its fee.
- **A list of pricing rules.** Each rule (`BasePriceRule`, `ZoneFeeRule`, `HeavyItemSurchargeRule`,
  `ServiceLevelRule`) does one job and can be tested on its own. The calculator only looks up the rate,
  runs the rules and rounds. A new kind of charge is a new class added to `DefaultPricingRules`, with no
  edit to the calculator.
- **`BigDecimal`, scale 2, `HALF_UP`.** `double` cannot represent money exactly. Amounts are built from
  strings, and the result is rounded **once**, at the end, to whole pesewas using the usual commercial
  rounding (`1.665 → 1.67`). Rounding inside each rule would let small errors build up.
- **Zero weight is rejected.** A shipment with no weight is not a real shipment; pricing it would give a
  quote of just the flat fee and hide a data-entry error. Weight must be a positive finite number, so
  negative, zero, `NaN` and infinity are all rejected. A tiny weight such as `0.001` is still accepted.
- **The express rule must be last.** The business rule (kept from the original) is that express multiplies
  everything so far, including the zone fee and the heavy surcharge. `DefaultPricingRules` fixes that order
  in one place, and `ShippingCalculatorTest.ruleOrderMattersExpressMustComeAfterFee` fails if someone
  reorders it (`75.00` vs `70.00`).
- **Null request throws `InvalidShipmentException`** (not `NullPointerException`), so every bad-input case
  from the calculator's public method is one of our own types. A null constructor dependency is a wiring
  bug, so it throws `NullPointerException` through `Objects.requireNonNull`.
- **Sub-packages under `com.tidewater.freight`.** The classes are grouped into `exception`, `model`,
  `rate` and `pricing`, with `ShippingCalculator` and `Demo` at the root. Lower packages never import
  higher ones (`exception` ← `model` ← `rate` / `pricing` ← root). Adding a zone touches only `rate`,
  and adding a kind of charge touches only `pricing`.

## 2. How the design absorbs the new zone

[`NewZoneTest`](src/test/java/com/tidewater/freight/NewZoneTest.java) stubs the repository to return an
`ARCTIC` zone (rate 20, fee 40) and runs it through the **unchanged** `DefaultPricingRules.create()`:

- ARCTIC 10 kg STANDARD → `240.00` (200 + 40)
- ARCTIC 35 kg EXPRESS → `1140.00` ((700 + 40 + 20) × 1.5)

To add the zone for real, the only change is one line in the `InMemoryRateRepository` constructor
(or the same data in any other `RateRepository` implementation):

```java
addZone("ARCTIC", "20", "40");
```

No edit to `ShippingCalculator`, any rule class or `DefaultPricingRules` is needed. Adding one row to the
parameterized table in `ShippingCalculatorTest` then covers it end to end.

## 3. Kept / fixed / rewrote

| Item | Decision | Reason |
|---|---|---|
| `RateRepository` | **Changed** | Now returns `ZoneRate` (rate and fee) instead of a `double` rate, and documents that it throws `UnknownZoneException`. |
| `InMemoryRateRepository` | **Kept the idea, fixed** | Kept the map-based lookup. The map now holds `ZoneRate` with the fee, amounts are built from strings, and a missing zone throws `UnknownZoneException` instead of `IllegalArgumentException`. |
| `ShippingCalculator` | **Rewritten** | Constructor injection of repository and rules, `BigDecimal` result, no `println`, no zone or level `if`s, no swallowed exception. |
| `Demo` | **Fixed** | Uses the new API and prints a clear `Unknown zone: MOON` message instead of `0.0`. |
| T1 `calculatesACost` | **Rewritten** | `assertTrue(cost > 0)` accepted almost any value. Replaced by exact-value tests (`domesticTenKgStandardCosts50` and the rest). |
| T2 `calculatesExpressCost` | **Deleted** | It mocked the class under test and asserted its own stub; no real code ran. Replaced by `internationalFortyKgExpressCosts967point50`, which runs the real calculator. |
| T3 `returnsACostForAShipment` | **Deleted** | `assertNotNull` on a primitive `double` can never fail. Replaced by `regionalFiveKgStandardCosts50IncludingFee`. |

## 4. The improvement beyond the brief

1. **Parameterized cost table.** `ShippingCalculatorTest.costTableMatchesReferenceValues` uses
   `@ParameterizedTest` and `@CsvSource` to list every reference scenario plus the rounding case, run against
   the real `InMemoryRateRepository`. Adding a zone or a price case later is one more row, and every
   pricing mistake shows up as a failing row with a readable name such as `REGIONAL 31.0kg EXPRESS costs 417.00`.
2. **Mutation-score gate.** The `pitest-maven` configuration in `pom.xml` now has
   `<mutationThreshold>` (with `Demo` excluded, because it is a console entry point and not logic). The
   original suite was green while testing nothing; line coverage would not have caught that, but mutation
   testing does, because it changes the code and checks a test fails. With the threshold, the build fails
   if the tests stop catching bugs, which blocks that exact problem from coming back.
   See *Mutation result* below for the measured score.

## 5. What the AI got wrong (what to look for next time)

- **Swallowing exceptions:** `catch (Exception e) { return 0; }` turned every failure into a free quote.
- **Hollow assertions:** `assertTrue(cost > 0)` passes for wrong values.
- **Mocking the class under test:** the express test stubbed `ShippingCalculator` and asserted the stub.
- **Assertions that can never fail:** `assertNotNull` on an autoboxed primitive.
- **Hardwired dependencies:** `new InMemoryRateRepository()` inside the calculator, despite the interface.
- **Logging with `println`** inside a calculation.
- **Magic numbers** (`10, 25, 30, 20, 1.5`), `double` for money and an empty `if` branch.

A green build is not evidence on its own: read what each test asserts and ask whether it could fail.

## Mutation result

`mvn test-compile org.pitest:pitest-maven:mutationCoverage` currently reports **44 mutations generated,
44 killed (100%)**, with 98% line coverage of the mutated classes. The threshold is set at **85**, the
starting value from the brief. It is below today's score on purpose, so that an equivalent mutant created
by a future change does not break the build by itself, while a real drop in test quality (like the
original suite) still fails it.
