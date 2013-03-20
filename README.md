Spring Batch Demo Project
=========================
A sample demo project that shows several features of the Spring Batch framework.

Job Launch options
------------------
The projects demonstrates following ways to launch a Spring Batch job:
* Command line
* HTTP request to an embedded Jetty server
* JMS message
* Launching a job from a runtime XML job description
* JMX console

Job execution parallelization
-------------------
* Single threaded
* Multithreaded partitioning
* Multithreaded remote partitioning with load balancing using a message broker
* Multithreaded asynchronous remote partitioning
 