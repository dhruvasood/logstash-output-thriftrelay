package com.twitter.logstash.output.thriftrelay

import com.twitter.finagle.http.RequestBuilder
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.{Filter, Service, http}
import com.twitter.io.Buf
import com.twitter.util.Future

class ThriftHttpFilter(url: String, authorization: String)
  extends Filter[ThriftClientRequest, Array[Byte], http.Request, http.Response] {
  override def apply(request: ThriftClientRequest, service: Service[http.Request, http.Response]): Future[Array[Byte]] = {
    val httpRequest = RequestBuilder()
      .url(url)
      .addHeader("Content-Type", "application/x-thrift")
      .addHeader("Accept", "application/x-thrift")
      .addHeader("User-Agent", "JavaME/THttpClient")
      .addHeader("Connection", "Keep-Alive")
      .addHeader("Keep-Alive", "5000")
      .addHeader("Cache-Control", "no-transform")
      .addHeader("X-B3-Flags", "1")
      .addHeader("Authorization", authorization)
      .buildPost(Buf.ByteArray.Owned(request.message))
    val httpResponse = service(httpRequest)
    httpResponse.map(
      resp => Stream.continually(resp.getInputStream().read).takeWhile(_ != -1).map(_.toByte)
        .toArray)
  }
}