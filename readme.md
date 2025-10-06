# RandKit

Lightweight random-variate generation for Java 17+.
Built on the JDK’s `java.util.random.RandomGenerator`, with a tiny, predictable API.

* **Docs / Javadoc:** `docs/javadoc/` (if you’re viewing this on GitHub Pages: `/javadoc/`)
* **Packages:**
  `org.loudouncodes.randkit.api` – public interfaces
  `org.loudouncodes.randkit.continuous` – continuous distributions
  `org.loudouncodes.randkit.discrete` – discrete distributions
  `org.loudouncodes.randkit.util` – RNG utilities

---

## Why this exists (my seed story)

30+ years ago I wrote a Pascal random number library that could spit out numbers shaped like bell curves, exponentials, and a bunch of other distributions I don’t fully remember—but I loved it. I’ve wanted a clean, modern remake for a long time. RandKit is that do-over: a small, sharp library that leans on Java’s built-in RNGs, stays deterministic when you seed it, and gives you the distributions you actually use in sims, games, and teaching—without a pile of dependencies.

---

## Design goals

* **Tiny surface area:** just the essentials (`sample`, `pdf/pmf`, `cdf`, `quantile`, `mean`, `variance`).
* **Deterministic by seed:** same seed + params ⇒ same results on the same JDK family.
* **No modulo bias:** unbiased integer ranges.
* **JDK-native RNGs:** accept any `RandomGenerator` (LXM family by default).
* **Clear support metadata:** distributions report their mathematical support.

---

## Quick start

```java
// Continuous uniform on [0,1), default RNG
double x = new org.loudouncodes.randkit.continuous.UniformDouble(0.0, 1.0).sample();

// Discrete uniform on integers [1,6], deterministic by seed
int k = new org.loudouncodes.randkit.discrete.UniformInt(1234L, 1, 6).sample();

// Bring your own RNG
var rng = org.loudouncodes.randkit.util.Randoms.seeded(42L);
double y = new org.loudouncodes.randkit.continuous.UniformDouble(rng, -2.0, 3.0).sample();
```

Build & test:

```bash
./gradlew clean build
```

JDK 17+ required.

---

## API at a glance

* `ContinuousDistribution` → `double sample()`, `pdf`, `cdf`, `quantile`, `mean`, `variance`
* `DiscreteDistribution` → `int sample()`, `pmf`, `cdf`, `mean`, `variance`
* `DistributionSupport` → lower/upper bounds, open/closed endpoints, continuous vs discrete
* `Randoms` → default and seeded `RandomGenerator` helpers

---

## Current distributions

### Continuous

* **UniformDouble(a, b)** — uniform on **[a, b)**

### Discrete

* **UniformInt(a, b)** — uniform on integers **[a, b]**

---

## Roadmap: distributions to add

These are prioritized by usefulness and implementation complexity. The v1 set is what I plan to land first; the v1.1+ list follows.

### Continuous (v1 target)

* **Normal(μ, σ)** — Box–Muller polar (per-instance spare)
* **Exponential(λ)** — inverse CDF
* **Gamma(k, θ)** — Marsaglia–Tsang
* **Beta(α, β)** — via two Gammas
* **Weibull(k, λ)** — inverse CDF
* **LogNormal(μ, σ)** — exp(Normal)
* **Triangular(min, mode, max)** — piecewise inverse

### Discrete (v1 target)

* **Bernoulli(p)**
* **Binomial(n, p)** — exact for small/medium n; documented approximations for very large n
* **Geometric(p)** — trials-until-first-success (documented variant)
* **Poisson(λ)** — Knuth for small λ; transformed rejection for larger λ
* **Categorical(weights…)** — Vose alias method

### Transforms & utilities (v1 target)

* **Truncated<T>** — acceptance–rejection on top of another distribution
* **Mixture<T>** — categorical over components
* **Empirical** — ECDF + inverse via binary search
* **Combinatorics** — Fisher–Yates shuffle, k-subset sampling, reservoir sampling

### Nice-to-haves (v1.1+)

* Continuous: Laplace, Pareto, Student-t, Beta-PERT
* Discrete: Hypergeometric, Negative Binomial
* Multivariate: Multivariate Normal (Cholesky), Dirichlet
* Faster large-λ Poisson (PTRS), faster large-n Binomial (BTPE)

---

## Determinism & RNGs

* By default, distributions use a high-quality LXM generator via `Randoms.defaultGenerator()`.
* For reproducible runs, use the **seeded constructors** or pass your own `RandomGenerator`.
* No hidden global state; any Box–Muller “spare” values are per-instance and documented.

---

## Threading

Instances are **not** synchronized. Prefer one instance per thread or supply thread-local/splittable RNGs. If you share an instance across threads, provide your own synchronization.

---

## Support metadata

Each distribution can report its **support** (bounds, open/closed, continuous/discrete) via `DistributionSupport`. This helps with validation, plotting axes, and clamping without poking at internals.

---

## Project status

* ✅ Bootstrapped (`UniformDouble`, `UniformInt`, API, RNG utilities, tests)
* 🚧 Building out the v1 list above
* 📄 Docs: put your site’s home at `docs/index.md`; Javadoc can live at `docs/javadoc/`


