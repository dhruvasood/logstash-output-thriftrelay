namespace rb ServoException
namespace java com.twitter.servo.exception.thriftjava
#@namespace scala com.twitter.servo.exception.thriftscala
namespace py gen.twitter.servo.exception

enum ClientErrorCause {
  /** Improperly-formatted request can't be fulfilled. */
  BAD_REQUEST     = 0,

  /** Required request authorization failed. */
  UNAUTHORIZED    = 1,

  /** Server timed out while fulfilling the request (and it's the client's fault). */
  REQUEST_TIMEOUT = 2,

  /** Initiating client has exceeded its maximum rate. */
  RATE_LIMITED    = 3,

  /** Reserved for later additional error types. */
  RESERVED_4      = 4,
  RESERVED_5      = 5,
  RESERVED_6      = 6
}

enum ServerErrorCause {
  /** Generic server error. */
  INTERNAL_SERVER_ERROR = 0,

  /** Server lacks the ablity to fulfill the request. */
  NOT_IMPLEMENTED       = 1,

  /** Request cannot be fulfilled due to error from dependent service. */
  DEPENDENCY_ERROR      = 2,

  /** Server is currently unavailable. */
  SERVICE_UNAVAILABLE   = 3,

  /** Server timed out while fulfilling the request (and it's the server's fault). */
  REQUEST_TIMEOUT       = 4,

  /** Reserved for later additional error types. */
  RESERVED_5            = 5,
  RESERVED_6            = 6,
  RESERVED_7            = 7
}

/**
 * Akin to 4xx status codes.
 */
exception ClientError {
  1: ClientErrorCause errorCause
  2: string message
}

/**
 * Akin to 5xx status codes.
 */
exception ServerError {
  1: ServerErrorCause errorCause
  2: string message
}