# encoding: utf-8
require "logstash/namespace"
require "logstash/environment"
require "logstash/outputs/base"
require "logstash/json"
require 'json'
require 'securerandom'

require 'java'
require 'httpcore-4.2.4.jar'
require 'libthrift-0.9.1.jar'
require 'thrift-connector-1.0.0.jar'
java_import 'com.twitter.logstash.output.thriftrelay.PublisherService'

class LogStash::Outputs::ThriftRelayPublic < LogStash::Outputs::Base
  config_name "thrift" #name of the logstash config section

  #Parameters you can specify in the logstash configuration
  config :url, :validate => :string, :required => true
  config :stream, :validate => :string, :required => true
  config :oauth2_token, :validate => :string, :required => true
  @publisherService = nil

  public
  def register
    $stdout.puts("Thrift Plugin Configuration")
    $stdout.puts("url: " + @url)
    $stdout.puts("eventbus stream: " + @stream)
    @publisherService = PublisherService.new(@url, @oauth2_token, @stream)
  end # def register

  public
  def receive(event)
    if event == LogStash::SHUTDOWN
      @publisherService.close()
      return
    end
    @publisherService.send(event["message"].to_java_bytes)
  end # def event
end # class LogStash::Outputs::ThriftRelayPublic
