package com.twitter.logstash.output.thriftrelay

import java.net.URI
import java.util.concurrent.{ArrayBlockingQueue, Executors, TimeUnit}

import com.twitter.conversions.time._
import com.twitter.eventbus.publisherservice.thriftscala.PublisherService
import com.twitter.finagle._
import com.twitter.finagle.service._
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.thrift.service.ThriftResponseClassifier
import com.twitter.finagle.util.DefaultTimer
import com.twitter.logging.Logger
import com.twitter.util._
import scribe.thrift.LogEntry

import scala.collection.JavaConversions._

class PublisherService(url: String,
                       token: String,
                       stream: String,
                       scribeBufferLen: Int = 10,
                       ratePerSec: Int = 500,
                       bufferSize: Int = 100,
                       timeoutSec: Duration = 1.second,
                       retryTTL: Duration = 30.seconds,
                       minRetriesPerSec: Int = 100,
                       percentRetries: Double = 1.0,
                       backoffStartDuration: Duration = 10.milliseconds,
                       backoffMaxDuration: Duration = 10.seconds) {
  val host = new URI(url).getHost
  val logger: Logger = Logger.get(classOf[PublisherService])
  val budget: RetryBudget = RetryBudget(
    retryTTL, minRetriesPerSec, percentRetries, Stopwatch.systemMillis)
  val policy: RetryPolicy[(http.Request, Try[http.Response])] = RetryPolicy
    .backoff(Backoff.equalJittered(backoffStartDuration, backoffMaxDuration)) {
      // TODO(sadhan): Add metrics which can alert on HTTP 429
      case (_, Return(rep)) if rep.status == http.Status.TooManyRequests => true
      // TODO(sadhan): Add metrics which can alert on server errors
      case (_, Return(rep)) if rep.status == http.Status.InternalServerError => true
      case (_, Return(rep)) if rep.status != http.Status.Ok =>
        // TODO(sadhan): Add metrics which can alert on other http errors
        logger.warning("Http failure with status code " + rep.status.reason)
        true
      case (_, Throw(e)) =>
        // TODO(sadhan): Add metrics which can alert on exception failures
        logger.warning(e, "Request failure because of an exception")
        true
    }
  val buffer = new ArrayBlockingQueue[LogEntry](bufferSize)
  val client: Service[http.Request, http.Response] = Http.client.withTls(host).withResponseClassifier(
    ThriftResponseClassifier.ThriftExceptionsAsFailures).newService(s"$host:443")
  val thriftHttpFilter = new ThriftHttpFilter(url, "Bearer " + token)
  val timeoutFilter = new TimeoutFilter[http.Request, http.Response](timeoutSec, DefaultTimer.twitter)
  // TODO(sadhan): Replace with a stats receiver where metrics could be exported and reported/alerted on
  val retryFilter = new RetryFilter(policy, DefaultTimer.twitter, NullStatsReceiver, budget)
  val retryTimedPublisherServiceClient = thriftHttpFilter.andThen(retryFilter).andThen(timeoutFilter).andThen(client)
  val publisherService = new PublisherService.FinagledClient(retryTimedPublisherServiceClient, stats = NullStatsReceiver)
  val executor = Executors.newSingleThreadScheduledExecutor()
  val runner = new Runnable { override def run(): Unit = throttle() }
  // Rate limit sending of events to the endpoint
  executor.scheduleAtFixedRate(runner, 0, 1000000/ratePerSec, TimeUnit.MICROSECONDS)

  def send(message: Array[Byte]): Unit = {
    val request = LogEntry(stream, Base64StringEncoder.encode(message))
    buffer.put(request)
  }

  def throttle(): Unit = {
    val request = List(buffer.take())
    buffer.drainTo(request, scribeBufferLen)
    val response = publisherService.log(buffer.toSeq)
    response.onFailure {
      case e@(_:GlobalRequestTimeoutException | _:IndividualRequestTimeoutException) =>
        // TODO(sadhan): Add metrics which can alert on for timeout failures
      case exc =>
        throw new RuntimeException(exc)
        // Throwing is not enough because there are background threads still running
        System.exit(1)
    }
  }

}