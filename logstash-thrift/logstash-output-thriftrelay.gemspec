# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'logstash/outputs/thriftrelay/version'

Gem::Specification.new do |spec|
  spec.name          = "logstash-output-thriftrelay"
  spec.version       = Logstash::Output::ThriftRelay::VERSION
  spec.authors       = ["Sadhan Sood"]
  spec.email         = ["ssood@twitter.com"]

  spec.summary       = "description"
  spec.description   = "summary"

  # Files
  spec.files = Dir['lib/**/*', 'target/*.jar','spec/**/*','vendor/es**/*','*.gemspec','*.md','CONTRIBUTORS','Gemfile','LI    CENSE','NOTICE.TXT']
 
  # Tests
  spec.test_files = spec.files.grep(%r{^(test|spec|features)/})
 
  # Special flag to let us know this is actually a logstash plugin
  spec.metadata = { "logstash_plugin" => "true", "logstash_group" => "output" } 

  # Gem dependencies
  spec.add_runtime_dependency "logstash-core", ">= 2.0.0", "< 3.0.0"
  spec.add_runtime_dependency "json"
  spec.add_development_dependency "logstash-devutils"
  
  spec.require_paths = ["lib", "target"]

  spec.add_development_dependency "bundler", "~> 1.11"
  spec.add_development_dependency "rake", "~> 10.0"
  
end
