# Contributing to LoudounCodes Combinatorics

Thanks for helping make this a great, classroom-friendly library!  
Our north star: **simple, well-documented Java iterables** that students can use in one glance.

---

## TL;DR

```bash
# 1) Clone
git clone https://github.com/<your-org>/loudouncodes-combinatorics.git
cd loudouncodes-combinatorics

# 2) Ensure Java (17 or later)
java -version

# 3) Build, format, test, javadoc
./gradlew spotlessApply build javadoc
````

If `./gradlew` works, you’re basically set.

---

## Development Environment

### Prereqs

* **Java 17+** (Temurin/Adoptium or any modern JDK)
* **Git**
* **No global Gradle required** — the repo includes the Gradle Wrapper.

### First-time setup

1. **Clone & enter**

   ```bash
   git clone https://github.com/<your-org>/loudouncodes-combinatorics.git
   cd loudouncodes-combinatorics
   ```
2. **Verify Java**

   ```bash
   java -version
   ```

   Should show 17 or newer.
3. **Build**

   ```bash
   ./gradlew build
   ```
4. **Run tests**

   ```bash
   ./gradlew test
   ```
5. **Generate Javadoc (doclint on)**

   ```bash
   ./gradlew javadoc
   ```
6. **Apply formatting (Spotless/Google Java Format)**

   ```bash
   ./gradlew spotlessApply
   ```

### IDE tips

* Import as a **Gradle** project.
* Enable “treat warnings as errors” for Javadoc if your IDE supports it (keeps docs clean).
* Ensure your IDE uses the project’s Gradle JVM (Java 17+).

---

## Project Conventions

### API shape & behavior

* **Iterables over `int[]`**: Generators expose `Iterable<int[]>` (e.g., combinations, permutations).
* **Defensive copies**: `Iterator#next()` must return a fresh array so callers can mutate safely.
* **Ordering**: Prefer **lexicographic** or an obviously stated canonical order. Document it clearly.
* **`size()`**: Provide a `long size()` if there’s a natural count (may overflow for large inputs—document that).
* **Edge cases**:

  * Validate bad inputs and throw `IllegalArgumentException`.
  * Define behavior for zero-size cases in Javadoc (e.g., `k=0` emits one empty tuple).
* **Examples**: Each public class should include at least one small, copy-pasteable example.

### Documentation

* **Doclint-safe Javadoc**: Use lower-case HTML tags (`<p>`, `<ul>`, etc.). Close tags properly. Avoid custom tags like `@implNote` (use prose).
* Update `package-info.java` if you add a concept students should discover at package level.

### Testing

* **Frameworks**: JUnit 5 + AssertJ (already configured).
* **Coverage**: Focus on correctness and iterator contracts.

  * Verify **order**, **count**, **edge cases**, **defensive copies**, and **exhaustion** (`NoSuchElementException`).
* **Run tests**

  ```bash
  ./gradlew test
  ```
* **Single test class**

  ```bash
  ./gradlew test --tests "org.loudouncodes.combinatorics.CombinationsTest"
  ```

### Formatting & linting

* **Spotless** with Google Java Format is enforced in CI.

  ```bash
  ./gradlew spotlessApply
  ```
* Build will fail if formatting is off:

  ```bash
  ./gradlew spotlessCheck
  ```

---

## How to Propose a Change

1. **Open an issue** describing the problem or feature. Include:

   * What you want to add/change and why it benefits classroom use.
   * Expected input/output shape, ordering, and edge cases.
2. **Fork & branch**

   ```bash
   git checkout -b feature/my-awesome-generator
   ```
3. **Implement**

   * Keep files small, readable, and well-commented.
   * Add **Javadoc** with examples.
   * Add **unit tests** (happy path + edge cases).
4. **Format & test**

   ```bash
   ./gradlew spotlessApply build javadoc
   ```
5. **Pull request**

   * Include a concise description, rationale, and any design choices (ordering, complexity).
   * Reference the issue you opened.

### PR checklist

* [ ] Code is formatted (`spotlessApply`).
* [ ] All tests pass (`./gradlew test`).
* [ ] New/changed APIs have Javadoc with examples.
* [ ] Tests cover iterator contract, ordering, edge cases, and defensive copies.
* [ ] README and/or `package-info.java` updated if appropriate.

---

## Scope Guardrails (What Fits / What Doesn’t)

**Do focus on:**

* Small, practical generators for **puzzles, games, and demos** (e.g., Set, Secret Santa, simple search problems).
* Clean APIs that high school students can understand in minutes.

**Be cautious with:**

* Heavy discrete-math features that explode in size (PowerSet, SetPartitions, IntegerPartitions).

  * These are welcome as **stretch goals** (with clear docs about growth), but keep core library approachable.

---

## Stretch Goals (if we grow into a full course library)

* **PowerSet** — all subsets (size `2^n`).
* **IntegerPartitions** — partitions of an integer `n` (with `size()` via Euler’s pentagonal recurrence).
* **SetPartitions / Stirling numbers** — partitions of sets into unlabeled blocks.
* **Compositions** — ordered summands.
* **Adapters & utilities** — mapping to arrays/streams; `BigInteger` counting for large `n`.
* **Visualization aids** — Ferrers/Young diagrams for partitions.

Please discuss large additions in an issue first so we can keep the design cohesive.

