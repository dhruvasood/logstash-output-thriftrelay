namespace java com.twitter.eventbus.publisherservice.thriftjava
namespace scala com.twitter.eventbus.publisherservice.thriftscala
namespace rb PublisherService

include "exceptions.thrift"
include "scribe.thrift"

struct Request {
  1: required string stream,
  2: required list<byte> event
}

/**
 * The main interface-definition for PublisherService.
 * Please use scribe-publisher_service.thrift  for scribe support.
 */
service PublisherService extends scribe.scribe {

  /**
   * TODO(sadhan): Enable this interface once it is in production
   *
  void publish(1: required Request req) throws (
    1: exceptions.ClientError clientError,
    2: exceptions.ServerError serverError
  )
  */

  /**
   * A basic uptime endpoint. Returns the server's uptime in milliseconds.
   */
  i64 uptime() throws (
    1: exceptions.ClientError clientError,
    2: exceptions.ServerError serverError
  )
}