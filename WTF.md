# Q&A

## Why?

Dependency-Track is based on an in-memory [publish-subscribe](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) architecture.

On a technical level, the pub-sub architecture is implemented using Java 
[`ExecutorService`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ExecutorService.html)s.
An `ExecutorService` can be thought of as a pool of worker threads, consuming from an internal task queue. Tasks can be
submitted to an `ExecutorService`, which will then execute them one-by-one. As multiple threads work on the queue in 
parallel, the order in which tasks are being processed is not guaranteed. Thread pool sizes can vary from one, 
up to unbounded numbers of threads.

In Dependency-Track, when an event is published, subscribers to the event are looked up. Per API contract, event
subscribers must implement an `inform` method, which takes the published event as argument. The invocation of **all** 
subscriber's `inform` methods forms a task, which is enqueued to the `ExecutorService`s task queue.

![](docs/dependencytrack-executorservice.png)

There are three `ExecutorService` instances in Dependency-Track:

* [`EventService`](https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/event/framework/EventService.java)
* [`SingleThreadedEventService`](https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/event/framework/SingleThreadedEventService.java)
* [`NotificationService`](https://github.com/stevespringett/Alpine/blob/alpine-parent-2.2.0/alpine-infra/src/main/java/alpine/notification/NotificationService.java)

`EventService` forms the primary worker pool. Its thread pool size defaults to `<NUM_CPU> * 4`. 
A machine with a 4-core CPU will thus have a thread pool size of `16`. The size is configurable.
Common tasks handled by this worker pool include:

* Processing of uploaded BOMs and VEXs
* Performing vulnerability analysis of components, projects, or the entire portfolio
* Performing repository meta analysis of components, projects, or the entire portfolio
* Calculation of metrics for components, projects, or the entire portfolio

`SingleThreadedEventService` is a worker pool with only a single thread. The purpose of this worker pool is execute
tasks that must not be run in parallel. As such, it serves as a means to serialize task execution. 
Common tasks handled by this worker pool include:

* Mirroring of the [NVD](https://nvd.nist.gov/) vulnerability database
* Updating [Lucene](https://lucene.apache.org/) indexes on disk

`NotificationService` is a dedicated worker pool for dispatching [notifications](https://docs.dependencytrack.org/integrations/notifications/).
Its thread pool size defaults to `4` and is not configurable.

### Limitations

While this architecture works great for small to medium workloads, it presents various challenges for larger ones.

1. **Not horizontally scalable**. As pub-sub is happening entirely in-memory, it is not possible to distribute
the work to multiple application instances. The only way to handle more load using this architecture is to scale
vertically (increasing resource allocations for CPU, RAM, and potentially I/O).
2. **No ordering guarantees of events**. As multiple threads work on a shared queue of tasks in parallel, there is no way
of enforcing a reliable ordering of events. 
3. **Limited fault-tolerance**. If an instance of Dependency-Track goes down, planned or unplanned, all queued tasks are 
gone. Not only does this impact business-as-usual operation, but also limits the times when upgrades can be applied.
4. **Shared, multipurpose task queue**. A single task queue is used to process all kinds of events. This means that
lots of events of a certain type can "clog" the task queue, preventing other types of events from being processed.
This is further amplified if processing of events "clogging" the queue relies on external services, introducing further
latency. Ideally, there should be a dedicated queue per event type, so that one busy queue doesn't block others.
5. **Prone to race conditions**. As a consequence of (2), it is possible that multiple events addressing the same
thing are processed in parallel, leading to race conditions in cache lookups or database operations. Race conditions
would be an even bigger problem if the work was shared across multiple application instances, and would require
distributed locking as a countermeasure, which is [inherently hard to get right](https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html).

### Related Issues

* https://github.com/DependencyTrack/dependency-track/issues/218
* https://github.com/DependencyTrack/dependency-track/issues/903
* https://github.com/DependencyTrack/dependency-track/issues/1210
* https://github.com/DependencyTrack/dependency-track/issues/1856

## Why Kafka?

TBD

### Considered Alternatives

* RabbitMQ
* Apache Pulsar
* NATS (JetStream)
* Liftbridge

## Why Java?

TBD

## Why not microservices?

TBD
