# Analysis: Tidewater Freight Shipping Service (starter code)

Each defect follows the AmaliTech four-part defect-diagnosis format:
**Name it**, **Locate it**, **Explain the impact**, **Set the direction**.

## Ranking

1. **C1**: the swallowed exception (silent wrong money).
2. **C3**: the split rate rules that block the new zone.
3. **The hollow tests (T1–T4)**: a green suite that proves nothing.
4. Then **C2**, **C5**, **C4**, **C6**.

---

## Problems in the code

### C1: Swallowed exception returns a free quote
- **Name it:** Error hiding / swallowed exception.
- **Locate it:** `ShippingCalculator.calculateCost`, the `catch (Exception e) { return 0; }` block.
- **Explain the impact:** Any failure (an unknown zone such as `MOON`, a `null` service level, a broken
  repository) becomes a price of `0`. Callers cannot tell a failure from a free shipment, so the
  business can ship goods for nothing without anyone noticing.
- **Set the direction:** Add unchecked custom exceptions (`TidewaterException` and subclasses).
  Never return a default value on failure. The only `try/catch` sits at the repository boundary and
  wraps unexpected failures in `RateUnavailableException`, keeping the cause.

### C2: No input validation
- **Name it:** Missing input validation / primitive obsession.
- **Locate it:** The parameters of `calculateCost(String zone, double weightKg, String serviceLevel)`.
- **Explain the impact:** Negative weight gives a negative cost, `NaN` flows through, zero weight is
  accepted, an unknown service level such as `"OVERNIGHT"` is priced as standard, and typos in raw
  strings are not caught. Garbage in, wrong money out.
- **Set the direction:** A `ServiceLevel` enum replaces the service-level string, and a validated,
  immutable `ShipmentRequest` rejects a blank zone, a missing service level and any weight that is not a
  positive finite number.

### C3: Rate rules split across two places
- **Name it:** Open/Closed violation / shotgun surgery.
- **Locate it:** The per-kg rate is in `InMemoryRateRepository`; the zone fees are in the `if/else`
  chain in `ShippingCalculator`.
- **Explain the impact:** A new zone added only to the repository is silently charged no fee. Every
  new zone needs edits to existing calculator logic, and the business has a new zone coming.
- **Set the direction:** `ZoneRate` holds the per-kg rate and the flat fee together, and the repository
  returns it. Pricing steps become separate `PricingRule` classes that never name a zone, so a new zone
  is one more entry in a repository.

### C4: One method doing five jobs
- **Name it:** Single Responsibility violation / long method.
- **Locate it:** `ShippingCalculator.calculateCost` (lookup, zone fee, heavy surcharge, express
  multiplier, logging).
- **Explain the impact:** No rule can be understood, changed or tested on its own, and any change
  risks breaking the others.
- **Set the direction:** One class per rule (`BasePriceRule`, `ZoneFeeRule`,
  `HeavyItemSurchargeRule`, `ServiceLevelRule`), put in order by `DefaultPricingRules`. The
  calculator only looks up the rate, runs the rules and rounds.

### C5: Hardwired repository
- **Name it:** Dependency Inversion violation.
- **Locate it:** `private final RateRepository repository = new InMemoryRateRepository();` in
  `ShippingCalculator`.
- **Explain the impact:** The calculator cannot be given another rate source and cannot be tested in
  isolation, even though a `RateRepository` interface exists.
- **Set the direction:** Constructor injection: `ShippingCalculator(RateRepository, List<PricingRule>)`.

### C6: Side effects, magic numbers, `double` money, empty branch
- **Name it:** Code smells (side effect in a calculation, magic numbers, floating-point money, dead code).
- **Locate it:** `System.out.println` in `calculateCost`; the literals `10, 25, 30, 20, 1.5`; the
  `double` return type; the empty `if (zone.equals("DOMESTIC")) {}` branch.
- **Explain the impact:** The calculation prints to stdout, the numbers have no names, `double` causes
  rounding errors on money, and the empty branch is noise.
- **Set the direction:** Remove the `println`, give every constant a name, use `BigDecimal` rounded
  once to 2 decimals with `HALF_UP`, and delete the empty branch.

---

## Problems in the tests (`ShippingCalculatorTest`)

### T1: `calculatesACost`
- **Name it:** Weak (hollow) assertion.
- **Locate it:** `assertTrue(cost > 0)`.
- **Explain the impact:** It passes for the right value and for almost every wrong one.
- **Set the direction:** Rewrite it with exact expected `BigDecimal` values.

### T2: `calculatesExpressCost`
- **Name it:** Mocking the class under test.
- **Locate it:** `mock(ShippingCalculator.class)` followed by `when(...).thenReturn(967.5)`.
- **Explain the impact:** It asserts the stub it just set up; no production code runs at all.
- **Set the direction:** Delete it and replace it with a real test that mocks only the repository.

### T3: `returnsACostForAShipment`
- **Name it:** Assertion that can never fail.
- **Locate it:** `assertNotNull(cost)` on a primitive `double` (autoboxed, so never null).
- **Explain the impact:** The test passes whatever the calculator returns.
- **Set the direction:** Delete it and replace it with exact-value tests.

### T4: The whole suite
- **Name it:** Missing failure, boundary and fee tests.
- **Locate it:** `ShippingCalculatorTest` as a whole.
- **Explain the impact:** The `return 0` bug, the 30 kg boundary and the zone fees are untested, so
  the suite stays green while the money is wrong.
- **Set the direction:** Add value-type, validation, repository and per-rule tests (including the
  30 kg boundary), plus calculator happy-path, failure-path and new-zone tests and a PIT mutation
  threshold.
