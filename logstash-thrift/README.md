# Thriftrelay Logstash Output Plugin

This is a plugin for [Logstash](https://github.com/elastic/logstash).

## Documentation
### Environment
- To get started, you'll need JRuby with the Bundler gem installed.
- You'll also need maven since this plugin also contains Java code which is built using maven

- Install dependencies
```sh
bundle install
```
### Build and install Gem in Logstash
- Go to the root path of the Thriftrelay plugin
- Compile the maven project

```sh
mvn compile
```
- Package the maven project
```sh
mvn package
```
- Build your plugin gem
```sh
gem build logstash-output-thriftrelay.gemspec
```
- Install the plugin from the Logstash home
```sh
/path/to/logstash/bin/plugin install /your/local/plugin/logstash-output-thriftrelay-1.0.0-java.gem
```
### Run the Loglens Logstash plugin
- Create Logstash config file, e.g.
```sh
# a random input... e.g. a file
input {
  file {
    path => "some file"
  }
}

#the config of the loglens output plugin
output {
  thriftrelay {
    url => "the url"
    stream => "relay" # the eventbus stream
    oauth2_token => "your oauth2 bearer token"
  }
}
```
- Start Logstash and proceed to test the plugin
```sh
/path/to/logstash/bin/logstash -f <configfile>
```
