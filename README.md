# In-Memory Identity Resolution & Analytics Pipeline

A high-performance, real-time data streaming and identity resolution pipeline designed to ingest behavioral events from multiple sources, reconcile fragmented user identities in real time, and compute complex analytics metrics under low-latency constraints.

## Overview

In modern multi-platform digital ecosystems, users interact with applications across various devices and platforms (e.g., browsers and mobile apps). These interactions generate high-velocity telemetry data. However, at the time of data collection, a user's full identity is rarely available as a unified entity; instead, it is fragmented into distinct, overlapping identifiers (session cookies, device tokens, system IDs).

This project implements a lightweight, high-performance microservice designed to ingest these continuous telemetry streams, dynamically "stitch" or reconcile disparate identifiers into unified user profiles using an advanced **Dynamic Graph Connectivity** approach, and maintain global behavioral analytics in-memory.

## Core Architecture & Problem Domain

The system solves two distinct engineering challenges at the intersection of distributed systems and graph theory:

### 1. Real-Time Identity Resolution (Connected Components)
Mathematically, incoming data represents an evolving **Bipartite Graph** where nodes are either discrete behavioral events or individual identifier keys. Reconciling identities means tracking the **Connected Components** of this graph in real time.
* **Union Operations:** When an event arrives with multiple identifiers, it establishes linkages between them, merging separate components into a single unified user entity.
* **Dynamic Graph Connectivity (Edge Deletions):** The architecture supports an update mechanism where past associations can be overwritten. This requires an efficient edge-deletion and backtracking strategy to dynamically split components when a historical linkage is severed, maintaining accurate user-graph topology.

### 2. Stream-Based Aggregation
The service maintains highly optimized running metrics across the global user graph without relying on heavy external database abstractions:
* **Graph Traversal & Sizing:** Tracking total unique interconnected components.
* **State Machine & Behavioral Filters:** Filtering users based on specific chronological behavior criteria (e.g., identifying users who performed single touchpoints versus cross-platform multi-device engagement).

## System Design & Constraints

* **Pure In-Memory Engineering:** To maximize throughput and guarantee predictable sub-millisecond responses, the pipeline bypasses external persistent storage or caching engines. It is constructed entirely out of standard data structures tailored for safe concurrency and minimal garbage collection overhead.
* **Streaming vs. Batch Paradigm:** The system processes records individualistically as they stream via HTTP, updating the internal graph state atomically rather than relying on batch reconciliation windows.
* **Zero External Storage Libraries:** Built purely from native collection types to show mastery over custom data structure orchestration and algorithmic efficiency.

# Results About the Solution provided
```
received metrics: {UniqueUsers:1022 BouncedUsers:8 CrossDeviceUsers:979 ElapsedTime:1.426041ms}
expected metrics: {UniqueUsers:1022 BouncedUsers:8 CrossDeviceUsers:979 ElapsedTime:0s}
iteration performance score=0.99 (write=0.95, read=1.00)
score is (CorrectnessScore=9, PerformanceScore=8.23, Count=10, Result=0.88)
total score is 0.88
received metrics: {UniqueUsers:1031 BouncedUsers:12 CrossDeviceUsers:985 ElapsedTime:1.044792ms}
expected metrics: {UniqueUsers:1031 BouncedUsers:12 CrossDeviceUsers:985 ElapsedTime:0s}
iteration performance score=1.00 (write=1.00, read=1.00)
new score is (CorrectnessScore=9, PerformanceScore=9.19, Count=10, Result=0.91)
total score is 0.91
```
Note that the result might vary depending on the execution as every execution is randomized

## Execution

To run the application:

Run `sbt run` to start the application. 